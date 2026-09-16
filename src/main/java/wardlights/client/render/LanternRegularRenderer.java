package wardlights.client.render;

import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;
import wardlights.client.model.LanternRegularModel;
import wardlights.items.ItemLantern;

public class LanternRegularRenderer extends GeoItemRenderer<ItemLantern> {

    public static final LanternRegularRenderer INSTANCE = new LanternRegularRenderer();

    public LanternRegularRenderer() {
        super(new LanternRegularModel());
    }
}
