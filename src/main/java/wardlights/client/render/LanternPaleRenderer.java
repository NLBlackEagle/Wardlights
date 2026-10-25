package wardlights.client.render;

import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;
import wardlights.client.model.LanternPaleModel;
import wardlights.lantern.item.ItemLanternPale;

public class LanternPaleRenderer extends GeoItemRenderer<ItemLanternPale> {

    public static final LanternPaleRenderer INSTANCE = new LanternPaleRenderer();

    public LanternPaleRenderer() {
        super(new LanternPaleModel());
    }
}
