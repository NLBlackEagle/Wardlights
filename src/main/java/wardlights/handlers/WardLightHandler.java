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
 * added while the player's lantern works, removed as soon as it stops.
 */
@Mod.EventBusSubscriber(modid = Wardlights.MODID)
public class WardLightHandler {

    /** Checked twice a second, which is plenty for a status icon. */
    private static final int CHECK_INTERVAL = 10;

    /**
     * Vanilla shows "**:**" instead of a countdown for this exact duration,
     * since it's what beacons and the like use for "until it stops".
     */
    private static final int ENDLESS = 32767;

    /** Topped back up well before it could run out. */
    private static final int REFRESH_BELOW = 200;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase != TickEvent.Phase.END || player.world.isRemote
                || player.ticksExisted % CHECK_INTERVAL != 0) {
            return;
        }

        PotionEffect current = player.getActivePotionEffect(ModRegistry.WARDLIGHT);
        if (LanternUtil.isWardActive(player)) {
            if (current == null || current.getDuration() < REFRESH_BELOW) {
                player.addPotionEffect(new PotionEffect(ModRegistry.WARDLIGHT, ENDLESS, 0, true, false));
            }
        } else if (current != null) {
            player.removePotionEffect(ModRegistry.WARDLIGHT);
        }
    }
}
