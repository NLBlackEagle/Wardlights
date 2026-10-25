package wardlights.init;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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
import wardlights.bauble.chest.ItemAegisOfSacrifice;
import wardlights.bauble.chest.ItemBulwarkOfValor;
import wardlights.bauble.crown.ItemBloomingCrown;
import wardlights.bauble.iceandfire.ItemCyclopsEye;
import wardlights.bauble.iceandfire.ItemSirenScale;
import wardlights.bauble.ring.ItemPiercingRing;
import wardlights.bauble.ring.ItemViralRing;
import wardlights.config.WardlightsConfig;
import wardlights.lantern.LanternType;
import wardlights.lantern.PotionWardLight;
import wardlights.lantern.block.BlockLanternPale;
import wardlights.lantern.block.BlockLanternPixie;
import wardlights.lantern.block.BlockLanternRegular;
import wardlights.lantern.block.BlockLanternSrp;
import wardlights.lantern.block.BlockLanternVapula;
import wardlights.lantern.item.ItemLantern;
import wardlights.lantern.item.ItemLanternPale;
import wardlights.lantern.item.ItemLanternPixie;
import wardlights.lantern.item.ItemLanternSrp;
import wardlights.lantern.item.ItemLanternVapula;
import wardlights.lantern.tileentity.TileEntityLanternPale;
import wardlights.lantern.tileentity.TileEntityLanternPixie;
import wardlights.lantern.tileentity.TileEntityLanternRegular;
import wardlights.lantern.tileentity.TileEntityLanternSrp;
import wardlights.lantern.tileentity.TileEntityLanternVapula;
import wardlights.material.ItemMaterial;
import wardlights.material.MaterialDropHandler;

@Mod.EventBusSubscriber(modid = Wardlights.MODID)
public class ModRegistry {

    /** One WardLight effect per lantern type, shown while that type's ward works for the player. */
    public static final Map<LanternType, Potion> WARDLIGHTS = new EnumMap<>(LanternType.class);

    public static Item LANTERN_REGULAR;
    public static Item LANTERN_PIXIE;
    public static Item LANTERN_PALE;
    public static Item LANTERN_SRP;
    public static Item LANTERN_VAPULA;
    /** Only registered when {@link #hasChestBaubles()}; null otherwise. */
    public static Item BULWARK_OF_VALOR;
    public static Item AEGIS_OF_SACRIFICE;
    /** Only registered when SRParasites is installed; null otherwise. */
    public static Item VIRAL_RING;
    public static Item DISPATCHER_CROWN;
    public static Item PIERCING_RING;
    /** Only registered when Ice and Fire is installed (dropped by its Cyclops / Sirens); null otherwise. */
    public static Item CYCLOPS_EYE;
    public static Item SIREN_SCALE;

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
    /** Viral Ring ingredient; always registered, added to chest loot by {@link MaterialDropHandler}. */
    public static Item ANCIENT_RING;
    /** Blooming Crown ingredient; always registered, added to chest loot like the Ancient Ring. */
    public static Item ANCIENT_CROWN;
    /** Viral Ring ingredient; dropped by Beckons ({@link MaterialDropHandler}). */
    public static Item BECKON_PEARL;
    /** Blooming Crown ingredient; dropped by Dispatchers ({@link MaterialDropHandler}). */
    public static Item DISPATCHER_BLOOM;

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
    public static final String PALEBLOOM_MODID = "palebloom";
    /** Fish's Undead Rising. */
    public static final String FISH_UNDEAD_RISING_MODID = "mod_lavacow";
    public static final String BLOODMOON_MODID = "bloodmoon";
    public static final String LYCANITESMOBS_MODID = "lycanitesmobs";
    public static final String ICEANDFIRE_MODID = "iceandfire";

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
        // Liquid colors match each lantern's light (the effects hide their particles, but some UIs show the color).
        registerWardLight(event, LanternType.REGULAR, 0xFFE8A0);
        registerWardLight(event, LanternType.PIXIE, 0xD9A0FF);
        registerWardLight(event, LanternType.VAPULA, 0xA0D8FF);
        registerWardLight(event, LanternType.PALE, 0xECECE4);
        registerWardLight(event, LanternType.PARASITE, 0xC03030);
    }

    private static void registerWardLight(RegistryEvent.Register<Potion> event, LanternType type, int color) {
        Potion potion = new PotionWardLight(type, color);
        WARDLIGHTS.put(type, potion);
        event.getRegistry().register(potion);
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
        if (Loader.isModLoaded(SRPARASITES_MODID)) {
            VIRAL_RING = new ItemViralRing("viral_ring");
            DISPATCHER_CROWN = new ItemBloomingCrown("dispatcher_crown");
            PIERCING_RING = new ItemPiercingRing("piercing_ring");
            event.getRegistry().registerAll(VIRAL_RING, DISPATCHER_CROWN, PIERCING_RING);
        }
        if (Loader.isModLoaded(ICEANDFIRE_MODID)) {
            CYCLOPS_EYE = new ItemCyclopsEye("cyclops_eye");
            SIREN_SCALE = new ItemSirenScale("siren_scale");
            event.getRegistry().registerAll(CYCLOPS_EYE, SIREN_SCALE);
        }

        PLUTONIUM_ALLOY = registerMaterial(event.getRegistry(), "plutonium_alloy", NUCLEARCRAFT_MODID);
        PLUTONIUM_BLOOD_ALLOY = registerMaterial(event.getRegistry(), "plutonium_blood_alloy", NUCLEARCRAFT_MODID,
                CONTENTTWEAKER_MODID);
        CORRUPTED_HEART = registerMaterial(event.getRegistry(), "corrupted_heart", SRPARASITES_MODID);
        CORRUPTED_FABRIC = registerMaterial(event.getRegistry(), "corrupted_fabric", FISH_UNDEAD_RISING_MODID);
        BLOOD_ESSENCE = registerMaterial(event.getRegistry(), "blood_essence", BLOODMOON_MODID);
        if (BLOOD_ESSENCE != null) {
            ((ItemMaterial) BLOOD_ESSENCE).setGlintColor(WardlightsConfig.client::getBloodEssenceGlintColor);
        }
        BLIGHT_ESSENCE = registerMaterial(event.getRegistry(), "blight_essence", LYCANITESMOBS_MODID);
        if (BLIGHT_ESSENCE != null) {
            ((ItemMaterial) BLIGHT_ESSENCE).setGlintColor(WardlightsConfig.client::getBlightEssenceGlintColor);
        }
        TAINTED_FLESH = registerMaterial(event.getRegistry(), "tainted_flesh", SRPARASITES_MODID);
        DORMANT_BADGE = registerMaterial(event.getRegistry(), "dormant_badge", BLOODMOON_MODID, LYCANITESMOBS_MODID,
                SRPARASITES_MODID);
        BADGE_OF_BRAVERY = registerMaterial(event.getRegistry(), "badge_of_bravery");
        if (BADGE_OF_BRAVERY != null) {
            ((ItemMaterial) BADGE_OF_BRAVERY).setGlintColor(WardlightsConfig.client::getBadgeOfBraveryGlintColor);
        }
        ANCIENT_RING = registerMaterial(event.getRegistry(), "ancient_ring");
        ANCIENT_CROWN = registerMaterial(event.getRegistry(), "ancient_crown");
        BECKON_PEARL = registerMaterial(event.getRegistry(), "beckon_pearl", SRPARASITES_MODID);
        DISPATCHER_BLOOM = registerMaterial(event.getRegistry(), "dispatcher_bloom", SRPARASITES_MODID);
    }
}
