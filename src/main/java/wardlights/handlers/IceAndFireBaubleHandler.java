package wardlights.handlers;

import java.util.UUID;

import baubles.api.BaublesApi;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import wardlights.Wardlights;

/**
 * The Ice and Fire drop baubles: the Cyclops Eye (Strength I + Looting) and the
 * Siren Scale (swim speed).
 *
 * <p>Strength and swim speed are attribute modifiers, added by the items'
 * onEquipped and removed by onUnequipped (server side; both attributes are
 * read or synced from there). Strength I is exactly what the vanilla potion
 * does, +3 attack damage, without a duration to keep topping up.
 *
 * <p>The modifiers aren't saved with the player, so they're added again on
 * login and respawn: a respawn (keep-inventory, leaving the End) makes a new
 * player entity with fresh attributes, and Baubles copies the worn items over
 * without calling onEquipped. Re-adding them on login also picks up config changes.
 */
@Mod.EventBusSubscriber(modid = Wardlights.MODID)
public class IceAndFireBaubleHandler {

    /** Fixed ids, so the modifiers can be found again to remove them. */
    private static final UUID STRENGTH_ID = UUID.fromString("9a4d2c61-3e7f-4b85-a0d2-6f1c8e5b7a42");
    private static final UUID SWIM_SPEED_ID = UUID.fromString("5c3e8f0a-7d1b-4e2a-9c6f-2b8d4a1e7f30");

    /** Vanilla Strength I: +3 attack damage. */
    private static final double STRENGTH_AMOUNT = 3;

    public static void onCyclopsEyeEquipped(EntityLivingBase wearer) {
        setModifier(wearer, SharedMonsterAttributes.ATTACK_DAMAGE, STRENGTH_ID, "Cyclops Eye", STRENGTH_AMOUNT, 0);
    }

    public static void onCyclopsEyeUnequipped(EntityLivingBase wearer) {
        setModifier(wearer, SharedMonsterAttributes.ATTACK_DAMAGE, STRENGTH_ID, "Cyclops Eye", 0, 0);
    }

    public static void onSirenScaleEquipped(EntityLivingBase wearer) {
        // Operation 1: adds bonus x base speed, so 0.5 = 50% faster.
        setModifier(wearer, EntityLivingBase.SWIM_SPEED, SWIM_SPEED_ID, "Siren Scale",
                ForgeConfigHandler.server.baubles.sirenScaleSwimSpeed, 1);
    }

    public static void onSirenScaleUnequipped(EntityLivingBase wearer) {
        setModifier(wearer, EntityLivingBase.SWIM_SPEED, SWIM_SPEED_ID, "Siren Scale", 0, 1);
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
    public static void onLootingLevel(LootingLevelEvent event) {
        if (event.getDamageSource() == null || !(event.getDamageSource().getTrueSource() instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getDamageSource().getTrueSource();
        if (isWearing(player, ModRegistry.CYCLOPS_EYE)) {
            event.setLootingLevel(event.getLootingLevel() + ForgeConfigHandler.server.baubles.cyclopsEyeLooting);
        }
    }

    private static void reapply(EntityPlayer player) {
        if (isWearing(player, ModRegistry.CYCLOPS_EYE)) {
            onCyclopsEyeEquipped(player);
        }
        if (isWearing(player, ModRegistry.SIREN_SCALE)) {
            onSirenScaleEquipped(player);
        }
    }

    /** Replaces the modifier with this amount; 0 just removes it. Server only. */
    private static void setModifier(EntityLivingBase wearer, IAttribute attribute, UUID id, String name,
            double amount, int operation) {
        IAttributeInstance instance = wearer.getEntityAttribute(attribute);
        if (wearer.world.isRemote || instance == null) {
            return;
        }
        // Modifiers can't be changed in place, and adding one whose id is already there throws.
        instance.removeModifier(id);
        if (amount > 0) {
            instance.applyModifier(new AttributeModifier(id, name, amount, operation).setSaved(false));
        }
    }

    private static boolean isWearing(EntityPlayer player, Item bauble) {
        return bauble != null && BaublesApi.isBaubleEquipped(player, bauble) >= 0;
    }
}
