package wardlights.client.render;

import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;
import wardlights.client.model.LanternPixieModel;
import wardlights.items.ItemLanternPixie;

public class LanternPixieRenderer extends GeoItemRenderer<ItemLanternPixie> {

    public static final LanternPixieRenderer INSTANCE = new LanternPixieRenderer();

    public LanternPixieRenderer() {
        super(new LanternPixieModel());
    }
}
