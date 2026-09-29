package wardlights.animation;

import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

/**
 * The Bulwark of Valor bauble's badge model. Like {@link AegisBadgePart},
 * it has no idle animation, so no controllers are registered.
 */
public final class BulwarkBadgePart implements IAnimatable {

    public static final BulwarkBadgePart INSTANCE = new BulwarkBadgePart();

    private final AnimationFactory factory = new AnimationFactory(this);

    private BulwarkBadgePart() {
    }

    @Override
    public void registerControllers(AnimationData data) {
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
