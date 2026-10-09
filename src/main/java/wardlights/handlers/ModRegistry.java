package wardlights.handlers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
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
import wardlights.items.ItemMaterial;
import wardlights.potion.PotionWardLight;
import wardlights.tileentity.TileEntityLanternPale;
import wardlights.tileentity.TileEntityLanternPixie;
import wardlights.tileentity.TileEntityLanternRegular;
import wardlights.tileentity.TileEntityLanternSrp;
import wardlights.tileentity.TileEntityLanternVapula;

@Mod.EventBusSubscriber(modid = Wardlights.MODID)
public class ModRegistry {

    /** Shown while a lantern is working. */
    public static Potion WARDLIGHT;

    public static Item LANTERN_REGULAR;
    public static Item LANTERN_PIXIE;
    public static Item LANTERN_PALE;
    public static Item LANTERN_SRP;
    public static Item LANTERN_VAPULA;
    /** Only registered when {@link #hasChestBaubles()}; null otherwise. */
    public static Item BULWARK_OF_VALOR;
    public static Item AEGIS_OF_SACRIFICE;

    /** Crafting ingredients; each is null when a mod it's made from isn't installed. */
    public static Item PLUTONIUM_ALLOY;
    public static Item PLUTONIUM_BLOOD_ALLOY;
    public static Item CORRUPTED_HEART;
    public static Item CORRUPTED_FABRIC;
    public static Item BLOOD_ESSENCE;
    public static Item BLIGHT_ESSENCE;
    /** Dropped by overworld parasites; EagleMixins' parasite loot handler hands it out. */
    public static Item TAINTED_FLESH;
    /** Blood Essence + Blight Essence + Tainted Flesh; a villager trade awakens it into the Badge of Bravery. */
    public static Item DORMANT_BADGE;
    /** Always registered: it has no source mod, only a recipe and a villager trade set up by the pack. */
    public static Item BADGE_OF_BRAVERY;

    public static Block LANTERN_REGULAR_BLOCK;
    public static Block LANTERN_PIXIE_BLOCK;
    public static Block LANTERN_PALE_BLOCK;
    public static Block LANTERN_SRP_BLOCK;
    public static Block LANTERN_VAPULA_BLOCK;

    public static final String TRINKETS_AND_BAUBLES_MODID = "xat";
    public static final String NUCLEARCRAFT_MODID = "nuclearcraft";
    /** Provides the pack's Blood Tear (contenttweaker:blood_tear). */
    public static final String CONTENTTWEAKER_MODID = "contenttweaker";
    public static final String SRPARASITES_MODID = "srparasites";
    /** Fish's Undead Rising. */
    public static final String FISH_UNDEAD_RISING_MODID = "mod_lavacow";
    public static final String BLOODMOON_MODID = "bloodmoon";
    public static final String LYCANITESMOBS_MODID = "lycanitesmobs";

    /**
     * Every crafting ingredient that actually got registered, so the client can
     * give them all a model in one loop. Items whose mods are missing never land here.
     */
    public static final List<Item> MATERIALS = new ArrayList<>();

    /** True only when every listed mod is installed. */
    public static boolean areModsLoaded(String... modIds) {
        for (String modId : modIds) {
            if (!Loader.isModLoaded(modId)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Registers a crafting ingredient only when all the mods it's made from are
     * installed; returns null otherwise, so the item simply doesn't exist then.
     */
    private static Item registerMaterial(IForgeRegistry<Item> registry, String name, String... requiredModIds) {
        return registerMaterial(registry, name, ItemMaterial.NO_TINT, requiredModIds);
    }

    /** Same as above, with a 0xRRGGBB color multiplied over the item's texture. */
    private static Item registerMaterial(IForgeRegistry<Item> registry, String name, int tintColor,
            String... requiredModIds) {
        if (!areModsLoaded(requiredModIds)) {
            return null;
        }
        Item item = new ItemMaterial(name, tintColor);
        registry.register(item);
        MATERIALS.add(item);
        return item;
    }

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
    public static void registerPotions(RegistryEvent.Register<Potion> event) {
        WARDLIGHT = new PotionWardLight();
        event.getRegistry().register(WARDLIGHT);
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

        PLUTONIUM_ALLOY = registerMaterial(event.getRegistry(), "plutonium_alloy", NUCLEARCRAFT_MODID);
        PLUTONIUM_BLOOD_ALLOY = registerMaterial(event.getRegistry(), "plutonium_blood_alloy", NUCLEARCRAFT_MODID,
                CONTENTTWEAKER_MODID);
        CORRUPTED_HEART = registerMaterial(event.getRegistry(), "corrupted_heart", SRPARASITES_MODID);
        CORRUPTED_FABRIC = registerMaterial(event.getRegistry(), "corrupted_fabric", FISH_UNDEAD_RISING_MODID);
        BLOOD_ESSENCE = registerMaterial(event.getRegistry(), "blood_essence", BLOODMOON_MODID);
        if (BLOOD_ESSENCE != null) {
            ((ItemMaterial) BLOOD_ESSENCE).setGlintColor(ForgeConfigHandler.client::getBloodEssenceGlintColor);
        }
        BLIGHT_ESSENCE = registerMaterial(event.getRegistry(), "blight_essence", LYCANITESMOBS_MODID);
        if (BLIGHT_ESSENCE != null) {
            ((ItemMaterial) BLIGHT_ESSENCE).setGlintColor(ForgeConfigHandler.client::getBlightEssenceGlintColor);
        }
        TAINTED_FLESH = registerMaterial(event.getRegistry(), "tainted_flesh", SRPARASITES_MODID);
        DORMANT_BADGE = registerMaterial(event.getRegistry(), "dormant_badge", BLOODMOON_MODID, LYCANITESMOBS_MODID,
                SRPARASITES_MODID);
        BADGE_OF_BRAVERY = registerMaterial(event.getRegistry(), "badge_of_bravery");
        if (BADGE_OF_BRAVERY != null) {
            ((ItemMaterial) BADGE_OF_BRAVERY).setGlintColor(ForgeConfigHandler.client::getBadgeOfBraveryGlintColor);
        }
    }
}
