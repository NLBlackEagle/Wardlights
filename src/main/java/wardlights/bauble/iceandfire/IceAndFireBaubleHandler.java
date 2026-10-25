package wardlights.bauble.iceandfire;

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
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import wardlights.Wardlights;
import wardlights.config.WardlightsConfig;
import wardlights.init.ModRegistry;

/**
 * The Ice and Fire drop baubles: the Cyclops Eye (+1 Strength level + Looting) and the
 * Siren Scale (Lycanites' Swiftswimming I).
 *
 * <p>The base effects are endless potion effects that milk can't cure, added by the
 * items' onEquipped and removed by onUnequipped. They're added again:
 * <ul>
 * <li>on login and respawn: a respawn (keep-inventory, leaving the End) makes a
 * new player entity without effects, and Baubles copies the worn items over
 * without calling onEquipped;</li>
 * <li>when the effect runs out or is removed while the item is still worn, e.g.
 * a Strength potion ran out. That's done a tick later, because the effect list
 * is still being looped over when the event fires.</li>
 * </ul>
 *
 * <p>The Cyclops Eye's Strength stacks: with no other Strength it's an endless
 * Strength I, and any other Strength is raised one level for as long as it lasts
 * (a Strength II potion becomes Strength III). Strength added while the eye is
 * worn is turned away in {@link #onPotionApplicable} and added again one level
 * higher; taking the eye off lowers it a level again.
 */
@Mod.EventBusSubscriber(modid = Wardlights.MODID)
public class IceAndFireBaubleHandler {

    /** Effectively endless; the HUD shows it as **:**. */
    private static final int DURATION = Integer.MAX_VALUE;

    /** Anything longer than this is ours; no potion or beacon lasts anywhere near it. */
    private static final int OURS_ABOVE = 20 * 60 * 60 * 24;

    /** Player data flag: set while the player's Strength includes the eye's extra level. */
    private static final String STRENGTH_RAISED = Wardlights.MODID + ":cyclopsEyeStrength";

    private static final ResourceLocation SWIFTSWIMMING_ID = new ResourceLocation(ModRegistry.LYCANITESMOBS_MODID,
            "swiftswimming");

    @Nullable
    private static Potion swiftswimming;

    /** True while we add Strength ourselves, so onPotionApplicable doesn't raise it a second time. */
    private static boolean addingStrength;

    /** Raises the wearer's Strength a level, or gives Strength I when there's none. Safe to call again. */
    public static void onCyclopsEyeEquipped(EntityLivingBase wearer) {
        if (wearer.world.isRemote) {
            return;
        }
        PotionEffect current = wearer.getActivePotionEffect(MobEffects.STRENGTH);
        if (current == null) {
            addStrength(wearer, endless(MobEffects.STRENGTH));
        } else if (!isStrengthRaised(wearer) && !isOurs(current)) {
            replaceStrength(wearer, current, current.getAmplifier() + 1);
        }
        wearer.getEntityData().setBoolean(STRENGTH_RAISED, true);
    }

    /** Takes the extra level back off; the eye's own Strength I just goes. Safe to call again. */
    public static void onCyclopsEyeUnequipped(EntityLivingBase wearer) {
        if (wearer.world.isRemote || !isStrengthRaised(wearer)) {
            return;
        }
        wearer.getEntityData().setBoolean(STRENGTH_RAISED, false);
        PotionEffect current = wearer.getActivePotionEffect(MobEffects.STRENGTH);
        if (current == null) {
            return;
        }
        if (isOurs(current)) {
            wearer.removePotionEffect(MobEffects.STRENGTH);
        } else if (current.getAmplifier() > 0) {
            replaceStrength(wearer, current, current.getAmplifier() - 1);
        }
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

    /**
     * Strength from anywhere else (potion, beacon, another mod) while the eye is
     * worn: turn it away and add it again one level higher. Vanilla then merges it
     * with the current one as usual, which works out because both are raised.
     */
    @SubscribeEvent
    public static void onPotionApplicable(PotionEvent.PotionApplicableEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        PotionEffect incoming = event.getPotionEffect();
        // Amplifiers are saved as a byte, so one past 127 would wrap around.
        if (addingStrength || entity.world.isRemote || incoming.getPotion() != MobEffects.STRENGTH
                || incoming.getAmplifier() >= Byte.MAX_VALUE || !isStrengthRaised(entity)) {
            return;
        }
        event.setResult(Event.Result.DENY);
        PotionEffect current = entity.getActivePotionEffect(MobEffects.STRENGTH);
        if (current != null && isOurs(current)) {
            // Merging into ours would keep it endless and uncurable, so swap it out.
            entity.removePotionEffect(MobEffects.STRENGTH);
        }
        addStrength(entity, copyWithAmplifier(incoming, incoming.getAmplifier() + 1));
    }

    @SubscribeEvent
    public static void onLootingLevel(LootingLevelEvent event) {
        if (event.getDamageSource() == null || !(event.getDamageSource().getTrueSource() instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getDamageSource().getTrueSource();
        if (isWearing(player, ModRegistry.CYCLOPS_EYE)) {
            event.setLootingLevel(event.getLootingLevel() + WardlightsConfig.server.baubles.cyclopsEyeLooting);
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
        } else {
            // The eye came off without onUnequipped (another mod emptied the slot, say).
            onCyclopsEyeUnequipped(player);
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
        wearer.addPotionEffect(endless(potion));
    }

    /** Our endless level I: no particles, not curable by milk. */
    private static PotionEffect endless(Potion potion) {
        PotionEffect effect = new PotionEffect(potion, DURATION, 0, false, false);
        effect.setCurativeItems(new ArrayList<>());
        return effect;
    }

    private static boolean isOurs(PotionEffect effect) {
        return effect.getAmplifier() == 0 && effect.getDuration() > OURS_ABOVE;
    }

    private static boolean isStrengthRaised(EntityLivingBase entity) {
        return entity.getEntityData().getBoolean(STRENGTH_RAISED);
    }

    /** Swaps the current Strength for the same one at another level, keeping its time left and cures. */
    private static void replaceStrength(EntityLivingBase entity, PotionEffect current, int amplifier) {
        PotionEffect replacement = copyWithAmplifier(current, amplifier);
        entity.removePotionEffect(MobEffects.STRENGTH);
        addStrength(entity, replacement);
    }

    private static PotionEffect copyWithAmplifier(PotionEffect effect, int amplifier) {
        PotionEffect copy = new PotionEffect(effect.getPotion(), effect.getDuration(), amplifier,
                effect.getIsAmbient(), effect.doesShowParticles());
        copy.setCurativeItems(new ArrayList<>(effect.getCurativeItems()));
        return copy;
    }

    /** Adds Strength without onPotionApplicable raising it. */
    private static void addStrength(EntityLivingBase entity, PotionEffect effect) {
        addingStrength = true;
        try {
            entity.addPotionEffect(effect);
        } finally {
            addingStrength = false;
        }
    }

    /** Only removes our own endless level I, never one from a potion or beacon. Server only. */
    private static void removeEffect(EntityLivingBase wearer, @Nullable Potion potion) {
        if (wearer.world.isRemote || potion == null) {
            return;
        }
        PotionEffect current = wearer.getActivePotionEffect(potion);
        if (current != null && isOurs(current)) {
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
