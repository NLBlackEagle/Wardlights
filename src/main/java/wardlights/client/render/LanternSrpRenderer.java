package wardlights.client.render;

import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;
import wardlights.client.model.LanternSrpModel;
import wardlights.items.ItemLanternSrp;

public class LanternSrpRenderer extends GeoItemRenderer<ItemLanternSrp> {

    public static final LanternSrpRenderer INSTANCE = new LanternSrpRenderer();

    public LanternSrpRenderer() {
        super(new LanternSrpModel());
    }
}
