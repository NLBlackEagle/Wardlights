package wardlights.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

/**
 * Animated like the Parasite lantern (GeckoLib), but composited from 3
 * simultaneous clips — "idle"/"idle_2" each orbit a different floating
 * shard bone, "crystal_idle" bobs the central crystal — since the source
 * model splits the motion across 3 separate bones. Each needs its own
 * AnimationController; a single controller can only drive one clip name at
 * a time, but since they target different bones/channels the 3 controllers
 * don't conflict and GeckoLib composites them together automatically.
 */
public class ItemLanternVapula extends Item implements IAnimatable {

    private final AnimationFactory factory = new AnimationFactory(this);

    public ItemLanternVapula(String registryName) {
        setRegistryName(registryName);
        setTranslationKey(registryName);
        setCreativeTab(CreativeTabs.MISC);
        setMaxStackSize(1);
    }

    private PlayState loop(AnimationEvent<ItemLanternVapula> event, String clipName) {
        event.getController().setAnimation(new AnimationBuilder().loop(clipName));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "idle_controller", 0,
                event -> loop(event, "idle")));
        data.addAnimationController(new AnimationController<>(this, "idle_2_controller", 0,
                event -> loop(event, "idle_2")));
        data.addAnimationController(new AnimationController<>(this, "crystal_idle_controller", 0,
                event -> loop(event, "crystal_idle")));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
