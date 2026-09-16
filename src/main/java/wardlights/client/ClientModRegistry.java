package wardlights.client;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import wardlights.Wardlights;
import wardlights.client.render.LanternPaleRenderer;
import wardlights.client.render.LanternPixieRenderer;
import wardlights.client.render.LanternRegularRenderer;
import wardlights.client.render.LanternSrpRenderer;
import wardlights.client.render.LanternVapulaRenderer;
import wardlights.handlers.ModRegistry;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Wardlights.MODID)
public class ClientModRegistry {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModRegistry.LANTERN_REGULAR.setTileEntityItemStackRenderer(LanternRegularRenderer.INSTANCE);
        ModRegistry.LANTERN_PIXIE.setTileEntityItemStackRenderer(LanternPixieRenderer.INSTANCE);
        ModRegistry.LANTERN_PALE.setTileEntityItemStackRenderer(LanternPaleRenderer.INSTANCE);
        ModRegistry.LANTERN_SRP.setTileEntityItemStackRenderer(LanternSrpRenderer.INSTANCE);
        ModRegistry.LANTERN_VAPULA.setTileEntityItemStackRenderer(LanternVapulaRenderer.INSTANCE);

        ModelLoader.setCustomModelResourceLocation(ModRegistry.LANTERN_REGULAR, 0,
                new ModelResourceLocation(ModRegistry.LANTERN_REGULAR.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(ModRegistry.LANTERN_PIXIE, 0,
                new ModelResourceLocation(ModRegistry.LANTERN_PIXIE.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(ModRegistry.LANTERN_PALE, 0,
                new ModelResourceLocation(ModRegistry.LANTERN_PALE.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(ModRegistry.LANTERN_SRP, 0,
                new ModelResourceLocation(ModRegistry.LANTERN_SRP.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(ModRegistry.LANTERN_VAPULA, 0,
                new ModelResourceLocation(ModRegistry.LANTERN_VAPULA.getRegistryName(), "inventory"));
    }
}
