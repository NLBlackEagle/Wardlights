package wardlights.tileentity;

import net.minecraft.util.ITickable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import wardlights.animation.LanternAnimations;
import wardlights.handlers.ParasiteLanternHandler;
import wardlights.util.LanternUtil;

public class TileEntityLanternSrp extends TileEntityLanternBase implements IAnimatable, ITickable {

    private final AnimationFactory factory = new AnimationFactory(this);

    /** A placed Parasite Lantern clears COTH around itself, day and night. */
    @Override
    public void update() {
        if (!world.isRemote && LanternUtil.isPlacedLanternCheckTick(world)) {
            ParasiteLanternHandler.clearCoth(world, pos);
        }
    }

    @Override
    public void registerControllers(AnimationData data) {
        LanternAnimations.registerSrpControllers(this, data);
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
