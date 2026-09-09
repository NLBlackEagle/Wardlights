package wardlights;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wardlights.handlers.ModRegistry;
import wardlights.proxy.CommonProxy;

@Mod(modid = Wardlights.MODID, version = Wardlights.VERSION, name = Wardlights.NAME, dependencies = "required-after:fermiumbooter")
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
        ModRegistry.init();
        Wardlights.PROXY.preInit();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }
}

/*
todo: all the below stuff!

- Lanterns Main Concept: bauble, right-hand and toolbelt equipable: Pale Lantern = keeps darkness entities at bay
  (cancels targeting)
- Lantern types:
  Lantern 1; Regular Lantern - Creates a light source preventing darkness mobs using you as a excuse to materialize!
  Lantern 2; Pixie Infused - Creates an even bigger light source around the player and increases the player's mining speed
  Lantern 3; Vapula Infused - Tricks Vapula & Geonach into not spawning when knocking their house down
  Lantern 4; Pale Infused - Regular darkness entities fear you now instead!
  Lantern 5; Parasite Infused - prevents COTH & FEAR
- Lantern Looks:
  Lantern 1; Cast iron lantern on a chain, player holds it through the chain, perhaps it could do little swinging animations?
  Lantern 2; as above with a pink-ish glow and/or little particles
  Lantern 3; as above with a blu-ish particle added
  Lantern 4; as above but with white particles added in a larger radius around the player
  Lantern 5; as above but with red particles added
- Crafting:
  Lantern 1; 4x iron ingot 4x framed glass pane 1x glowing powder (Or 10 gold coins at the blacksmith)
  Lantern 2; 1x lantern + 4x pixie dust + 4x silver
  Lantern 3; 1x lantern + 4x vapulacrystal/geonach spear + 4x heart dust
  Lantern 4; 1x lantern + 4x weird root + 4x amber valve
  Lantern 5; 1x lantern + 4x essential segment + 4x quench

 */