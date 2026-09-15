package wardlights.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumHand;
import wardlights.handlers.ModRegistry;

public final class LanternUtil {

    private LanternUtil() {
    }

    /**
     * Main/off-hand only for now — Baubles slot checking comes later once the
     * Baubles API is wired up as a dependency.
     */
    public static boolean isHoldingLantern(EntityPlayer player) {
        return isLantern(player.getHeldItem(EnumHand.MAIN_HAND).getItem())
                || isLantern(player.getHeldItem(EnumHand.OFF_HAND).getItem());
    }

    private static boolean isLantern(Item item) {
        return item == ModRegistry.LANTERN_REGULAR
                || item == ModRegistry.LANTERN_PIXIE
                || item == ModRegistry.LANTERN_PALE
                || item == ModRegistry.LANTERN_SRP
                || item == ModRegistry.LANTERN_VAPULA;
    }
}
