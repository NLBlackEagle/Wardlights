package wardlights.blocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import wardlights.tileentity.TileEntityLanternPixie;

public class BlockLanternPixie extends BlockLanternBase {

    public BlockLanternPixie(String registryName) {
        super(registryName);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityLanternPixie();
    }
}
