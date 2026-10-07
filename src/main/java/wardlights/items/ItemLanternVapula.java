package wardlights.items;

import software.bernie.geckolib3.core.manager.AnimationData;
import wardlights.animation.LanternAnimations;
import wardlights.blocks.BlockLanternVapula;
import wardlights.lantern.LanternType;

/**
 * Animated like the Parasite lantern (GeckoLib), but composited from 3
 * simultaneous clips — "idle"/"idle_2" each orbit a different floating
 * shard bone, "crystal_idle" bobs the central crystal — since the source
 * model splits the motion across 3 separate bones. See
 * {@link LanternAnimations} for the shared controller registration (also
 * used by the placed-block tile entity). Also placeable as a block
 * (BlockLanternVapula) via the inherited ItemBlock#onItemUse.
 */
public class ItemLanternVapula extends ItemLanternBase {

    public ItemLanternVapula(BlockLanternVapula block, String registryName) {
        super(block, registryName, LanternType.VAPULA);
    }

    @Override
    public void registerControllers(AnimationData data) {
        LanternAnimations.registerVapulaControllers(this, data);
    }
}
