package wardlights.bauble.crown;

import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import baubles.api.BaublesApi;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import wardlights.Wardlights;
import wardlights.config.WardlightsConfig;
import wardlights.config.WardlightsConfig.ServerConfig.BaubleConfig;
import wardlights.init.ModRegistry;

/**
 * The Blooming Crown's two traits (registered as dispatcher_crown).
 *
 * <p>Adaptation works like Sentient Armor does in the pack, where SRPMixins'
 * adaptation overhaul replaces SRParasites' own. Each hit is named after what
 * directly hit (a player's name, a mob's id, else the damage type, so arrows are
 * "arrow"), then put through the type overrides. An unknown source has a chance
 * to be learned at 0 points while there's room (the source cap) and it isn't
 * blacklisted; a known one, including one learned this hit, has the same chance
 * to gain a point. Its hits are then cut by points x the per-point reduction,
 * points capped. Fire hits (see {@link AdaptationRules#fireTypes}) teach nothing
 * and are cut less. Unlike Sentient Armor, fire doesn't hurt the wearer more.
 * The rules come from SRParasites' and SRPMixins' configs when they're
 * installed ({@link AdaptationRules}); the numbers are the crown's own.
 *
 * <p>Rage: always active while health is below a line (Rage I below 85%, Rage II
 * below 35% by default), topped up but never extended; Rage from anywhere else
 * (Rustic's wine extending it) is blocked on wearers.
 */
@Mod.EventBusSubscriber(modid = Wardlights.MODID)
public class BloomingCrownHandler {

    private static final ResourceLocation RAGE_ID = new ResourceLocation(ModRegistry.SRPARASITES_MODID, "rage");

    @Nullable
    private static Potion rage;

    /** Same timings as the WardLight: 15 seconds, topped up before the HUD icon starts blinking. */
    private static final int RAGE_DURATION = 300;
    private static final int RAGE_REFRESH_BELOW = 220;

    /** Rage is checked twice a second, plenty for something that lasts 15 seconds per top-up. */
    private static final int RAGE_CHECK_INTERVAL = 10;

    /** Set while the crown adds Rage itself, so {@link #onPotionApplicable} lets it through. Server thread only. */
    private static boolean addingOwnRage;

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        EntityLivingBase wearer = event.getEntityLiving();
        ItemStack crown = getWornCrown(wearer);
        BaubleConfig config = WardlightsConfig.server.baubles;
        if (crown == null || config.dispatcherCrownSourceCap <= 0) {
            return;
        }
        DamageSource damage = event.getSource();
        Entity attacker = damage.getImmediateSource();
        boolean fromLiving = attacker instanceof EntityLivingBase;
        String source = adaptationSource(damage, attacker);
        if (source == null) {
            return;
        }
        source = AdaptationRules.override(source);
        if (source.equals(AdaptationRules.NEVER)) {
            return;
        }
        boolean fire = isFire(damage, wearer);

