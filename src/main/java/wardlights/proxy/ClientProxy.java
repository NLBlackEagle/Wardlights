package wardlights.proxy;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import wardlights.client.LanternLightPack;
import wardlights.client.render.AegisOfSacrificeBaubleRenderer;
import wardlights.client.render.BulwarkOfValorBaubleRenderer;
import wardlights.compat.EnhancedVisualsCompat;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        // Generated OptiFine dynamic lights file; added before resources first load so OptiFine finds it.
        List<IResourcePack> builtInPacks = ObfuscationReflectionHelper.getPrivateValue(Minecraft.class,
                Minecraft.getMinecraft(), "field_110449_ao");
        builtInPacks.add(new LanternLightPack());
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