package wardlights.client.model;

import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import wardlights.Wardlights;
import wardlights.items.ItemLantern;

public class LanternRegularModel extends AnimatedGeoModel<ItemLantern> {

    @Override
    public ResourceLocation getModelLocation(ItemLantern object) {
        return new ResourceLocation(Wardlights.MODID, "geo/item/lantern_regular.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ItemLantern object) {
        return new ResourceLocation(Wardlights.MODID, "textures/items/lantern_regular.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ItemLantern animatable) {
        return new ResourceLocation(Wardlights.MODID, "animations/item/lantern_regular.animation.json");
    }
}
