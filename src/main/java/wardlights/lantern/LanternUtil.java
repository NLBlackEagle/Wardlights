package wardlights.lantern;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.Loader;
import wardlights.Wardlights;
import wardlights.compat.ToolBeltCompat;
import wardlights.config.WardlightsConfig;
import wardlights.config.WardlightsConfig.ServerConfig.LanternConfig;
import wardlights.lantern.item.ItemLanternBase;

public final class LanternUtil {

    /** Player data: the name of their current lantern type (see {@link #getCurrentLantern}). */
    private static final String CURRENT_LANTERN_TAG = Wardlights.MODID + ":current_lantern";

    /** Brightest light level in the game; no light reaches this many blocks. */
    private static final int MAX_LIGHT = 15;

    private LanternUtil() {
    }

    /** Placed lanterns do their work twice a second, on the ticks where this is true. */
    public static boolean isPlacedLanternCheckTick(World world) {
        return world.getTotalWorldTime() % 10 == 0;
    }

    /** The "Lantern Light Level" config: how bright every lantern is, placed or carried. */
    public static int getLanternLightLevel() {
        return WardlightsConfig.server.lanterns.lanternLightLevel;
    }

    /**
     * True when b is within the "Lantern Radius" of a. Distance counts like light
     * spreading: steps along the axes, no diagonals. A light of level N is still 1
     * or more up to N - 1 steps away, so a radius of N reaches exactly that far.
     */
    public static boolean isWithinLanternRadius(BlockPos a, BlockPos b) {
        return getSteps(a, b) < WardlightsConfig.server.lanterns.lanternRadius;
    }

    /** Blocks between a and b the way light travels: along the axes, never diagonally. */
    private static int getSteps(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY()) + Math.abs(a.getZ() - b.getZ());
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

    /**
     * True when the player carries any lantern and the activation condition holds
     * where they stand. Not limited to the current lantern: every lantern blocks spawns.
     */
    public static boolean isCarriedWardActive(EntityPlayer player) {
        return !getActiveLantern(player).isEmpty() && meetsActivationCondition(player.world, new BlockPos(player));
    }

