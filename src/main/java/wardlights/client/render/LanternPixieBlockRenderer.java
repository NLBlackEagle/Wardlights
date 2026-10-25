package wardlights.client.render;

import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;
import wardlights.client.model.LanternPixieBlockModel;
import wardlights.lantern.tileentity.TileEntityLanternPixie;

public class LanternPixieBlockRenderer extends GeoBlockRenderer<TileEntityLanternPixie> {
    public LanternPixieBlockRenderer() {
        super(new LanternPixieBlockModel());
    }
}
