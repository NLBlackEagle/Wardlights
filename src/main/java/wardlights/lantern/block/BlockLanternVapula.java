package wardlights.lantern.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import wardlights.lantern.tileentity.TileEntityLanternVapula;

public class BlockLanternVapula extends BlockLanternBase {

    public BlockLanternVapula(String registryName) {
        super(registryName);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityLanternVapula();
    }
}
