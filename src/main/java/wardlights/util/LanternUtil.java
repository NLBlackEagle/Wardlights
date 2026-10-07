package wardlights.util;

import javax.annotation.Nullable;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Loader;
import wardlights.compat.ToolBeltCompat;
import wardlights.handlers.ForgeConfigHandler;
import wardlights.handlers.ForgeConfigHandler.ServerConfig.LanternConfig;
import wardlights.items.ItemLanternBase;
import wardlights.lantern.LanternType;

public final class LanternUtil {

    private LanternUtil() {
    }

    /**
     * The one lantern that counts for this player: a worn one first, then the
     * main hand, the off hand, and finally one stored in a Tool Belt. Empty
     * when they have none.
     */
    public static ItemStack getActiveLantern(EntityPlayer player) {
        ItemStack worn = getWornLantern(player);
        if (!worn.isEmpty()) {
            return worn;
        }
        for (EnumHand hand : EnumHand.values()) {
            ItemStack held = player.getHeldItem(hand);
            if (held.getItem() instanceof ItemLanternBase) {
                return held;
            }
        }
        if (Loader.isModLoaded(ToolBeltCompat.MODID)) {
            return ToolBeltCompat.findLanternInBelt(player);
        }
        return ItemStack.EMPTY;
    }

    /** The lantern in the player's Baubles slots, or empty. */
    public static ItemStack getWornLantern(EntityPlayer player) {
        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        if (baubles == null) {
            return ItemStack.EMPTY;
        }
        for (int slot = 0; slot < baubles.getSlots(); slot++) {
            ItemStack stack = baubles.getStackInSlot(slot);
            if (stack.getItem() instanceof ItemLanternBase) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /** The active lantern's type, or null when the player has none. */
    @Nullable
    public static LanternType getActiveLanternType(EntityPlayer player) {
        ItemStack lantern = getActiveLantern(player);
        return lantern.isEmpty() ? null : ((ItemLanternBase) lantern.getItem()).getLanternType();
    }

    /** True when the player has a lantern and the activation condition holds where they stand. */
    public static boolean isWardActive(EntityPlayer player) {
        return !getActiveLantern(player).isEmpty() && meetsActivationCondition(player);
    }

    /** The configured darkness / underground check, at the player's position. */
    public static boolean meetsActivationCondition(EntityPlayer player) {
        LanternConfig config = ForgeConfigHandler.server.lanterns;
        BlockPos pos = new BlockPos(player);
        boolean dark = player.world.getLightFromNeighbors(pos) <= config.darknessLightLevel;
        boolean underground = pos.getY() < config.undergroundYLevel;
        switch (config.activationCondition) {
            case DARKNESS:
                return dark;
            case UNDERGROUND:
                return underground;
            case BOTH:
                return dark && underground;
            case ALWAYS:
                return true;
            case EITHER:
            default:
                return dark || underground;
        }
    }
}
