package wardlights.blocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import wardlights.tileentity.TileEntityLanternPale;

public class BlockLanternPale extends BlockLanternBase {

    public BlockLanternPale(String registryName) {
        super(registryName);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityLanternPale();
    }
}
