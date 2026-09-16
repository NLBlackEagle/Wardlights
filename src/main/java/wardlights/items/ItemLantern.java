package wardlights.items;

import net.minecraft.item.ItemBlock;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import wardlights.animation.LanternAnimations;
import wardlights.blocks.BlockLanternRegular;

/**
 * IBauble behaviour and LanternType data come later. Also placeable as a
 * block (BlockLanternRegular) via the inherited ItemBlock#onItemUse.
 */
public class ItemLantern extends ItemBlock implements IAnimatable {

    private final AnimationFactory factory = new AnimationFactory(this);

    public ItemLantern(BlockLanternRegular block, String registryName) {
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
