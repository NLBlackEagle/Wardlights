package wardlights.blocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import wardlights.tileentity.TileEntityLanternSrp;

public class BlockLanternSrp extends BlockLanternBase {

    public BlockLanternSrp(String registryName) {
        super(registryName);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityLanternSrp();
    }
}
