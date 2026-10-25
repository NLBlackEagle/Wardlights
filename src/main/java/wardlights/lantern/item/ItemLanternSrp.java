package wardlights.lantern.item;

import software.bernie.geckolib3.core.manager.AnimationData;
import wardlights.animation.LanternAnimations;
import wardlights.lantern.LanternType;
import wardlights.lantern.block.BlockLanternSrp;

/**
 * Animated variant of the lantern (Lantern 5 / Parasite Infused) — renders
 * via GeckoLib. "idle", "idle_twitch_one" and "idle_twitch_two" are all full
 * standalone loops of the same length; at every loop boundary one is picked
 * at random to play next. "idle_gnaw" is the rare exception — it's a
 * one-shot that shares idle's jaw bones, so it's queued together with a
 * freshly-picked loop for afterward rather than treated as its own loop
 * choice. See {@link LanternAnimations} for the shared clip-picking logic
 * (also used by the placed-block tile entity). Also placeable as a block
 * (BlockLanternSrp) via the inherited ItemBlock#onItemUse.
 */
public class ItemLanternSrp extends ItemLanternBase {

    public ItemLanternSrp(BlockLanternSrp block, String registryName) {
        super(block, registryName, LanternType.PARASITE);
    }

    @Override
    public void registerControllers(AnimationData data) {
        LanternAnimations.registerSrpControllers(this, data);
    }
}
