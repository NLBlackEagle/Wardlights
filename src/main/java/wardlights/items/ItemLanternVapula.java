package wardlights.items;

import net.minecraft.item.ItemBlock;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import wardlights.animation.LanternAnimations;
import wardlights.blocks.BlockLanternVapula;

/**
 * Animated like the Parasite lantern (GeckoLib), but composited from 3
 * simultaneous clips — "idle"/"idle_2" each orbit a different floating
 * shard bone, "crystal_idle" bobs the central crystal — since the source
 * model splits the motion across 3 separate bones. See
 * {@link LanternAnimations} for the shared controller registration (also
 * used by the placed-block tile entity). Also placeable as a block
 * (BlockLanternVapula) via the inherited ItemBlock#onItemUse.
 */
public class ItemLanternVapula extends ItemBlock implements IAnimatable {

    private final AnimationFactory factory = new AnimationFactory(this);

    public ItemLanternVapula(BlockLanternVapula block, String registryName) {
        super(block);
        setRegistryName(registryName);
        setMaxStackSize(1);
    }

    @Override
    public void registerControllers(AnimationData data) {
        LanternAnimations.registerVapulaControllers(this, data);
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
