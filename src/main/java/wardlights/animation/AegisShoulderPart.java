package wardlights.animation;

import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

/**
 * Not a registered Item — the shoulder half of the Aegis of Sacrifice bauble
 * carries its own {@link IAnimatable} state (idle "gore" pulse) so its clip
 * never gets applied against the badge's geo file, which doesn't have those
 * bones. See {@link wardlights.client.render.AegisOfSacrificeBaubleRenderer}.
 */
public final class AegisShoulderPart implements IAnimatable {

    public static final AegisShoulderPart INSTANCE = new AegisShoulderPart();

    private final AnimationFactory factory = new AnimationFactory(this);

    private AegisShoulderPart() {
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
