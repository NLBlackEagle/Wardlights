package wardlights.proxy;

import net.minecraft.entity.player.EntityPlayer;
import wardlights.client.render.AegisOfSacrificeBaubleRenderer;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
    }

    @Override
    public void renderAegisOfSacrificeBauble(EntityPlayer player, float partialTicks) {
        AegisOfSacrificeBaubleRenderer.render(player, partialTicks);
    }
}