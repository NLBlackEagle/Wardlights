package wardlights.client;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ColorHandlerEvent;
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
import wardlights.items.ItemMaterial;
import wardlights.tileentity.TileEntityLanternPale;
import wardlights.tileentity.TileEntityLanternPixie;
import wardlights.tileentity.TileEntityLanternRegular;
import wardlights.tileentity.TileEntityLanternSrp;
import wardlights.tileentity.TileEntityLanternVapula;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Wardlights.MODID)
public class ClientModRegistry {

    /**
     * Tints materials that reuse a vanilla texture. tintIndex is the model's
     * layer number (item/generated gives layer0 index 0); -1 (0xFFFFFFFF) is white,
     * which leaves the texture unchanged.
     */
    @SubscribeEvent
    public static void registerItemColors(ColorHandlerEvent.Item event) {
        for (Item material : ModRegistry.MATERIALS) {
            int tint = ((ItemMaterial) material).getTintColor();
            if (tint != ItemMaterial.NO_TINT) {
                event.getItemColors().registerItemColorHandler(
                        (stack, tintIndex) -> tintIndex == 0 ? tint : -1, material);
            }
        }
    }

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
        for (Item material : ModRegistry.MATERIALS) {
            ModelLoader.setCustomModelResourceLocation(material, 0,
                    new ModelResourceLocation(material.getRegistryName(), "inventory"));
        }
        if (ModRegistry.hasChestBaubles()) {
            ModelLoader.setCustomModelResourceLocation(ModRegistry.BULWARK_OF_VALOR, 0,
                    new ModelResourceLocation(ModRegistry.BULWARK_OF_VALOR.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(ModRegistry.AEGIS_OF_SACRIFICE, 0,
                    new ModelResourceLocation(ModRegistry.AEGIS_OF_SACRIFICE.getRegistryName(), "inventory"));
        }
        if (ModRegistry.VIRAL_RING != null) {
            ModelLoader.setCustomModelResourceLocation(ModRegistry.VIRAL_RING, 0,
                    new ModelResourceLocation(ModRegistry.VIRAL_RING.getRegistryName(), "inventory"));
        }
        if (ModRegistry.PIERCING_RING != null) {
            ModelLoader.setCustomModelResourceLocation(ModRegistry.PIERCING_RING, 0,
                    new ModelResourceLocation(ModRegistry.PIERCING_RING.getRegistryName(), "inventory"));
        }
        if (ModRegistry.DISPATCHER_CROWN != null) {
            ModelLoader.setCustomModelResourceLocation(ModRegistry.DISPATCHER_CROWN, 0,
                    new ModelResourceLocation(ModRegistry.DISPATCHER_CROWN.getRegistryName(), "inventory"));
        }
        if (ModRegistry.CYCLOPS_EYE != null) {
            ModelLoader.setCustomModelResourceLocation(ModRegistry.CYCLOPS_EYE, 0,
                    new ModelResourceLocation(ModRegistry.CYCLOPS_EYE.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(ModRegistry.SIREN_SCALE, 0,
                    new ModelResourceLocation(ModRegistry.SIREN_SCALE.getRegistryName(), "inventory"));
        }

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLanternRegular.class, new LanternRegularBlockRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLanternPixie.class, new LanternPixieBlockRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLanternPale.class, new LanternPaleBlockRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLanternSrp.class, new LanternSrpBlockRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLanternVapula.class, new LanternVapulaBlockRenderer());
    }
}
