package wardlights.lantern.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import wardlights.Wardlights;
import wardlights.init.ModRegistry;
import wardlights.lantern.LanternType;
import wardlights.lantern.LanternUtil;

/**
 * Picks each player's current lantern ({@link LanternUtil#updateCurrentLantern},
 * first come, first served) and keeps the WardLight effects in sync with it: only
 * the current lantern's WardLight, and only while its ward works. It's a short,
 * normal effect that keeps getting topped up, and is removed as soon as it stops. Lantern
 * effects that only apply while the ward is up (the Pixie Lantern's Luck I) work
 * the same way. None of them show particles: the lantern's own glow already
 * shows it, swirls would just be noise.
 */
@Mod.EventBusSubscriber(modid = Wardlights.MODID)
public class WardLightHandler {

    /** Checked twice a second, which is plenty for a status icon. */
    private static final int CHECK_INTERVAL = 10;

    /** 15 seconds per top-up. */
    private static final int DURATION = 300;

    /**
     * Vanilla makes the HUD icon blink once an effect has 200 ticks or less
     * left, so it's topped up just before it gets there.
     */
    private static final int REFRESH_BELOW = 220;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase != TickEvent.Phase.END || player.world.isRemote
                || player.ticksExisted % CHECK_INTERVAL != 0) {
            return;
        }

        LanternType current = LanternUtil.updateCurrentLantern(player);
        LanternType ward = current != null && LanternUtil.hasWardedLantern(player, current) ? current : null;
        for (LanternType type : LanternType.values()) {
            syncWardLight(player, ModRegistry.WARDLIGHTS.get(type), type == ward);
        }
        syncLanternEffect(player, MobEffects.LUCK, ward == LanternType.PIXIE);
    }

    private static void syncWardLight(EntityPlayer player, Potion wardLight, boolean warded) {
        PotionEffect current = player.getActivePotionEffect(wardLight);
        if (warded) {
            if (current != null && (current.getDuration() > DURATION || current.getIsAmbient())) {
                // Left over from the old endless ambient version; combining can't shorten it, so start over.
                player.removePotionEffect(wardLight);
                current = null;
            }
            if (current == null || current.getDuration() < REFRESH_BELOW) {
                player.addPotionEffect(new PotionEffect(wardLight, DURATION, 0, false, false));
            }
        } else if (current != null) {
            player.removePotionEffect(wardLight);
        }
    }

    /**
     * Same top-up / remove cycle for a vanilla effect a lantern grants. The player
     * can also have it from elsewhere (a drunk potion), so an effect that's
     * stronger or outlasts ours is never removed; adding ours can't shorten it either.
     */
    private static void syncLanternEffect(EntityPlayer player, Potion potion, boolean active) {
        PotionEffect current = player.getActivePotionEffect(potion);
        if (active) {
            if (current == null || current.getDuration() < REFRESH_BELOW) {
                player.addPotionEffect(new PotionEffect(potion, DURATION, 0, false, false));
            }
        } else if (current != null && current.getAmplifier() == 0 && current.getDuration() <= DURATION) {
            player.removePotionEffect(potion);
        }
    }
}
