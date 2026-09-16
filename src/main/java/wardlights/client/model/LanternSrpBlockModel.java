package wardlights.client.model;

import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import wardlights.Wardlights;
import wardlights.tileentity.TileEntityLanternSrp;

public class LanternSrpBlockModel extends AnimatedGeoModel<TileEntityLanternSrp> {

    @Override
    public ResourceLocation getModelLocation(TileEntityLanternSrp object) {
        return new ResourceLocation(Wardlights.MODID, "geo/item/lantern_srp.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(TileEntityLanternSrp object) {
        return new ResourceLocation(Wardlights.MODID, "textures/items/lantern_srp.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(TileEntityLanternSrp animatable) {
        return new ResourceLocation(Wardlights.MODID, "animations/item/lantern_srp.animation.json");
    }
}
