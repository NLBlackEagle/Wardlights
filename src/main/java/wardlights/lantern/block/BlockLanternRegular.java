package wardlights.lantern.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import wardlights.lantern.tileentity.TileEntityLanternRegular;

public class BlockLanternRegular extends BlockLanternBase {

    public BlockLanternRegular(String registryName) {
        super(registryName);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityLanternRegular();
    }
}
