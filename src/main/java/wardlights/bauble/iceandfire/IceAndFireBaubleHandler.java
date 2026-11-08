package wardlights.bauble.iceandfire;

import java.util.UUID;

import javax.annotation.Nullable;

import baubles.api.BaublesApi;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import wardlights.Wardlights;
import wardlights.bauble.WornAttributeBonus;
import wardlights.config.WardlightsConfig;
import wardlights.init.ModRegistry;

/**
 * The Ice and Fire drop baubles: the Cyclops Eye (attack damage + Looting) and
 * the Siren Scale (swim speed).
 *
 * <p>Both bonuses are attribute modifiers ({@link WornAttributeBonus}), so they
 * stack with Strength or Swiftswimming from anywhere else. The eye's +3 attack
 * damage is exactly one Strength level; the scale multiplies Forge's swim speed,
 * which is the same attribute Lycanites' Swiftswimming raises.
 */
@Mod.EventBusSubscriber(modid = Wardlights.MODID)
public class IceAndFireBaubleHandler {

    public static final WornAttributeBonus CYCLOPS_EYE_DAMAGE = new WornAttributeBonus(
            SharedMonsterAttributes.ATTACK_DAMAGE, UUID.fromString("e2f5567a-13fb-435f-b168-f103a1313684"),
            "Cyclops Eye", 0, () -> WardlightsConfig.server.baubles.cyclopsEyeAttackDamage);

    public static final WornAttributeBonus SIREN_SCALE_SWIM_SPEED = new WornAttributeBonus(
            EntityLivingBase.SWIM_SPEED, UUID.fromString("3f716f8a-defb-40ab-9367-3784d05607f2"),
            "Siren Scale", 2, () -> WardlightsConfig.server.baubles.sirenScaleSwimSpeed);

    /** Player data flag older versions set while the eye raised the player's Strength. */
    private static final String LEGACY_STRENGTH_RAISED = Wardlights.MODID + ":cyclopsEyeStrength";

    /** Older versions' endless effects lasted longer than this; no potion or beacon comes close. */
    private static final int LEGACY_ENDLESS_ABOVE = 20 * 60 * 60 * 24;

    private static final ResourceLocation SWIFTSWIMMING_ID = new ResourceLocation(ModRegistry.LYCANITESMOBS_MODID,
            "swiftswimming");

    /**
     * Fallback for a bauble that left its slot without onUnequipped (another mod
     * emptied the slot, say): its bonus goes as soon as it's no longer worn.
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase != TickEvent.Phase.END || player.world.isRemote) {
            return;
        }
        removeIfNotWorn(player, CYCLOPS_EYE_DAMAGE, ModRegistry.CYCLOPS_EYE);
        removeIfNotWorn(player, SIREN_SCALE_SWIM_SPEED, ModRegistry.SIREN_SCALE);
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

    /**
     * Older versions gave these bonuses as endless, milk-proof Strength I and
     * Swiftswimming I, saved with the player. Take those off once; the worn
     * baubles give their modifier from the next tick on.
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        if (player.getEntityData().hasKey(LEGACY_STRENGTH_RAISED)) {
            player.getEntityData().removeTag(LEGACY_STRENGTH_RAISED);
            removeLegacyEndless(player, MobEffects.STRENGTH);
        }
        removeLegacyEndless(player, ForgeRegistries.POTIONS.getValue(SWIFTSWIMMING_ID));
    }

    private static void removeLegacyEndless(EntityPlayer player, @Nullable Potion potion) {
        PotionEffect effect = potion == null ? null : player.getActivePotionEffect(potion);
        if (effect != null && effect.getAmplifier() == 0 && effect.getDuration() > LEGACY_ENDLESS_ABOVE) {
            player.removePotionEffect(potion);
        }
    }

    private static void removeIfNotWorn(EntityPlayer player, WornAttributeBonus bonus, @Nullable Item bauble) {
        // isApplied first: it's a map lookup, much cheaper than searching the bauble slots.
        if (bonus.isApplied(player) && !isWearing(player, bauble)) {
            bonus.remove(player);
        }
    }

    private static boolean isWearing(EntityPlayer player, @Nullable Item bauble) {
        return bauble != null && BaublesApi.isBaubleEquipped(player, bauble) >= 0;
    }
}
