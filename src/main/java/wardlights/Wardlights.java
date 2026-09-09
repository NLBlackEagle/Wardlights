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