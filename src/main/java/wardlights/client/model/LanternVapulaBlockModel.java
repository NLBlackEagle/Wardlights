package wardlights.client.model;

import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import wardlights.Wardlights;
import wardlights.lantern.tileentity.TileEntityLanternVapula;

public class LanternVapulaBlockModel extends AnimatedGeoModel<TileEntityLanternVapula> {

    @Override
    public ResourceLocation getModelLocation(TileEntityLanternVapula object) {
        return new ResourceLocation(Wardlights.MODID, "geo/item/lantern_vapula.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(TileEntityLanternVapula object) {
        return new ResourceLocation(Wardlights.MODID, "textures/items/lantern_vapula.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(TileEntityLanternVapula animatable) {
        return new ResourceLocation(Wardlights.MODID, "animations/item/lantern_vapula.animation.json");
    }
}
