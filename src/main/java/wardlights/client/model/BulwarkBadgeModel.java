package wardlights.client.model;

import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import wardlights.Wardlights;
import wardlights.animation.BulwarkBadgePart;

public class BulwarkBadgeModel extends AnimatedGeoModel<BulwarkBadgePart> {

    @Override
    public ResourceLocation getModelLocation(BulwarkBadgePart object) {
        return new ResourceLocation(Wardlights.MODID, "geo/item/bulwark_of_valor_badge.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(BulwarkBadgePart object) {
        return new ResourceLocation(Wardlights.MODID, "textures/items/bulwark_of_valor_badge.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(BulwarkBadgePart animatable) {
        // Same trick as AegisBadgeModel: no badge animation file exists and no
        // clip is ever queued, so point at the shoulder's real file.
        return new ResourceLocation(Wardlights.MODID, "animations/item/bulwark_of_valor_shoulder.animation.json");
    }
}
