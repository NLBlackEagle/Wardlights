package wardlights.animation;

import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

/**
 * The badge half of the Aegis of Sacrifice bauble. No clip is ever queued —
 * the badge has no idle animation, and registering zero controllers here
 * keeps it that way (GeckoLib only throws when a queued clip names a bone
 * missing from the model, so an empty controller set is always safe).
 */
public final class AegisBadgePart implements IAnimatable {

    public static final AegisBadgePart INSTANCE = new AegisBadgePart();

    private final AnimationFactory factory = new AnimationFactory(this);

    private AegisBadgePart() {
    }

    @Override
    public void registerControllers(AnimationData data) {
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
