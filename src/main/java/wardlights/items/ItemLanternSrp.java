package wardlights.items;

import net.minecraft.item.ItemBlock;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import wardlights.animation.LanternAnimations;
import wardlights.blocks.BlockLanternSrp;

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
public class ItemLanternSrp extends ItemBlock implements IAnimatable {

    private final AnimationFactory factory = new AnimationFactory(this);

    public ItemLanternSrp(BlockLanternSrp block, String registryName) {
        super(block);
        setRegistryName(registryName);
        setMaxStackSize(1);
    }

    @Override
    public void registerControllers(AnimationData data) {
        LanternAnimations.registerSrpControllers(this, data);
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
