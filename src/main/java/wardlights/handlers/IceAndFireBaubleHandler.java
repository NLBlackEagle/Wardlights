package wardlights.handlers;

import java.util.ArrayList;

import javax.annotation.Nullable;

import baubles.api.BaublesApi;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import wardlights.Wardlights;

/**
 * The Ice and Fire drop baubles: the Cyclops Eye (Strength I + Looting) and the
 * Siren Scale (Lycanites' Swiftswimming I).
 *
 * <p>The effects are endless potion effects that milk can't cure, added by the
 * items' onEquipped and removed by onUnequipped. They're added again:
 * <ul>
 * <li>on login and respawn: a respawn (keep-inventory, leaving the End) makes a
 * new player entity without effects, and Baubles copies the worn items over
 * without calling onEquipped;</li>
 * <li>when the effect runs out or is removed while the item is still worn, e.g.
 * a Strength II potion replaced ours and has now ended. That's done a tick
 * later, because the effect list is still being looped over when the event fires.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = Wardlights.MODID)
public class IceAndFireBaubleHandler {

    /** Effectively endless; the HUD shows it as **:**. */
    private static final int DURATION = Integer.MAX_VALUE;

    /** Anything longer than this is ours; no potion or beacon lasts anywhere near it. */
    private static final int OURS_ABOVE = 20 * 60 * 60 * 24;

    private static final ResourceLocation SWIFTSWIMMING_ID = new ResourceLocation(ModRegistry.LYCANITESMOBS_MODID,
            "swiftswimming");

    @Nullable
    private static Potion swiftswimming;

    public static void onCyclopsEyeEquipped(EntityLivingBase wearer) {
        addEffect(wearer, MobEffects.STRENGTH);
    }

    public static void onCyclopsEyeUnequipped(EntityLivingBase wearer) {
        removeEffect(wearer, MobEffects.STRENGTH);
    }

    public static void onSirenScaleEquipped(EntityLivingBase wearer) {
        addEffect(wearer, getSwiftswimming());
    }

    public static void onSirenScaleUnequipped(EntityLivingBase wearer) {
        removeEffect(wearer, getSwiftswimming());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        reapply(event.player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        reapply(event.player);
    }

    @SubscribeEvent
    public static void onPotionExpiry(PotionEvent.PotionExpiryEvent event) {
        reapplyLater(event.getEntityLiving(), event.getPotionEffect().getPotion());
    }

    @SubscribeEvent
    public static void onPotionRemove(PotionEvent.PotionRemoveEvent event) {
        reapplyLater(event.getEntityLiving(), event.getPotion());
    }

    @SubscribeEvent
    public static void onLootingLevel(LootingLevelEvent event) {
        if (event.getDamageSource() == null || !(event.getDamageSource().getTrueSource() instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getDamageSource().getTrueSource();
        if (isWearing(player, ModRegistry.CYCLOPS_EYE)) {
            event.setLootingLevel(event.getLootingLevel() + ForgeConfigHandler.server.baubles.cyclopsEyeLooting);
        }
    }

    /** Only for our two effects; the wearing check runs when the task does, so taking the item off wins. */
    private static void reapplyLater(EntityLivingBase entity, @Nullable Potion potion) {
        if (!(entity instanceof EntityPlayer) || entity.world.isRemote || potion == null
                || (potion != MobEffects.STRENGTH || ModRegistry.CYCLOPS_EYE == null)
                && (potion != getSwiftswimming() || ModRegistry.SIREN_SCALE == null)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;
        player.getServer().addScheduledTask(() -> reapply(player));
    }

    private static void reapply(EntityPlayer player) {
        if (isWearing(player, ModRegistry.CYCLOPS_EYE)) {
            onCyclopsEyeEquipped(player);
        }
        if (isWearing(player, ModRegistry.SIREN_SCALE)) {
            onSirenScaleEquipped(player);
        }
    }

    /** Level I, no particles, not curable by milk. Server only. */
    private static void addEffect(EntityLivingBase wearer, @Nullable Potion potion) {
        if (wearer.world.isRemote || potion == null) {
            return;
        }
        PotionEffect current = wearer.getActivePotionEffect(potion);
        if (current != null && (current.getAmplifier() > 0 || current.getDuration() > OURS_ABOVE)) {
            // Already ours, or a stronger one from somewhere else (ours comes back when that ends).
            return;
        }
        if (current != null) {
            // A level I from a potion: merging would keep its milk cure, so replace it outright.
            wearer.removePotionEffect(potion);
        }
        PotionEffect effect = new PotionEffect(potion, DURATION, 0, false, false);
        effect.setCurativeItems(new ArrayList<>());
        wearer.addPotionEffect(effect);
    }

    /** Only removes our own endless level I, never one from a potion or beacon. Server only. */
    private static void removeEffect(EntityLivingBase wearer, @Nullable Potion potion) {
        if (wearer.world.isRemote || potion == null) {
            return;
        }
        PotionEffect current = wearer.getActivePotionEffect(potion);
        if (current != null && current.getAmplifier() == 0 && current.getDuration() > OURS_ABOVE) {
            wearer.removePotionEffect(potion);
        }
    }

    private static boolean isWearing(EntityPlayer player, @Nullable Item bauble) {
        return bauble != null && BaublesApi.isBaubleEquipped(player, bauble) >= 0;
    }

    /** Lycanites Mobs' Swiftswimming, or null when it isn't installed. */
    @Nullable
    private static Potion getSwiftswimming() {
        if (swiftswimming == null) {
            swiftswimming = ForgeRegistries.POTIONS.getValue(SWIFTSWIMMING_ID);
        }
        return swiftswimming;
    }
}
