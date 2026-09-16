package wardlights.tileentity;

import net.minecraft.tileentity.TileEntity;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import wardlights.animation.LanternAnimations;

public class TileEntityLanternRegular extends TileEntity implements IAnimatable {

    private final AnimationFactory factory = new AnimationFactory(this);

    @Override
    public void registerControllers(AnimationData data) {
        LanternAnimations.registerSingleIdleLoop(this, data);
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
