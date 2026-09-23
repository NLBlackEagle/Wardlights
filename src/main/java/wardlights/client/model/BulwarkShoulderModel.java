package wardlights.client.model;

import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import wardlights.Wardlights;
import wardlights.animation.BulwarkShoulderPart;

public class BulwarkShoulderModel extends AnimatedGeoModel<BulwarkShoulderPart> {

    @Override
    public ResourceLocation getModelLocation(BulwarkShoulderPart object) {
        return new ResourceLocation(Wardlights.MODID, "geo/item/bulwark_of_valor_shoulder.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(BulwarkShoulderPart object) {
        return new ResourceLocation(Wardlights.MODID, "textures/items/bulwark_of_valor_shoulder.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(BulwarkShoulderPart animatable) {
        return new ResourceLocation(Wardlights.MODID, "animations/item/bulwark_of_valor_shoulder.animation.json");
    }
}
