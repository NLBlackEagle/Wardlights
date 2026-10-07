package wardlights.items;

import software.bernie.geckolib3.core.manager.AnimationData;
import wardlights.animation.LanternAnimations;
import wardlights.blocks.BlockLanternPixie;
import wardlights.lantern.LanternType;

/**
 * Animated like the Parasite lantern (GeckoLib) — the orbiting pixie bones
 * are driven by a single "idle" clip. Also placeable as a block
 * (BlockLanternPixie) via the inherited ItemBlock#onItemUse.
 */
public class ItemLanternPixie extends ItemLanternBase {

    public ItemLanternPixie(BlockLanternPixie block, String registryName) {
        super(block, registryName, LanternType.PIXIE);
    }

    @Override
    public void registerControllers(AnimationData data) {
        LanternAnimations.registerSingleIdleLoop(this, data);
    }
}
