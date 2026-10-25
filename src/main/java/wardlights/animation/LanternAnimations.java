package wardlights.animation;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import wardlights.config.WardlightsConfig;

/**
 * Shared GeckoLib controller-registration logic for the 5 lanterns, used by
 * both the held-item animatable and the placed-block tile-entity animatable
 * so the (sometimes intricate, e.g. SRP's) clip logic exists in one place.
 */
public final class LanternAnimations {

    private LanternAnimations() {
    }

    // Only placed blocks (tile entities) are gated — a held item is always
    // right next to the player, so a distance cutoff wouldn't do anything
    // useful there and would just be extra work every tick.
    private static boolean isTooFarToAnimate(IAnimatable animatable) {
        if (!(animatable instanceof TileEntity)) {
            return false;
        }
        int cutoff = WardlightsConfig.client.lanternAnimationDistance;
        if (cutoff <= 0) {
            return false;
        }
        TileEntity tile = (TileEntity) animatable;
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (tile.getWorld() == null || player == null) {
            return false;
        }
        double cutoffSq = (double) cutoff * cutoff;
        return player.getDistanceSq(tile.getPos()) > cutoffSq;
    }

    private static <T extends IAnimatable> PlayState loop(AnimationEvent<T> event, String clipName) {
        if (isTooFarToAnimate(event.getAnimatable())) {
            return PlayState.STOP;
        }
        event.getController().setAnimation(new AnimationBuilder().loop(clipName));
        return PlayState.CONTINUE;
    }

    /** Regular/Pixie/Pale: a single looping "idle" clip. */
    public static <T extends IAnimatable> void registerSingleIdleLoop(T animatable, AnimationData data) {
        data.addAnimationController(new AnimationController<>(animatable, "idle_controller", 0,
                event -> loop(event, "idle")));
    }

    /**
     * Vapula: 3 simultaneous clips driving different bones (2 orbiting shards
     * + the central crystal bob) — each needs its own controller since a
     * single controller only drives one clip name at a time.
     */
    public static <T extends IAnimatable> void registerVapulaControllers(T animatable, AnimationData data) {
        data.addAnimationController(new AnimationController<>(animatable, "idle_controller", 0,
                event -> loop(event, "idle")));
        data.addAnimationController(new AnimationController<>(animatable, "idle_2_controller", 0,
                event -> loop(event, "idle_2")));
        data.addAnimationController(new AnimationController<>(animatable, "crystal_idle_controller", 0,
                event -> loop(event, "crystal_idle")));
    }

    // --- SRP: randomized multi-clip idle with a rare "gnaw" one-shot ---

    private static final Random RANDOM = new Random();
    private static final String GNAW_CLIP = "idle_gnaw";
    private static final String[] LOOP_CLIPS = { "idle", "idle_twitch_one", "idle_twitch_two" };

    // Out of 1000, how often idle_gnaw gets picked instead of one of the
    // loops at a given boundary. Loops are ~2.75s, so ~109 decisions happen
    // per 5 minutes — at 10 (1%) that's roughly 1 gnaw per 5 minutes on
    // average, versus the old 10/100 (10%, ~11 per 5 minutes) that felt way
    // too frequent.
    private static final int GNAW_WEIGHT_PER_MILLE = 10;

    private static final int SRP_BLEND_TICKS = 5;

    private static String pickLoopClip() {
        return LOOP_CLIPS[RANDOM.nextInt(LOOP_CLIPS.length)];
    }

    private static String pickNextClip() {
        return RANDOM.nextInt(1000) < GNAW_WEIGHT_PER_MILLE ? GNAW_CLIP : pickLoopClip();
    }

    /**
     * Per-rendered-instance state (registerControllers runs once per unique
     * animation-data ID, so each distinct lantern stack/tile entity gets its
     * own). Timing tracks the currently-playing loop's own phase via the
     * animatable's absolute animation-tick clock, so a decision only ever
     * happens at a clean loop boundary rather than interrupting mid-cycle.
     */
    private static final class SrpLoopState {
        boolean initialized = false;
        boolean wasGnawing = false;
        double cycleStartAt = -1;
        double lastPhase = -1;
        AnimationBuilder currentBuilder;
    }

    private static <T extends IAnimatable> PlayState srpPredicate(AnimationEvent<T> event, SrpLoopState state) {
        if (isTooFarToAnimate(event.getAnimatable())) {
            return PlayState.STOP;
        }

        AnimationController<T> controller = event.getController();
        double now = event.getAnimationTick();
        Animation current = controller.getCurrentAnimation();

        if (!state.initialized) {
            state.initialized = true;
            state.cycleStartAt = now;
            state.currentBuilder = new AnimationBuilder().loop(pickLoopClip());
        } else {
            boolean isGnawing = current != null && GNAW_CLIP.equals(current.animationName);
            if (isGnawing) {
                state.wasGnawing = true;
            } else if (state.wasGnawing) {
                // Just moved from idle_gnaw's one-shot into its queued trailing
                // loop — resync phase tracking to that loop's actual start
                // instead of measuring from whenever idle_gnaw was first queued.
                state.wasGnawing = false;
                state.cycleStartAt = now;
                state.lastPhase = -1;
            } else {
                double length = current != null && current.animationLength != null ? current.animationLength : 0;
                if (length > 0) {
                    double phase = (now - state.cycleStartAt) % length;
                    boolean wrapped = state.lastPhase >= 0 && phase < state.lastPhase;
                    state.lastPhase = phase;

                    if (wrapped) {
                        state.cycleStartAt = now;
                        state.lastPhase = -1;
                        String next = pickNextClip();
                        state.currentBuilder = GNAW_CLIP.equals(next)
                                ? new AnimationBuilder().playOnce(GNAW_CLIP).loop(pickLoopClip())
                                : new AnimationBuilder().loop(next);
                    }
                }
            }
        }

        // Re-asserted every tick (like the other lanterns' single-loop
        // controllers) rather than only at decision points: AnimationBuilder
        // content-equality makes a repeat call a no-op, but calling it every
        // tick means a single missed/late resolution on any one frame (e.g.
        // right as a placed block's tile entity spins up) self-heals on the
        // very next tick instead of leaving the controller permanently stuck
        // with an empty animation queue.
        controller.setAnimation(state.currentBuilder);
        return PlayState.CONTINUE;
    }

    public static <T extends IAnimatable> void registerSrpControllers(T animatable, AnimationData data) {
        data.setResetSpeedInTicks(SRP_BLEND_TICKS);
        SrpLoopState loopState = new SrpLoopState();
        data.addAnimationController(new AnimationController<>(animatable, "idle_controller", SRP_BLEND_TICKS,
                event -> srpPredicate(event, loopState)));
    }
}
