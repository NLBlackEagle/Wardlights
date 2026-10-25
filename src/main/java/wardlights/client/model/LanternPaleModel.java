package wardlights.client.model;

import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import wardlights.Wardlights;
import wardlights.lantern.item.ItemLanternPale;

public class LanternPaleModel extends AnimatedGeoModel<ItemLanternPale> {

    @Override
    public ResourceLocation getModelLocation(ItemLanternPale object) {
        return new ResourceLocation(Wardlights.MODID, "geo/item/lantern_pale.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ItemLanternPale object) {
        return new ResourceLocation(Wardlights.MODID, "textures/items/lantern_pale.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ItemLanternPale animatable) {
        return new ResourceLocation(Wardlights.MODID, "animations/item/lantern_pale.animation.json");
    }
}
