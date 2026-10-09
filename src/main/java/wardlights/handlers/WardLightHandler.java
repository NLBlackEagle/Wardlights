package wardlights.handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import wardlights.Wardlights;
import wardlights.util.LanternUtil;

/**
 * Keeps the WardLight effect in sync with {@link LanternUtil#isWardActive}:
 * a short, normal effect that keeps getting topped up while the player's
 * lantern works, and is removed as soon as it stops.
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

        PotionEffect current = player.getActivePotionEffect(ModRegistry.WARDLIGHT);
        if (LanternUtil.isWardActive(player)) {
            if (current != null && (current.getDuration() > DURATION || current.getIsAmbient())) {
                // Left over from the old endless ambient version; combining can't shorten it, so start over.
                player.removePotionEffect(ModRegistry.WARDLIGHT);
                current = null;
            }
            if (current == null || current.getDuration() < REFRESH_BELOW) {
                player.addPotionEffect(new PotionEffect(ModRegistry.WARDLIGHT, DURATION, 0));
            }
        } else if (current != null) {
            player.removePotionEffect(ModRegistry.WARDLIGHT);
        }
    }
}
