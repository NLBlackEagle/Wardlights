package wardlights.lantern.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import wardlights.lantern.tileentity.TileEntityLanternPale;

public class BlockLanternPale extends BlockLanternBase {

    public BlockLanternPale(String registryName) {
        super(registryName);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityLanternPale();
    }
}
