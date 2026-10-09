package wardlights.blocks;

import javax.annotation.Nullable;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import wardlights.tileentity.TileEntityLanternBase;
import wardlights.util.LanternUtil;

/**
 * A placed lantern. The five lanterns only differ in their tile entity.
 *
 * <p>Placing keeps the item's NBT in the tile entity (see {@link TileEntityLanternBase})
 * and every drop puts it back, so a lantern keeps its Quality Tools quality.
 * Drops are worked out while the tile entity still exists: breaking by hand
 * delays removing the block until after the drops (Forge's usual pattern), and
 * explosions drop before they remove.
 */
public abstract class BlockLanternBase extends BlockHorizontal implements ITileEntityProvider {

    protected BlockLanternBase(String registryName) {
        super(Material.CIRCUITS);
        setRegistryName(registryName);
        setTranslationKey(registryName);
        setCreativeTab(CreativeTabs.MISC);
        setHardness(0.5F);
        setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Nullable
    @Override
    public abstract TileEntity createNewTileEntity(World world, int meta);

    /** Brightness comes from the "Lantern Light Level" config. */
    @SuppressWarnings("deprecation")
    @Override
    public int getLightValue(IBlockState state) {
        return LanternUtil.getLanternLightLevel();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityLanternBase && stack.hasTagCompound()) {
            ((TileEntityLanternBase) tile).setItemTag(stack.getTagCompound());
        }
    }

    /** When harvesting, the block stays until harvestBlock has worked out the drops from the tile entity. */
    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
            boolean willHarvest) {
        return willHarvest || super.removedByPlayer(state, world, pos, player, false);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state,
            @Nullable TileEntity tile, ItemStack tool) {
        super.harvestBlock(world, player, pos, state, tile, tool);
        world.setBlockToAir(pos);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
            int fortune) {
        drops.add(getLanternStack(world, pos));
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos,
            EntityPlayer player) {
        return getLanternStack(world, pos);
    }

    /** The lantern item, with the NBT it was placed with. */
    private ItemStack getLanternStack(IBlockAccess world, BlockPos pos) {
        ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
        TileEntity tile = world.getTileEntity(pos);
        NBTTagCompound tag = tile instanceof TileEntityLanternBase ? ((TileEntityLanternBase) tile).getItemTag() : null;
        if (tag != null) {
            stack.setTagCompound(tag);
        }
        return stack;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { FACING });
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
            float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
}
