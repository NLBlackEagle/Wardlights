package wardlights.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import wardlights.client.render.AegisOfSacrificeBaubleRenderer;
import wardlights.client.render.BulwarkOfValorBaubleRenderer;
import wardlights.compat.EnhancedVisualsCompat;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        if (Loader.isModLoaded(EnhancedVisualsCompat.MODID)) {
            MinecraftForge.EVENT_BUS.register(new EnhancedVisualsCompat());
        }
    }

    @Override
    public void renderAegisOfSacrificeBauble(EntityPlayer player, float partialTicks) {
        AegisOfSacrificeBaubleRenderer.render(player, partialTicks);
    }

    @Override
    public void renderBulwarkOfValorBauble(EntityPlayer player, float partialTicks) {
        BulwarkOfValorBaubleRenderer.render(player, partialTicks);
    }
}