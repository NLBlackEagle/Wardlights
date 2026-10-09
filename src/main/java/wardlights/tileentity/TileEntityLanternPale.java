package wardlights.tileentity;

import java.util.Collections;
import java.util.Set;

import net.minecraft.util.ITickable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import wardlights.animation.LanternAnimations;
import wardlights.handlers.PaleLanternHandler;
import wardlights.util.LanternUtil;

public class TileEntityLanternPale extends TileEntityLanternBase implements IAnimatable, ITickable {

    private final AnimationFactory factory = new AnimationFactory(this);

    /**
     * Hostile mobs (entity ids) within the radius at the last check, so each only
     * rolls for Pale Lung when it comes in. Not saved: after a reload, the ones
     * already inside count as coming in once more.
     */
    private Set<Integer> mobsInside = Collections.emptySet();

    /** While working, a placed Pale Lantern may give Pale Lung to hostile mobs that come near. */
    @Override
    public void update() {
        if (world.isRemote || !LanternUtil.isPlacedLanternCheckTick(world)) {
            return;
        }
        if (LanternUtil.meetsActivationCondition(world, pos)) {
            mobsInside = PaleLanternHandler.rollNewcomers(world, pos, mobsInside);
        } else {
            // Mobs already nearby when it starts working again count as coming in.
            mobsInside = Collections.emptySet();
        }
    }

    @Override
    public void registerControllers(AnimationData data) {
        LanternAnimations.registerSingleIdleLoop(this, data);
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
