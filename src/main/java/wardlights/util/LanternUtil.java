package wardlights.util;

import javax.annotation.Nullable;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.Loader;
import wardlights.compat.ToolBeltCompat;
import wardlights.handlers.ForgeConfigHandler;
import wardlights.handlers.ForgeConfigHandler.ServerConfig.LanternConfig;
import wardlights.items.ItemLanternBase;
import wardlights.lantern.LanternType;

public final class LanternUtil {

    /** Brightest light level in the game; no light reaches this many blocks. */
    private static final int MAX_LIGHT = 15;

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

    /**
     * The most block light a placed lantern could be giving this spot: its light
     * value minus the blocks in between, since light drops by one per block. Walls
     * only make the real value lower, so this never undercounts. 0 when none reach.
     * Placed lanterns are tile entities, so only the few in nearby chunks are checked.
     */
    public static int getPlacedLanternLight(World world, BlockPos pos) {
        int best = 0;
        for (int cx = (pos.getX() - MAX_LIGHT) >> 4; cx <= (pos.getX() + MAX_LIGHT) >> 4; cx++) {
            for (int cz = (pos.getZ() - MAX_LIGHT) >> 4; cz <= (pos.getZ() + MAX_LIGHT) >> 4; cz++) {
                Chunk chunk = world.getChunkProvider().getLoadedChunk(cx, cz);
                if (chunk == null) {
                    continue;
                }
                for (TileEntity tile : chunk.getTileEntityMap().values()) {
                    BlockPos lanternPos = tile.getPos();
                    int distance = Math.abs(lanternPos.getX() - pos.getX())
                            + Math.abs(lanternPos.getY() - pos.getY())
                            + Math.abs(lanternPos.getZ() - pos.getZ());
                    if (distance >= MAX_LIGHT) {
                        continue;
                    }
                    IBlockState state = world.getBlockState(lanternPos);
                    if (Item.getItemFromBlock(state.getBlock()) instanceof ItemLanternBase) {
                        best = Math.max(best, state.getLightValue(world, lanternPos) - distance);
                    }
                }
            }
        }
        return best;
    }

    /** The configured darkness / underground check, at the player's position. */
    public static boolean meetsActivationCondition(EntityPlayer player) {
        LanternConfig config = ForgeConfigHandler.server.lanterns;
        BlockPos pos = new BlockPos(player);
        int light = player.world.getLightFromNeighbors(pos);
        // Light that placed lanterns could account for on their own doesn't count, so a lantern
        // never switches its own ward off; anything brighter (torches, daylight) still does.
        boolean dark = light <= config.darknessLightLevel || light <= getPlacedLanternLight(player.world, pos);
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
