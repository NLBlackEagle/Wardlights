package wardlights.handlers;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import wardlights.Wardlights;
import wardlights.items.ItemLantern;
import wardlights.items.ItemLanternPale;
import wardlights.items.ItemLanternPixie;
import wardlights.items.ItemLanternSrp;
import wardlights.items.ItemLanternVapula;

@Mod.EventBusSubscriber(modid = Wardlights.MODID)
public class ModRegistry {

    public static Item LANTERN_REGULAR;
    public static Item LANTERN_PIXIE;
    public static Item LANTERN_PALE;
    public static Item LANTERN_SRP;
    public static Item LANTERN_VAPULA;

    public static void init() {

    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        LANTERN_REGULAR = new ItemLantern("lantern_regular");
        event.getRegistry().register(LANTERN_REGULAR);

        LANTERN_PIXIE = new ItemLanternPixie("lantern_pixie");
        event.getRegistry().register(LANTERN_PIXIE);

        LANTERN_PALE = new ItemLanternPale("lantern_pale");
        event.getRegistry().register(LANTERN_PALE);

        LANTERN_SRP = new ItemLanternSrp("lantern_srp");
        event.getRegistry().register(LANTERN_SRP);

        LANTERN_VAPULA = new ItemLanternVapula("lantern_vapula");
        event.getRegistry().register(LANTERN_VAPULA);
    }
}