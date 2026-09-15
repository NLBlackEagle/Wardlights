package wardlights.client.model;

import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import wardlights.Wardlights;
import wardlights.items.ItemLanternPixie;

public class LanternPixieModel extends AnimatedGeoModel<ItemLanternPixie> {

    @Override
    public ResourceLocation getModelLocation(ItemLanternPixie object) {
        return new ResourceLocation(Wardlights.MODID, "geo/item/lantern_pixie.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ItemLanternPixie object) {
        return new ResourceLocation(Wardlights.MODID, "textures/items/lantern_pixie.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ItemLanternPixie animatable) {
        return new ResourceLocation(Wardlights.MODID, "animations/item/lantern_pixie.animation.json");
    }
}
