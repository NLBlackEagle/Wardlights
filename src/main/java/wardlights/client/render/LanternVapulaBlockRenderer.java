package wardlights.client.render;

import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;
import wardlights.client.model.LanternVapulaBlockModel;
import wardlights.lantern.tileentity.TileEntityLanternVapula;

public class LanternVapulaBlockRenderer extends GeoBlockRenderer<TileEntityLanternVapula> {
    public LanternVapulaBlockRenderer() {
        super(new LanternVapulaBlockModel());
    }
}
