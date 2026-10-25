package wardlights.client.render;

import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;
import wardlights.client.model.LanternSrpBlockModel;
import wardlights.lantern.tileentity.TileEntityLanternSrp;

public class LanternSrpBlockRenderer extends GeoBlockRenderer<TileEntityLanternSrp> {
    public LanternSrpBlockRenderer() {
        super(new LanternSrpBlockModel());
    }
}
