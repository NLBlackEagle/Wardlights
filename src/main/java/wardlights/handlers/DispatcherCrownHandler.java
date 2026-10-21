package wardlights.handlers;

import javax.annotation.Nullable;

import baubles.api.BaublesApi;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
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
 * The Dispatcher Crown's two traits.
 *
 * <p>Adaptation, done the way SRParasites' adapting mobs do it
 * (EntityPMalleable#attackEntityFrom): every hit teaches the wearer a little
 * about "the thing" that hit them, and the next hits from it are cut by what
 * they've learned, as a share of the incoming damage before anything else
 * (armor, other mods) touches it. "The thing" is picked like SRP does: the
 * weapon in a player's main hand, the attacking mob's type (the shooter for
 * arrows and such), or the damage type when there's no attacker (fall, fire...).
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
        if (crown == null || config.dispatcherCrownMaxAdaptation <= 0 || event.getSource().canHarmInCreative()) {
            return;
        }
        NBTTagCompound tag = ItemDispatcherCrown.getOrCreateTag(crown);
        NBTTagCompound adaptation = tag.getCompoundTag(ItemDispatcherCrown.ADAPTATION_TAG);

        // Hurt first with what's been learned so far, then learn from this hit.
        String key = adaptationKey(event.getSource());
        float reduction = adaptation.getFloat(key);
        event.setAmount(Math.max(0, event.getAmount() - event.getAmount() * reduction));

        float max = (float) config.dispatcherCrownMaxAdaptation;
        adaptation.setFloat(key, Math.min(max, reduction + (float) config.dispatcherCrownAdaptationStep));
        tag.setTag(ItemDispatcherCrown.ADAPTATION_TAG, adaptation);
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

    /** Same order SRP uses: the hitting mob itself, else whoever fired it, else the damage type. */
    private static String adaptationKey(DamageSource source) {
        Entity attacker = source.getImmediateSource() instanceof EntityLivingBase
                ? source.getImmediateSource() : source.getTrueSource();
        if (attacker instanceof EntityPlayer) {
            return ((EntityPlayer) attacker).getHeldItemMainhand().getItem().getRegistryName().toString();
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
