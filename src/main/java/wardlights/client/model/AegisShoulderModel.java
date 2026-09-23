package wardlights.client.model;

import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import wardlights.Wardlights;
import wardlights.animation.AegisShoulderPart;

public class AegisShoulderModel extends AnimatedGeoModel<AegisShoulderPart> {

    @Override
    public ResourceLocation getModelLocation(AegisShoulderPart object) {
        return new ResourceLocation(Wardlights.MODID, "geo/item/aegis_of_sacrifice_shoulder.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(AegisShoulderPart object) {
        return new ResourceLocation(Wardlights.MODID, "textures/items/aegis_of_sacrifice_shoulder.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(AegisShoulderPart animatable) {
        return new ResourceLocation(Wardlights.MODID, "animations/item/aegis_of_sacrifice_shoulder.animation.json");
    }
}
