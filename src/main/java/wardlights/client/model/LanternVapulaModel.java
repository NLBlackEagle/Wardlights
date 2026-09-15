package wardlights.client.model;

import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import wardlights.Wardlights;
import wardlights.items.ItemLanternVapula;

public class LanternVapulaModel extends AnimatedGeoModel<ItemLanternVapula> {

    @Override
    public ResourceLocation getModelLocation(ItemLanternVapula object) {
        return new ResourceLocation(Wardlights.MODID, "geo/item/lantern_vapula.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ItemLanternVapula object) {
        return new ResourceLocation(Wardlights.MODID, "textures/items/lantern_vapula.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ItemLanternVapula animatable) {
        return new ResourceLocation(Wardlights.MODID, "animations/item/lantern_vapula.animation.json");
    }
}
