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
 * Animated like the Parasite lantern (GeckoLib) — the swaying tree bone and
 * falling petals are driven by a single "idle" clip.
 */
public class ItemLanternPale extends Item implements IAnimatable {

    private final AnimationFactory factory = new AnimationFactory(this);

    public ItemLanternPale(String registryName) {
        setRegistryName(registryName);
        setTranslationKey(registryName);
        setCreativeTab(CreativeTabs.MISC);
        setMaxStackSize(1);
    }

    private PlayState idlePredicate(AnimationEvent<ItemLanternPale> event) {
        event.getController().setAnimation(new AnimationBuilder().loop("idle"));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "idle_controller", 0, this::idlePredicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
