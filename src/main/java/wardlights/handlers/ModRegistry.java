package wardlights.handlers;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import wardlights.Wardlights;
import wardlights.blocks.BlockLanternPale;
import wardlights.blocks.BlockLanternPixie;
import wardlights.blocks.BlockLanternRegular;
import wardlights.blocks.BlockLanternSrp;
import wardlights.blocks.BlockLanternVapula;
import wardlights.items.ItemAegisOfSacrifice;
import wardlights.items.ItemBulwarkOfValor;
import wardlights.items.ItemLantern;
import wardlights.items.ItemLanternPale;
import wardlights.items.ItemLanternPixie;
import wardlights.items.ItemLanternSrp;
import wardlights.items.ItemLanternVapula;
import wardlights.tileentity.TileEntityLanternPale;
import wardlights.tileentity.TileEntityLanternPixie;
import wardlights.tileentity.TileEntityLanternRegular;
import wardlights.tileentity.TileEntityLanternSrp;
import wardlights.tileentity.TileEntityLanternVapula;

@Mod.EventBusSubscriber(modid = Wardlights.MODID)
public class ModRegistry {

    public static Item LANTERN_REGULAR;
    public static Item LANTERN_PIXIE;
    public static Item LANTERN_PALE;
    public static Item LANTERN_SRP;
    public static Item LANTERN_VAPULA;
    /** Only registered when {@link #hasChestBaubles()}; null otherwise. */
    public static Item BULWARK_OF_VALOR;
    public static Item AEGIS_OF_SACRIFICE;

    public static Block LANTERN_REGULAR_BLOCK;
    public static Block LANTERN_PIXIE_BLOCK;
    public static Block LANTERN_PALE_BLOCK;
    public static Block LANTERN_SRP_BLOCK;
    public static Block LANTERN_VAPULA_BLOCK;

    public static final String TRINKETS_AND_BAUBLES_MODID = "xat";

    /**
     * The chest baubles are crafted as upgrades of Trinkets and Baubles' Shield
     * of Honor, so they only exist when that mod is installed.
     */
    public static boolean hasChestBaubles() {
        return Loader.isModLoaded(TRINKETS_AND_BAUBLES_MODID);
    }

    public static void init() {
        GameRegistry.registerTileEntity(TileEntityLanternRegular.class,
                new ResourceLocation(Wardlights.MODID, "lantern_regular"));
        GameRegistry.registerTileEntity(TileEntityLanternPixie.class,
                new ResourceLocation(Wardlights.MODID, "lantern_pixie"));
        GameRegistry.registerTileEntity(TileEntityLanternPale.class,
                new ResourceLocation(Wardlights.MODID, "lantern_pale"));
        GameRegistry.registerTileEntity(TileEntityLanternSrp.class,
                new ResourceLocation(Wardlights.MODID, "lantern_srp"));
        GameRegistry.registerTileEntity(TileEntityLanternVapula.class,
                new ResourceLocation(Wardlights.MODID, "lantern_vapula"));
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        LANTERN_REGULAR_BLOCK = new BlockLanternRegular("lantern_regular");
        LANTERN_PIXIE_BLOCK = new BlockLanternPixie("lantern_pixie");
        LANTERN_PALE_BLOCK = new BlockLanternPale("lantern_pale");
        LANTERN_SRP_BLOCK = new BlockLanternSrp("lantern_srp");
        LANTERN_VAPULA_BLOCK = new BlockLanternVapula("lantern_vapula");

        event.getRegistry().registerAll(LANTERN_REGULAR_BLOCK, LANTERN_PIXIE_BLOCK, LANTERN_PALE_BLOCK,
                LANTERN_SRP_BLOCK, LANTERN_VAPULA_BLOCK);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        LANTERN_REGULAR = new ItemLantern((BlockLanternRegular) LANTERN_REGULAR_BLOCK, "lantern_regular");
        LANTERN_PIXIE = new ItemLanternPixie((BlockLanternPixie) LANTERN_PIXIE_BLOCK, "lantern_pixie");
        LANTERN_PALE = new ItemLanternPale((BlockLanternPale) LANTERN_PALE_BLOCK, "lantern_pale");
        LANTERN_SRP = new ItemLanternSrp((BlockLanternSrp) LANTERN_SRP_BLOCK, "lantern_srp");
        LANTERN_VAPULA = new ItemLanternVapula((BlockLanternVapula) LANTERN_VAPULA_BLOCK, "lantern_vapula");

        event.getRegistry().registerAll(LANTERN_REGULAR, LANTERN_PIXIE, LANTERN_PALE, LANTERN_SRP, LANTERN_VAPULA);

        if (hasChestBaubles()) {
            BULWARK_OF_VALOR = new ItemBulwarkOfValor("bulwark_of_valor");
            AEGIS_OF_SACRIFICE = new ItemAegisOfSacrifice("aegis_of_sacrifice");
            event.getRegistry().registerAll(BULWARK_OF_VALOR, AEGIS_OF_SACRIFICE);
        }
    }
}
