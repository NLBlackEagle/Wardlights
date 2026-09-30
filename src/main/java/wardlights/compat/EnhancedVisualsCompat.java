package wardlights.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import team.creative.enhancedvisuals.api.event.VisualExplosionEvent;
import wardlights.handlers.ChestBaubleHandler;
import wardlights.handlers.ForgeConfigHandler;

/**
 * Removes Enhanced Visuals' explosion screen blur while a chest bauble is worn.
 * Only the blur: the event could be cancelled outright, but that would also
 * skip the explosion's dust and ear ringing.
 *
 * <p>Enhanced Visuals fires this client-side, so this is only registered from
 * {@link wardlights.proxy.ClientProxy}, and only when the mod is loaded since
 * this class references its API.
 */
public class EnhancedVisualsCompat {

    public static final String MODID = "enhancedvisuals";

    @SubscribeEvent
    public void onVisualExplosion(VisualExplosionEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null && ForgeConfigHandler.server.chestBaubles.disableExplosionBlur
                && !ChestBaubleHandler.getWornChestBauble(player).isEmpty()) {
            event.setDisableBlur(true);
        }
    }
}
