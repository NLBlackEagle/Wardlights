package wardlights.client.render;

import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;
import wardlights.client.model.LanternRegularBlockModel;
import wardlights.lantern.tileentity.TileEntityLanternRegular;

public class LanternRegularBlockRenderer extends GeoBlockRenderer<TileEntityLanternRegular> {
    public LanternRegularBlockRenderer() {
        super(new LanternRegularBlockModel());
    }
}
