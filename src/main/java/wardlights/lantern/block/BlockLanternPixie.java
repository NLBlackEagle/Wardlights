package wardlights.lantern.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import wardlights.lantern.tileentity.TileEntityLanternPixie;

public class BlockLanternPixie extends BlockLanternBase {

    public BlockLanternPixie(String registryName) {
        super(registryName);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityLanternPixie();
    }
}
