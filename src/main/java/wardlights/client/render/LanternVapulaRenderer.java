package wardlights.client.render;

import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;
import wardlights.client.model.LanternVapulaModel;
import wardlights.items.ItemLanternVapula;

public class LanternVapulaRenderer extends GeoItemRenderer<ItemLanternVapula> {

    public static final LanternVapulaRenderer INSTANCE = new LanternVapulaRenderer();

    public LanternVapulaRenderer() {
        super(new LanternVapulaModel());
    }
}
