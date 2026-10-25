package wardlights.lantern.item;

import software.bernie.geckolib3.core.manager.AnimationData;
import wardlights.animation.LanternAnimations;
import wardlights.lantern.LanternType;
import wardlights.lantern.block.BlockLanternRegular;

/**
 * Lantern 1 / Regular. Placeable as a block (BlockLanternRegular), see
 * {@link ItemLanternBase}.
 */
public class ItemLantern extends ItemLanternBase {

    public ItemLantern(BlockLanternRegular block, String registryName) {
        super(block, registryName, LanternType.REGULAR);
    }

    @Override
    public void registerControllers(AnimationData data) {
        LanternAnimations.registerSingleIdleLoop(this, data);
    }
}
