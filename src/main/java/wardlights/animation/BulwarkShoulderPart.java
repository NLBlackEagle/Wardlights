package wardlights.animation;

import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

/**
 * Not a registered Item — the shoulder half of the Bulwark of Valor bauble
 * carries its own {@link IAnimatable} state (idle "Parasitic" pulse) so its
 * clip never gets applied against the badge's geo file, which doesn't have
 * those bones. See {@link wardlights.client.render.BulwarkOfValorBaubleRenderer}.
 */
public final class BulwarkShoulderPart implements IAnimatable {

    public static final BulwarkShoulderPart INSTANCE = new BulwarkShoulderPart();

    private final AnimationFactory factory = new AnimationFactory(this);

    private BulwarkShoulderPart() {
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
