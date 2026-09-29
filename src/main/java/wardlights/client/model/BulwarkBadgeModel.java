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
        // GeckoLib requires an animation file even though the badge never
        // plays a clip, so this one is intentionally empty.
        return new ResourceLocation(Wardlights.MODID, "animations/item/bulwark_of_valor_badge.animation.json");
    }
}