        NBTTagCompound tag = ItemBloomingCrown.getOrCreateTag(crown);
        NBTTagList adaptation = tag.getTagList(ItemBloomingCrown.ADAPTATION_TAG, Constants.NBT.TAG_COMPOUND);
        NBTTagCompound entry = findEntry(adaptation, source);
        // Fire hits teach nothing; they only get the (weakened) reduction below.
        if (!fire) {
            Random rng = wearer.getRNG();
            if (entry == null && adaptation.tagCount() < config.dispatcherCrownSourceCap
                    && rng.nextDouble() < config.dispatcherCrownLearnChance && !isBlacklisted(source, fromLiving)) {
                entry = new NBTTagCompound();
                entry.setString(ItemBloomingCrown.SOURCE_TAG, source);
                entry.setInteger(ItemBloomingCrown.POINTS_TAG, 0);
                adaptation.appendTag(entry);
                tag.setTag(ItemBloomingCrown.ADAPTATION_TAG, adaptation);
            }
            // A second roll, also on the hit that just added the source, so its first point takes two.
            if (entry != null && rng.nextDouble() < config.dispatcherCrownLearnChance) {
                entry.setInteger(ItemBloomingCrown.POINTS_TAG, entry.getInteger(ItemBloomingCrown.POINTS_TAG) + 1);
            }
        }
        if (entry == null) {
            return;
        }
        float reduction = ItemBloomingCrown.getReduction(entry.getInteger(ItemBloomingCrown.POINTS_TAG));
        if (fire) {
            reduction *= (float) AdaptationRules.burningEffectiveness();
        }
        event.setAmount(Math.max(0, event.getAmount() * (1 - reduction)));
    }

    @Nullable
    private static NBTTagCompound findEntry(NBTTagList adaptation, String source) {
        for (int i = 0; i < adaptation.tagCount(); i++) {
            NBTTagCompound entry = adaptation.getCompoundTagAt(i);
            if (entry.getString(ItemBloomingCrown.SOURCE_TAG).equals(source)) {
                return entry;
            }
        }
        return null;
    }

    /** A fire damage type, or "isBurning" in the list and the wearer is on fire. */
    private static boolean isFire(DamageSource damage, EntityLivingBase wearer) {
        Set<String> fireTypes = AdaptationRules.fireTypes();
        return fireTypes.contains(damage.getDamageType()) || fireTypes.contains("isBurning") && wearer.isBurning();
    }

    /**
     * Player and mob names are checked against the Mobs list, damage types
     * against the damage type list. Without SRPMixins' blacklist fix, names that
     * pass the Mobs list are checked against the damage type list too, like base SRP.
     */
    private static boolean isBlacklisted(String source, boolean fromLiving) {
        boolean inverted = AdaptationRules.blacklistInverted();
        if (fromLiving) {
            if (matches(source, AdaptationRules.blacklistMobs(), inverted)) {
                return true;
            }
            if (AdaptationRules.blacklistCheckFixed()) {
                return false;
            }
        }
        return matches(source, AdaptationRules.blacklistDamageTypes(), inverted);
    }

    /**
     * SRP's list matching: listed when the name contains any entry. Inverted, it's
     * listed when it contains none. An empty list never lists anything.
     */
    private static boolean matches(String source, String[] list, boolean inverted) {
        if (list.length == 0) {
            return false;
        }
        for (String entry : list) {
            if (source.contains(entry)) {
                return !inverted;
            }
        }
        return inverted;
    }

    /** From the crown's onWornTick. */
    public static void onWornTick(EntityLivingBase wearer) {
        if (wearer.ticksExisted % RAGE_CHECK_INTERVAL == 0) {
            updateRage(wearer);
        }
    }

    /**
     * Keeps the wearer's Rage at the level their health calls for: Rage II below
     * the Rage II line, Rage I below the Rage I line, none above. Called a
     * few times a second while the crown is worn; it's short and topped up like the
     * WardLight, so it never lasts long past the moment health climbs back above.
     */
    private static void updateRage(EntityLivingBase wearer) {
        Potion potion = getRage();
        if (potion == null || wearer.world.isRemote) {
            return;
        }
        BaubleConfig config = WardlightsConfig.server.baubles;
        float health = wearer.getHealth() / wearer.getMaxHealth() * 100;
        int amplifier = isBelow(health, config.dispatcherCrownRageIIHealth) ? 1
                : isBelow(health, config.dispatcherCrownRageIHealth) ? 0 : -1;
        PotionEffect current = wearer.getActivePotionEffect(potion);
        if (current != null && current.getAmplifier() != amplifier) {
            // Healed from Rage II to I, or above both lines: addPotionEffect can't lower a level.
            wearer.removePotionEffect(potion);
            current = null;
        }
        if (amplifier >= 0 && (current == null || current.getDuration() < RAGE_REFRESH_BELOW)) {
            addingOwnRage = true;
            wearer.addPotionEffect(new PotionEffect(potion, RAGE_DURATION, amplifier, false, true));
            addingOwnRage = false;
        }
    }

    /** Taking the crown off ends its Rage. */
    public static void removeRage(EntityLivingBase wearer) {
        Potion potion = getRage();
        if (potion != null && !wearer.world.isRemote) {
            wearer.removePotionEffect(potion);
        }
    }

    /**
     * Only the crown sets a wearer's Rage. Anything else adding it is denied,
     * which stops Rustic's wine (it re-adds every good effect with a longer
     * duration) and other sources from extending or replacing it.
     */
    @SubscribeEvent
    public static void onPotionApplicable(PotionEvent.PotionApplicableEvent event) {
        if (!addingOwnRage && event.getPotionEffect().getPotion() == getRage()
                && getWornCrown(event.getEntityLiving()) != null) {
            event.setResult(Event.Result.DENY);
        }
    }

    private static boolean isBelow(float health, int threshold) {
        return threshold > 0 && health < threshold;
    }

    /** The crown worn by this player, or null when they aren't a player wearing one. Server only. */
    @Nullable
    private static ItemStack getWornCrown(EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer) || entity.world.isRemote || ModRegistry.DISPATCHER_CROWN == null) {
            return null;
        }
        EntityPlayer player = (EntityPlayer) entity;
        int slot = BaublesApi.isBaubleEquipped(player, ModRegistry.DISPATCHER_CROWN);
        return slot < 0 ? null : BaublesApi.getBaublesHandler(player).getStackInSlot(slot);
    }

    /**
     * What the hit counts as, picked from the entity that directly hit: a
     * player's name, a mob's id, or else (a projectile, or nothing at all) the
     * damage type, so an arrow counts as "arrow" whoever shot it. Null for a mob
     * without an id, which SRPMixins skips entirely.
     */
    @Nullable
    private static String adaptationSource(DamageSource damage, @Nullable Entity attacker) {
        if (attacker instanceof EntityPlayer) {
            return attacker.getName();
        }
        if (attacker instanceof EntityLivingBase) {
            ResourceLocation id = EntityList.getKey(attacker);
            return id == null ? null : id.toString();
        }
        return damage.getDamageType();
    }

    /** SRParasites' Rage potion, or null when it isn't installed. */
    @Nullable
    private static Potion getRage() {
        if (rage == null) {
            rage = ForgeRegistries.POTIONS.getValue(RAGE_ID);
        }
        return rage;
    }
}
