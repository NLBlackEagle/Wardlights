package wardlights.handlers;

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
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import wardlights.Wardlights;
import wardlights.handlers.ForgeConfigHandler.ServerConfig.BaubleConfig;
import wardlights.items.ItemDispatcherCrown;

/**
 * The Blooming Crown's two traits (registered as dispatcher_crown).
 *
 * <p>Adaptation, done the way SRParasites' Sentient Armor does it
 * (SRPEventHandlerBus#entityHurt): each hit has a chance to add a point for
 * "the thing" that hit, and that thing's hits are then cut by points x the
 * per-point reduction, as a share of the incoming damage before anything else
 * (armor, other mods) touches it. Only a few things can be learned (the source
 * cap), and each one's points are capped. "The thing" is picked like SRP does:
 * the player's name, the hitting mob's type, or the damage type when the hit
 * came from something else (an arrow, a fireball...). Unlike SRP, hits with no
 * entity at all (fall, fire...) are learned by damage type too rather than
 * sharing one nameless entry.
 *
 * <p>Rage: always active while health is below a line (Rage I below 85%, Rage II
 * below 35% by default), topped up but never extended; Rage from anywhere else
 * (Rustic's wine extending it) is blocked on wearers.
 */
@Mod.EventBusSubscriber(modid = Wardlights.MODID)
public class DispatcherCrownHandler {

    private static final ResourceLocation RAGE_ID = new ResourceLocation(ModRegistry.SRPARASITES_MODID, "rage");

    @Nullable
    private static Potion rage;

    /** Same timings as the WardLight: 15 seconds, topped up before the HUD icon starts blinking. */
    private static final int RAGE_DURATION = 300;
    private static final int RAGE_REFRESH_BELOW = 220;

    /** Set while the crown adds Rage itself, so {@link #onPotionApplicable} lets it through. Server thread only. */
    private static boolean addingOwnRage;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        ItemStack crown = getWornCrown(event.getEntityLiving());
        BaubleConfig config = ForgeConfigHandler.server.baubles;
        // canHarmInCreative is only true for the void and /kill, which shouldn't be adapted to.
        if (crown == null || config.dispatcherCrownSourceCap <= 0 || event.getSource().canHarmInCreative()) {
            return;
        }
        String source = adaptationSource(event.getSource());
        if (isBlacklisted(source, config.dispatcherCrownBlacklist)) {
            return;
        }
        NBTTagCompound tag = ItemDispatcherCrown.getOrCreateTag(crown);
        NBTTagList adaptation = tag.getTagList(ItemDispatcherCrown.ADAPTATION_TAG, Constants.NBT.TAG_COMPOUND);

        // Like SRP, learn first, so a point gained on this hit already counts against it.
        if (event.getEntityLiving().getRNG().nextDouble() < config.dispatcherCrownLearnChance) {
            learn(adaptation, source, config.dispatcherCrownSourceCap);
            tag.setTag(ItemDispatcherCrown.ADAPTATION_TAG, adaptation);
        }
        float reduction = ItemDispatcherCrown.getReduction(ItemDispatcherCrown.getPoints(adaptation, source));
        event.setAmount(Math.max(0, event.getAmount() - event.getAmount() * reduction));
    }

    /** Adds a point to this source, or a new entry for it while there's room for one. */
    private static void learn(NBTTagList adaptation, String source, int sourceCap) {
        for (int i = 0; i < adaptation.tagCount(); i++) {
            NBTTagCompound entry = adaptation.getCompoundTagAt(i);
            if (entry.getString(ItemDispatcherCrown.SOURCE_TAG).equals(source)) {
                entry.setInteger(ItemDispatcherCrown.POINTS_TAG, entry.getInteger(ItemDispatcherCrown.POINTS_TAG) + 1);
                return;
            }
        }
        if (adaptation.tagCount() < sourceCap) {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString(ItemDispatcherCrown.SOURCE_TAG, source);
            entry.setInteger(ItemDispatcherCrown.POINTS_TAG, 1);
            adaptation.appendTag(entry);
        }
    }

    /** Same matching as SRP's blacklists: any entry contained in the source's name. */
    private static boolean isBlacklisted(String source, String[] blacklist) {
        for (String entry : blacklist) {
            if (!entry.isEmpty() && source.contains(entry)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Keeps the wearer's Rage at the level their health calls for: Rage II below
     * the Rage II line, Rage I below the Rage I line, none above. Called from the
     * crown's onWornTick a few times a second; it's short and topped up like the
     * WardLight, so it never lasts long past the moment health climbs back above.
     */
    public static void updateRage(EntityLivingBase wearer) {
        Potion potion = getRage();
        if (potion == null || wearer.world.isRemote) {
            return;
        }
        BaubleConfig config = ForgeConfigHandler.server.baubles;
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

    /** Same order SRP uses: a player's name, the hitting mob's type, else the damage type. */
    private static String adaptationSource(DamageSource source) {
        Entity attacker = source.getImmediateSource();
        if (attacker instanceof EntityPlayer) {
            return attacker.getName();
        }
        if (attacker instanceof EntityLivingBase) {
            ResourceLocation id = EntityList.getKey(attacker);
            if (id != null) {
                return id.toString();
            }
        }
        return source.getDamageType();
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
