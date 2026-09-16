package wardlights.items;

import net.minecraft.item.ItemBlock;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import wardlights.animation.LanternAnimations;
import wardlights.blocks.BlockLanternPale;

/**
 * Animated like the Parasite lantern (GeckoLib) — the swaying tree bone and
 * falling petals are driven by a single "idle" clip. Also placeable as a
 * block (BlockLanternPale) via the inherited ItemBlock#onItemUse.
 */
public class ItemLanternPale extends ItemBlock implements IAnimatable {

    private final AnimationFactory factory = new AnimationFactory(this);

    public ItemLanternPale(BlockLanternPale block, String registryName) {
        super(block);
        setRegistryName(registryName);
        setMaxStackSize(1);
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
