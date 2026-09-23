package wardlights.client.model;

import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import wardlights.Wardlights;
import wardlights.animation.AegisBadgePart;

public class AegisBadgeModel extends AnimatedGeoModel<AegisBadgePart> {

    @Override
    public ResourceLocation getModelLocation(AegisBadgePart object) {
        return new ResourceLocation(Wardlights.MODID, "geo/item/aegis_of_sacrifice_badge.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(AegisBadgePart object) {
        return new ResourceLocation(Wardlights.MODID, "textures/items/aegis_of_sacrifice_badge.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(AegisBadgePart animatable) {
        // The badge has no animation file of its own and registers no controllers,
        // so no clip is ever looked up by name here. Pointing at the shoulder's
        // (real, existing) animation file avoids a missing-resource load instead
        // of inventing a badge animation file that doesn't exist.
        return new ResourceLocation(Wardlights.MODID, "animations/item/aegis_of_sacrifice_shoulder.animation.json");
    }
}
