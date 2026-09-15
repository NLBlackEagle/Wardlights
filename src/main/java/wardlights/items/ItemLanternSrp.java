package wardlights.items;

import java.util.Random;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

/**
 * Animated variant of the lantern (Lantern 5 / Parasite Infused) — the other
 * 4 lanterns are static vanilla-JSON items, this one renders via GeckoLib.
 * "idle", "idle_twitch_one" and "idle_twitch_two" are all full standalone
 * loops of the same length; at every loop boundary one is picked at random
 * to play next. "idle_gnaw" is the rare exception — it's a one-shot that
 * shares idle's jaw bones, so it's queued together with a freshly-picked
 * loop for afterward rather than treated as its own loop choice.
 */
public class ItemLanternSrp extends Item implements IAnimatable {

    private static final Random RANDOM = new Random();

    private static final String GNAW_CLIP = "idle_gnaw";
    private static final String[] LOOP_CLIPS = { "idle", "idle_twitch_one", "idle_twitch_two" };

    // Out of 1000, how often idle_gnaw gets picked instead of one of the
    // loops at a given boundary. Loops are ~2.75s, so ~109 decisions happen
    // per 5 minutes — at 10 (1%) that's roughly 1 gnaw per 5 minutes on
    // average, versus the old 10/100 (10%, ~11 per 5 minutes) that felt way
    // too frequent.
    private static final int GNAW_WEIGHT_PER_MILLE = 10;

    private final AnimationFactory factory = new AnimationFactory(this);

    public ItemLanternSrp(String registryName) {
        setRegistryName(registryName);
        setTranslationKey(registryName);
        setCreativeTab(CreativeTabs.MISC);
        setMaxStackSize(1);
    }

    private static String pickLoopClip() {
        return LOOP_CLIPS[RANDOM.nextInt(LOOP_CLIPS.length)];
    }

    private static String pickNextClip() {
        return RANDOM.nextInt(1000) < GNAW_WEIGHT_PER_MILLE ? GNAW_CLIP : pickLoopClip();
    }

    /**
     * Per-rendered-instance state (registerControllers runs once per unique
     * animation-data ID, so each distinct lantern stack/entity gets its own).
     * Timing tracks the currently-playing loop's own phase via the
     * animatable's absolute animation-tick clock, so a decision only ever
     * happens at a clean loop boundary rather than interrupting mid-cycle.
     */
    private static final class LoopState {
        boolean initialized = false;
        boolean wasGnawing = false;
        double cycleStartAt = -1;
        double lastPhase = -1;
    }

    private PlayState idlePredicate(AnimationEvent<ItemLanternSrp> event, LoopState state) {
        AnimationController<ItemLanternSrp> controller = event.getController();
        double now = event.getAnimationTick();
        Animation current = controller.getCurrentAnimation();

        if (!state.initialized) {
            state.initialized = true;
            state.cycleStartAt = now;
            controller.setAnimation(new AnimationBuilder().loop(pickLoopClip()));
            return PlayState.CONTINUE;
        }

        boolean isGnawing = current != null && GNAW_CLIP.equals(current.animationName);
        if (isGnawing) {
            state.wasGnawing = true;
            return PlayState.CONTINUE;
        }
        if (state.wasGnawing) {
            // Just moved from idle_gnaw's one-shot into its queued trailing
            // loop — resync phase tracking to that loop's actual start
            // instead of measuring from whenever idle_gnaw was first queued.
            state.wasGnawing = false;
            state.cycleStartAt = now;
            state.lastPhase = -1;
            return PlayState.CONTINUE;
        }

        double length = current != null && current.animationLength != null ? current.animationLength : 0;
        if (length > 0) {
            double phase = (now - state.cycleStartAt) % length;
            boolean wrapped = state.lastPhase >= 0 && phase < state.lastPhase;
            state.lastPhase = phase;

            if (wrapped) {
                state.cycleStartAt = now;
                state.lastPhase = -1;
                String next = pickNextClip();
                if (GNAW_CLIP.equals(next)) {
                    controller.setAnimation(new AnimationBuilder().playOnce(GNAW_CLIP).loop(pickLoopClip()));
                } else {
                    // If this picks the SAME loop that's already playing, the
                    // builder content is unchanged and setAnimation is a
                    // no-op — the loop just keeps going with no interruption,
                    // which is exactly what we want.
                    controller.setAnimation(new AnimationBuilder().loop(next));
                }
            }
        }
        return PlayState.CONTINUE;
    }

    // Ticks to blend: bones the incoming clip animates blend via transition
    // ticks below; bones it *doesn't* touch (e.g. bone8, only in
    // idle_twitch_one) ease back to rest via reset speed. Both default to
    // near-0 in GeckoLib, which causes a pop/snap between clips otherwise.
    private static final int BLEND_TICKS = 5;

    @Override
    public void registerControllers(AnimationData data) {
        data.setResetSpeedInTicks(BLEND_TICKS);
        LoopState loopState = new LoopState();
        data.addAnimationController(new AnimationController<>(this, "idle_controller", BLEND_TICKS,
                event -> idlePredicate(event, loopState)));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