    /**
     * The one lantern whose traits apply to the player, first come, first served:
     * the first lantern to reach them (carried, or placed in range) stays theirs
     * while it still reaches them, whatever else comes along. Null when none does.
     *
     * <p>Picked by {@link #updateCurrentLantern} twice a second and kept in the
     * player's data, so trait checks are a quick read (at most half a second behind).
     */
    @Nullable
    public static LanternType getCurrentLantern(EntityPlayer player) {
        String name = player.getEntityData().getString(CURRENT_LANTERN_TAG);
        if (name.isEmpty()) {
            return null;
        }
        try {
            return LanternType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Picks the player's current lantern. A carried lantern always wins: your own
     * lantern always counts. Without one, placed lanterns in range decide: the
     * current one stays while it still reaches the player and works; otherwise
     * the first working one in line takes over (see {@link #findPlacedLanterns}
     * for the order). A current one that reaches but doesn't work only gives way
     * to a working one, so with none working it stays. Reach itself ignores the
     * activation condition, so a working ward never flips as the light changes.
     * Lanterns with daylight traits (Pixie, Parasite) always count as working.
     */
    @Nullable
    public static LanternType updateCurrentLantern(EntityPlayer player) {
        LanternType current = getCurrentLantern(player);
        LanternType next = getActiveLanternType(player);
        if (next == null) {
            List<PlacedLantern> placed = findPlacedLanterns(player.world, new BlockPos(player));
            boolean currentReaches = current != null && placed.stream().anyMatch(lantern -> lantern.type == current);
            boolean currentWorks = currentReaches
                    && placed.stream().anyMatch(lantern -> lantern.type == current && lantern.works);
            if (currentWorks) {
                next = current;
            } else {
                // The first working one in line; with none working, keep the current one if it still reaches.
                LanternType fallback = currentReaches ? current : placed.isEmpty() ? null : placed.get(0).type;
                next = placed.stream().filter(lantern -> lantern.works).map(lantern -> lantern.type)
                        .findFirst().orElse(fallback);
            }
        }

        if (next != current) {
            if (next != null) {
                player.getEntityData().setString(CURRENT_LANTERN_TAG, next.name());
            } else {
                player.getEntityData().removeTag(CURRENT_LANTERN_TAG);
            }
        }
        return next;
    }

    /** Whether a lantern of this type at pos counts as working when picking a player's current lantern. */
    private static boolean worksAt(LanternType type, World world, BlockPos pos) {
        return type.hasDaylightTraits() || meetsActivationCondition(world, pos);
    }

    /** A placed lantern within reach of a player: its type, how far away, and whether it works. */
    private static final class PlacedLantern {
        final LanternType type;
        final int steps;
        final boolean works;

        PlacedLantern(LanternType type, int steps, boolean works) {
            this.type = type;
            this.steps = steps;
            this.works = works;
        }
    }

    /**
     * The placed lanterns within the lantern radius of pos, in the order they'd
     * become a player's ward: by type priority ({@link LanternType#getPlacedPriority},
     * Parasite, then Pixie, then the rest), then nearest first.
     */
    private static List<PlacedLantern> findPlacedLanterns(World world, BlockPos pos) {
        List<PlacedLantern> found = new ArrayList<>();
        forEachPlacedLantern(world, pos, WardlightsConfig.server.lanterns.lanternRadius,
                (lanternPos, state, type) -> {
                    if (isWithinLanternRadius(lanternPos, pos)) {
                        found.add(new PlacedLantern(type, getSteps(lanternPos, pos), worksAt(type, world, lanternPos)));
                    }
                    return false;
                });
        found.sort(Comparator.<PlacedLantern>comparingInt(lantern -> lantern.type.getPlacedPriority())
                .thenComparingInt(lantern -> lantern.steps));
        return found;
    }

    /**
     * True when this lantern type is the player's current one, ignoring the
     * activation condition. For traits that work day and night.
     */
    public static boolean hasLantern(EntityPlayer player, LanternType type) {
        return getCurrentLantern(player) == type;
    }

    /**
     * True when this lantern type is the player's current one and its ward works:
     * carried with the activation condition holding where they stand, or a working
     * placed one in range.
     */
    public static boolean hasWardedLantern(EntityPlayer player, LanternType type) {
        if (getCurrentLantern(player) != type) {
            return false;
        }
        BlockPos pos = new BlockPos(player);
        return (getActiveLanternType(player) == type && meetsActivationCondition(player.world, pos))
                || isNearWorkingPlacedLantern(player.world, pos, type);
    }

    /**
     * True when this lantern type is the player's current one, they carry it and
     * its ward works where they stand. For traits only a carried lantern gives.
     */
    public static boolean hasCarriedWardedLantern(EntityPlayer player, LanternType type) {
        return getCurrentLantern(player) == type && getActiveLanternType(player) == type
                && meetsActivationCondition(player.world, new BlockPos(player));
    }

    /**
     * True when a placed lantern (of this type, or any type when null) within the
     * lantern radius of pos is working: the activation condition holds where
     * the lantern stands.
     */
    public static boolean isNearWorkingPlacedLantern(World world, BlockPos pos, @Nullable LanternType type) {
        return isNearPlacedLantern(world, pos, type, true);
    }

    private static boolean isNearPlacedLantern(World world, BlockPos pos, @Nullable LanternType type,
            boolean mustWork) {
        return forEachPlacedLantern(world, pos, WardlightsConfig.server.lanterns.lanternRadius,
                (lanternPos, state, lanternType) ->
                (type == null || lanternType == type) && isWithinLanternRadius(lanternPos, pos)
                        && (!mustWork || meetsActivationCondition(world, lanternPos)));
    }

    /**
     * The most block light a placed lantern could be giving this spot: its light
     * value minus the blocks in between, since light drops by one per block. Walls
     * only make the real value lower, so this never undercounts. 0 when none reach.
     */
    public static int getPlacedLanternLight(World world, BlockPos pos) {
        int[] best = {0};
        forEachPlacedLantern(world, pos, MAX_LIGHT, (lanternPos, state, lanternType) -> {
            best[0] = Math.max(best[0], state.getLightValue(world, lanternPos) - getSteps(lanternPos, pos));
            return false;
        });
        return best[0];
    }

    /** Looks at one placed lantern; returning true stops the search. */
    private interface PlacedLanternVisitor {
        boolean visit(BlockPos lanternPos, IBlockState state, LanternType type);
    }

    /**
     * Visits the placed lanterns in loaded chunks within range blocks (on each
     * horizontal axis) of pos, stopping at the first visit that returns true.
     * Placed lanterns are tile entities, so only the few in nearby chunks are looked at.
     */
    private static boolean forEachPlacedLantern(World world, BlockPos pos, int range, PlacedLanternVisitor visitor) {
        for (int cx = (pos.getX() - range) >> 4; cx <= (pos.getX() + range) >> 4; cx++) {
            for (int cz = (pos.getZ() - range) >> 4; cz <= (pos.getZ() + range) >> 4; cz++) {
                Chunk chunk = world.getChunkProvider().getLoadedChunk(cx, cz);
                if (chunk == null) {
                    continue;
                }
                for (TileEntity tile : chunk.getTileEntityMap().values()) {
                    BlockPos lanternPos = tile.getPos();
                    if (Math.abs(lanternPos.getY() - pos.getY()) > range) {
                        continue;
                    }
                    IBlockState state = world.getBlockState(lanternPos);
                    Item item = Item.getItemFromBlock(state.getBlock());
                    if (item instanceof ItemLanternBase
                            && visitor.visit(lanternPos, state, ((ItemLanternBase) item).getLanternType())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** The configured darkness / underground check at a position (a player's, or a placed lantern's). */
    public static boolean meetsActivationCondition(World world, BlockPos pos) {
        LanternConfig config = WardlightsConfig.server.lanterns;
        int light = world.getLightFromNeighbors(pos);
        // Light that placed lanterns could account for on their own doesn't count, so a lantern
        // never switches its own ward off; anything brighter (torches, daylight) still does.
        boolean dark = light <= config.darknessLightLevel || light <= getPlacedLanternLight(world, pos);
        // Sky light is the raw value (not dimmed at night), so an open pit counts as surface day and night.
        boolean underground = pos.getY() < config.undergroundYLevel
                && world.getLightFor(EnumSkyBlock.SKY, pos) <= config.undergroundMaxSkyLight;
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
