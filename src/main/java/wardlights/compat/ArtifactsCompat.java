package wardlights.compat;

import java.util.Arrays;

import artifacts.common.ModConfig;
import artifacts.common.entity.EntityMimic;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import wardlights.config.WardlightsConfig;
import wardlights.config.WardlightsConfig.ServerConfig.LanternConfig;
import wardlights.lantern.LanternType;
import wardlights.lantern.LanternUtil;

/**
 * Pixie Lantern: while its carrier is deep in a cave (below "Pixie Lantern
 * Mimic Max Y", with no sky light where they stand), an unlooted chest they open
 * or break gets an extra roll to be an Artifacts mimic.
 *
 * <p>Works like Artifacts' own unlooted-chest mimic and uses its dimension
 * list. Artifacts handles these events at LOW priority, so this extra roll
 * comes first; on a miss the chest is left untouched and Artifacts still makes
 * its own roll.
 *
 * <p>Only registered on the event bus when Artifacts is loaded (see
 * {@link wardlights.Wardlights#preInit}), since this class references
 * Artifacts' classes and would crash on class load without it.
 */
public class ArtifactsCompat {

    public static final String MODID = "artifacts";

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        // A chest with a solid block on top doesn't open, so it can't reveal a mimic either.
        if (event.getUseBlock() != Event.Result.DENY && !world.isRemote
                && !world.getBlockState(pos.up()).doesSideBlockChestOpening(world, pos.up(), EnumFacing.DOWN)
                && tryMimic(world, pos, event.getEntityPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!event.getWorld().isRemote && tryMimic(event.getWorld(), event.getPos(), event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    /** Below maxY with no sky light at all where the player stands: no opening to the sky, not even a ravine's. */
    private static boolean isDeepInCave(EntityPlayer player, int maxY) {
        BlockPos pos = new BlockPos(player);
        return pos.getY() < maxY && player.world.getLightFor(EnumSkyBlock.SKY, pos) == 0;
    }

    /** Rolls the Pixie Lantern's mimic chance on this chest; true when it became a mimic. */
    private static boolean tryMimic(World world, BlockPos pos, EntityPlayer player) {
        LanternConfig config = WardlightsConfig.server.lanterns;
        if (player == null || player.isSpectator() || config.pixieMimicChance <= 0
                || !isDeepInCave(player, config.pixieMimicMaxY)
                || !LanternUtil.hasLantern(player, LanternType.PIXIE)
                || !Arrays.asList(ModConfig.general.unlootedChestDimensions).contains(world.provider.getDimension())) {
            return false;
        }
        TileEntity tile = world.getTileEntity(pos);
        // A chest still holding its loot table has never been opened.
        if (!(tile instanceof TileEntityChest) || !(world.getBlockState(pos).getBlock() instanceof BlockChest)
                || ((TileEntityChest) tile).getLootTable() == null
                || world.rand.nextDouble() >= config.pixieMimicChance) {
            return false;
        }
        // Same as an Artifacts mimic: the loot spills out where the chest stood.
        ((TileEntityChest) tile).fillWithLoot(player);
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        EntityMimic mimic = new EntityMimic(world);
        mimic.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        mimic.enablePersistence();
        mimic.setAwakeWithTarget(player);
        world.spawnEntity(mimic);
        return true;
    }
}
