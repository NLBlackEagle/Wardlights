package wardlights.client;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import wardlights.Wardlights;
import wardlights.bauble.ring.ViralLimitHandler;
import wardlights.config.WardlightsConfig;
import wardlights.init.ModRegistry;

/** Tooltip lines Wardlights adds to other mods' items. */
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Wardlights.MODID)
public class ClientTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Item poisonStone = ViralLimitHandler.getPoisonStone();
        if (poisonStone == null || event.getItemStack().getItem() != poisonStone
                || !Loader.isModLoaded(ModRegistry.SRPARASITES_MODID)) {
            return;
        }
        // Right under the name, where the Viral Ring's own line sits.
        List<String> tooltip = event.getToolTip();
        tooltip.add(Math.min(1, tooltip.size()), I18n.format("tooltip.wardlights.poison_stone.limit",
                I18n.format("enchantment.level." + WardlightsConfig.server.baubles.poisonStoneMaxViralLevel)));
    }
}
