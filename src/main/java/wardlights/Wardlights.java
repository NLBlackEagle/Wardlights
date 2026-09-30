package wardlights;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib3.GeckoLib;
import wardlights.compat.FirstAidCompat;
import wardlights.handlers.ModRegistry;
import wardlights.proxy.CommonProxy;

@Mod(modid = Wardlights.MODID, version = Wardlights.VERSION, name = Wardlights.NAME, dependencies = "required-after:fermiumbooter;required-after:geckolib3;required-after:baubles")
public class Wardlights {
    public static final String MODID = "wardlights";
    public static final String VERSION = "1.0.0";
    public static final String NAME = "Wardlights";
    public static final Logger LOGGER = LogManager.getLogger();

    @SidedProxy(clientSide = "wardlights.proxy.ClientProxy", serverSide = "wardlights.proxy.CommonProxy")
    public static CommonProxy PROXY;

    @Instance(MODID)
    public static Wardlights instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        GeckoLib.initialize();
        ModRegistry.init();
        Wardlights.PROXY.preInit();
        if (Loader.isModLoaded(FirstAidCompat.MODID)) {
            MinecraftForge.EVENT_BUS.register(new FirstAidCompat());
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }
}

/*
todo: [Lanterns]

- Lanterns Main Concept: bauble, right-hand and toolbelt equipable: Pale Lantern = keeps darkness entities at bay
  (cancels targeting) and make it a belt-equippable too - max 1 lantern active. (make each lantern have its own white and blacklist potion effects)
  only activates in darkness and/or underground (below y64), otherwise does not work. And add a effect called WardLight so people know
  when it is active.
- Lantern types:
  Lantern 1; Regular Lantern - Creates a light source preventing darkness mobs using you as a excuse to materialize! (only underground & darkness)
  Lantern 2; Pixie Infused - (prevents pixie stealing, even during light) add effects such as luck, & adds 5% chance below y25 for a mimic to spawn. (only underground & darkness)
  Lantern 3; Vapula Infused - Increase mining speed make vapula and geoachs mined into your ally temporary & increase their spawns (only underground & darkness)
  Lantern 4; Pale Infused - Have a entity list that will cancel their targeting if conditions met & make a whitelist of entities which will get pale bloom effect when they are nearby
  Lantern 5; Parasite Infused - make SRP targeting ignore you (also during light) <config.perc.times>
- Lantern Looks:
  Lantern 1; (Default) simple cast iron lantern with a candle burning.
  Lantern 2; Lavender like glow and little fire-fly pink/purple/white like particles floating about
  Lantern 3; Crystal floating in the center emitting blue-white light
  Lantern 4; Overgrown lantern, pale leaves, white light and white particles floating about.
  Lantern 5; Tendrils poking out making a twitching animation with red light emitted from within. SRP overgrowth
- Crafting:
  Lantern 1; 4x iron ingot 4x framed glass pane 1x glowing powder (Or 10 gold coins at the blacksmith)
  Lantern 2; 1x lantern + 4x pixie dust + 4x silver
  Lantern 3; 1x lantern + 4x vapulacrystal/geonach spear + 4x heart dust
  Lantern 4; 1x lantern + 4x weird root + 4x amber valve
  Lantern 5; 1x lantern + 4x essential segment + 4x quench

todo: [SOH 1 & 2]

- SOH 1 Recipe:
    <nuclearcraft:alloy:1>
    <potionfingers:ring:1>
    <nuclearcraft:alloy:1>
    <eaglemixins:badge_of_bravery>
    <xat:damage_shield>
    <srparasites:dispatcher_drop>
    <nuclearcraft:alloy:1>
    <srparasites:ada_bolster_drop>
    <nuclearcraft:alloy:1>

- SOH 2 Recipe:
    <eaglemixins:plutonium_blood_alloy>
    <potionfingers:ring:1>
    <eaglemixins:plutonium_blood_alloy>
    <eaglemixins:corrupted_fabric>
    <wardlights:bulwark_of_valor>
    <eaglemixins:corrupted_heart>
    <eaglemixins:plutonium_blood_alloy>
    <variedcommodities:orb:0>
    <eaglemixins:plutonium_blood_alloy>

- Make items in Eaglemixins for the SOH baubles:
  Corrupted Heart (collection of SRP parts) (black)
  Corrupted Fabric (collection of FUR parts) (black)
  Plutonium Alloy (Lithium + Plutonium) (grey-white-ish)
  Plutonium Blood Alloy (Blood Tears + Plutonium Alloy) (make it reddish)
  Badge of Bravery (Sold by brutal merchants for Blood & Blight essence)
  Blood Essence (dropped by "Blood" entities spawning during bloodmoon)
  Blight Essence (dropped by "Blight" entities during lycanites events)


todo: [Ring of Cleansing (LC) Hits through SRP adaptation and counters fear]

todo: [Necklace of Guarding (Teddy Bear + Blood Gem) (Immunity vs Corrosion & Counters Fear)]

todo: [Viral Ring - Limits viral to max 2]

todo: [Sentient Crown - Counters fear and gives adaptation of max 5%

todo: [Cyclops Eye - Gives strength 1 and looting 1]

todo: [Siren Scale - Swimming Speed 1]

 */