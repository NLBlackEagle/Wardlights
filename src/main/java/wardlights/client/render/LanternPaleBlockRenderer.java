package wardlights.client.render;

import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;
import wardlights.client.model.LanternPaleBlockModel;
import wardlights.tileentity.TileEntityLanternPale;

public class LanternPaleBlockRenderer extends GeoBlockRenderer<TileEntityLanternPale> {
    public LanternPaleBlockRenderer() {
        super(new LanternPaleBlockModel());
    }
}
