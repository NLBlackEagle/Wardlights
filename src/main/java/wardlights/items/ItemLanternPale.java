package wardlights.items;

import software.bernie.geckolib3.core.manager.AnimationData;
import wardlights.animation.LanternAnimations;
import wardlights.blocks.BlockLanternPale;
import wardlights.lantern.LanternType;

/**
 * Animated like the Parasite lantern (GeckoLib) — the swaying tree bone and
 * falling petals are driven by a single "idle" clip. Also placeable as a
 * block (BlockLanternPale) via the inherited ItemBlock#onItemUse.
 */
public class ItemLanternPale extends ItemLanternBase {

    public ItemLanternPale(BlockLanternPale block, String registryName) {
        super(block, registryName, LanternType.PALE);
    }

    @Override
    public void registerControllers(AnimationData data) {
        LanternAnimations.registerSingleIdleLoop(this, data);
    }
}
