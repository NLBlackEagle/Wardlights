package wardlights.client;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import wardlights.Wardlights;
import wardlights.client.render.LanternPaleBlockRenderer;
import wardlights.client.render.LanternPaleRenderer;
import wardlights.client.render.LanternPixieBlockRenderer;
import wardlights.client.render.LanternPixieRenderer;
import wardlights.client.render.LanternRegularBlockRenderer;
import wardlights.client.render.LanternRegularRenderer;
import wardlights.client.render.LanternSrpBlockRenderer;
import wardlights.client.render.LanternSrpRenderer;
import wardlights.client.render.LanternVapulaBlockRenderer;
import wardlights.client.render.LanternVapulaRenderer;
import wardlights.handlers.ModRegistry;
import wardlights.tileentity.TileEntityLanternPale;
import wardlights.tileentity.TileEntityLanternPixie;
import wardlights.tileentity.TileEntityLanternRegular;
import wardlights.tileentity.TileEntityLanternSrp;
import wardlights.tileentity.TileEntityLanternVapula;

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
        if (ModRegistry.hasChestBaubles()) {
            ModelLoader.setCustomModelResourceLocation(ModRegistry.BULWARK_OF_VALOR, 0,
                    new ModelResourceLocation(ModRegistry.BULWARK_OF_VALOR.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(ModRegistry.AEGIS_OF_SACRIFICE, 0,
                    new ModelResourceLocation(ModRegistry.AEGIS_OF_SACRIFICE.getRegistryName(), "inventory"));
        }

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLanternRegular.class, new LanternRegularBlockRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLanternPixie.class, new LanternPixieBlockRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLanternPale.class, new LanternPaleBlockRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLanternSrp.class, new LanternSrpBlockRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLanternVapula.class, new LanternVapulaBlockRenderer());
    }
}
