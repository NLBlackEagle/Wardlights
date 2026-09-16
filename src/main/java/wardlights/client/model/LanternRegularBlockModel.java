package wardlights.client.model;

import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import wardlights.Wardlights;
import wardlights.tileentity.TileEntityLanternRegular;

public class LanternRegularBlockModel extends AnimatedGeoModel<TileEntityLanternRegular> {

    @Override
    public ResourceLocation getModelLocation(TileEntityLanternRegular object) {
        return new ResourceLocation(Wardlights.MODID, "geo/item/lantern_regular.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(TileEntityLanternRegular object) {
        return new ResourceLocation(Wardlights.MODID, "textures/items/lantern_regular.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(TileEntityLanternRegular animatable) {
        return new ResourceLocation(Wardlights.MODID, "animations/item/lantern_regular.animation.json");
    }
}
