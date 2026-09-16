package wardlights.items;

import net.minecraft.item.ItemBlock;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import wardlights.animation.LanternAnimations;
import wardlights.blocks.BlockLanternPixie;

/**
 * Animated like the Parasite lantern (GeckoLib) — the orbiting pixie bones
 * are driven by a single "idle" clip. Also placeable as a block
 * (BlockLanternPixie) via the inherited ItemBlock#onItemUse.
 */
public class ItemLanternPixie extends ItemBlock implements IAnimatable {

    private final AnimationFactory factory = new AnimationFactory(this);

    public ItemLanternPixie(BlockLanternPixie block, String registryName) {
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
