package wardlights.client.model;

import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import wardlights.Wardlights;
import wardlights.lantern.tileentity.TileEntityLanternPixie;

public class LanternPixieBlockModel extends AnimatedGeoModel<TileEntityLanternPixie> {

    @Override
    public ResourceLocation getModelLocation(TileEntityLanternPixie object) {
        return new ResourceLocation(Wardlights.MODID, "geo/item/lantern_pixie.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(TileEntityLanternPixie object) {
        return new ResourceLocation(Wardlights.MODID, "textures/items/lantern_pixie.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(TileEntityLanternPixie animatable) {
        return new ResourceLocation(Wardlights.MODID, "animations/item/lantern_pixie.animation.json");
    }
}
