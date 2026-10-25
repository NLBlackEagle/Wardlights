package wardlights.client.model;

import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import wardlights.Wardlights;
import wardlights.lantern.tileentity.TileEntityLanternPale;

public class LanternPaleBlockModel extends AnimatedGeoModel<TileEntityLanternPale> {

    @Override
    public ResourceLocation getModelLocation(TileEntityLanternPale object) {
        return new ResourceLocation(Wardlights.MODID, "geo/item/lantern_pale.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(TileEntityLanternPale object) {
        return new ResourceLocation(Wardlights.MODID, "textures/items/lantern_pale.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(TileEntityLanternPale animatable) {
        return new ResourceLocation(Wardlights.MODID, "animations/item/lantern_pale.animation.json");
    }
}
