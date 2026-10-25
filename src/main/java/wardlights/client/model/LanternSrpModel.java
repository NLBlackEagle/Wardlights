package wardlights.client.model;

import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import wardlights.Wardlights;
import wardlights.lantern.item.ItemLanternSrp;

public class LanternSrpModel extends AnimatedGeoModel<ItemLanternSrp> {

    @Override
    public ResourceLocation getModelLocation(ItemLanternSrp object) {
        return new ResourceLocation(Wardlights.MODID, "geo/item/lantern_srp.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ItemLanternSrp object) {
        return new ResourceLocation(Wardlights.MODID, "textures/items/lantern_srp.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ItemLanternSrp animatable) {
        return new ResourceLocation(Wardlights.MODID, "animations/item/lantern_srp.animation.json");
    }
}
