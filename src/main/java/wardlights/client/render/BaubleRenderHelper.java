package wardlights.client.render;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import wardlights.config.WardlightsConfig;

/**
 * Shared draw logic for the GeckoLib body baubles —
 * {@link AegisOfSacrificeBaubleRenderer} (badge + shoulder) and
 * {@link BulwarkOfValorBaubleRenderer} (badge only).
 * Invoked directly (not via {@code GeoItemRenderer}) since that class's
 * recentering translate is meant for block-item rendering, not a
 * body-attached bauble.
 *
 * <p>All parts were modeled/tuned against Blockbench's "third-person left
 * hand" preview (their {@code models/item/*.json} only define a
 * {@code thirdperson_lefthand} display block), so — rather than Baubles' own
 * {@code IRenderBauble.Helper} chest helpers, which assume a model authored
 * directly in body-relative space — this replicates the exact vanilla
 * transform chain a held item goes through for that slot: anchor at the left
 * arm's pivot ({@link net.minecraft.client.model.ModelBiped#bipedLeftArm},
 * rotation point (5,2,0) px, decompiled from the recomp MC jar), the same
 * rotate(-90,X)/rotate(180,Y)/offset {@code LayerHeldItem.renderHeldItem}
 * applies before handing off to item rendering, and then that item's own
 * {@code thirdperson_lefthand} rotation/translation/scale on top — the exact
 * transform vanilla's {@code ItemRenderer} would have applied had this gone
 * through the normal item-display pipeline instead of Baubles' direct hook.
 * Badges follow the torso (anchored at the arm's rest pivot in body space);
 * shoulder pads follow the upper arm itself, so they move like a real
 * pauldron (see {@link #applyHandAnchor(EntityPlayer, Anchor)}). A shoulder
 * model's optional {@code forearm} bone rides the forearm instead, which only
 * differs from the upper arm under Mo' Bends' elbow bend.
 *
 * <p>Both anchors go through {@code ModelRenderer#postRender} on the player's
 * live model rather than hand-rolled pivot math, which is what makes this work
 * with Mo' Bends with no dependency on it: Mo' Bends swaps
 * {@code bipedBody}/{@code bipedLeftArm} for its own {@code ModelPart}
 * subclasses (decompiled from MoBends 1.2.1), whose transforms walk the whole
 * parent chain (arm → bent torso) and keep vanilla's origins (torso frame at
 * the neck, shoulder pivot at (5,2,0) px). The arm needs one extra step to
 * avoid being carried down to the forearm (see
 * {@link #applyUpperArmTransform}). On vanilla the same call is just
 * translate(pivot) + rotate Z/Y/X.
 *
 * <p>Each render call is wrapped push/try/finally-pop: {@code pushMatrix()}
 * and {@code popMatrix()} share one global GL matrix stack with the rest of
 * the frame, so if anything between them throws and the pop is skipped, every
 * other draw call that frame (and often subsequent frames) inherits the
 * leftover transform — the entire scene visibly corrupts, not just this
 * bauble. The finally block guarantees the stack stays balanced no matter
 * what happens inside.
 *
 * <p>Badge and shoulder each need their own {@code IAnimatable}/model pair —
 * GeckoLib throws if a queued clip names a bone absent from the model
 * currently being animated, and only the shoulder geos have the bones their
 * idle clips drive.
 */
public final class BaubleRenderHelper {

    private BaubleRenderHelper() {
    }

    /**
     * Shoulder-model bone (Blockbench group) that rides Mo' Bends' forearm
     * instead of the upper arm. Keep its cubes wholly below the elbow, 4 px
     * down the arm, or they split open when the elbow bends.
     */
    private static final String FOREARM_BONE = "forearm";

    private enum Anchor {
        TORSO, UPPER_ARM, FOREARM
    }

    // ModelRenderer#postRender's scale argument, as every biped layer passes it.
    private static final float MODEL_SCALE = 0.0625f;

    // ModelBiped#bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F) (pixels),
    // converted to block units the same way ModelRenderer#postRender does
    // (rotationPoint * 0.0625F). Used relative to the torso for the badge, and
    // as the whole anchor when the player's model isn't a ModelBiped.
    private static final float ARM_PIVOT_X = 5.0f * 0.0625f;
    private static final float ARM_PIVOT_Y = 2.0f * 0.0625f;
    private static final float ARM_PIVOT_Z = 0.0f;

    // LayerHeldItem#renderHeldItem's fixed chain for the LEFT hand, after
    // ModelBiped#postRenderArm's pivot translate above:
    // rotate(-90, X); rotate(180, Y); translate((-1)/16, 0.125, -0.625).
    private static final float HAND_OFFSET_X = -1.0f / 16.0f;
    private static final float HAND_OFFSET_Y = 0.125f;
    private static final float HAND_OFFSET_Z = -0.625f;

    /**
     * Tells GeckoLib which model animates {@code partType}. An
     * {@code AnimationController} looks its model up through the registered
     * fetchers every time a clip is set, and silently skips the clip (logging
     * "Could not find suitable model ... Did you register a Model Fetcher?")
     * when none match. {@code GeoItemRenderer} only registers a fetcher for
     * {@code Item}s, and these parts aren't Items, so each renderer registers
     * its own.
     */
    public static <T extends IAnimatable> void registerModelFetcher(Class<T> partType, AnimatedGeoModel<T> model) {
        AnimationController.addModelFetcher(
                (AnimationController.ModelFetcher<T>) animatable -> partType.isInstance(animatable) ? model : null);
    }

    /** A minimal {@link IGeoRenderer} backed by the given model. */
    public static <T extends IAnimatable> IGeoRenderer<T> geoRenderer(AnimatedGeoModel<T> model) {
        return new IGeoRenderer<T>() {
            @Override
            public GeoModelProvider getGeoModelProvider() {
                return model;
            }

            @Override
            public ResourceLocation getTextureLocation(T instance) {
                return model.getTextureLocation(instance);
            }
        };
    }

    public static <T extends IAnimatable> void renderBadge(AnimatedGeoModel<T> model, IGeoRenderer<T> renderer,
            T part, EntityPlayer player, float partialTicks, float[] rotation, float[] translation, float[] scale,
            BaublePlacement placement) {
        renderPart(model, renderer, part, player, partialTicks, false, rotation, translation, scale,
                placement.resolve(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST)));
    }

    public static <T extends IAnimatable> void renderShoulder(AnimatedGeoModel<T> model, IGeoRenderer<T> renderer,
            T part, EntityPlayer player, float partialTicks, float[] rotation, float[] translation, float[] scale,
            BaublePlacement placement) {
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (isShoulderHidingArmor(chest)) {
            return;
        }
        renderPart(model, renderer, part, player, partialTicks, true, rotation, translation, scale,
                placement.resolve(chest));
    }

    /** Whether the chest armor is listed in the "Armor Hiding Bauble Shoulders" config. */
    private static boolean isShoulderHidingArmor(ItemStack chest) {
        ResourceLocation id = chest.isEmpty() ? null : chest.getItem().getRegistryName();
        if (id == null) {
            return false;
        }
        String name = id.toString();
        for (String entry : WardlightsConfig.client.shoulderHidingArmor) {
            if (BaublePlacement.matchesId(entry, name)) {
                return true;
            }
        }
        return false;
    }

    private static <T extends IAnimatable> void renderPart(AnimatedGeoModel<T> model, IGeoRenderer<T> renderer,
            T part, EntityPlayer player, float partialTicks, boolean trackArmSwing, float[] rotation,
            float[] translation, float[] scale, double[] tweak) {
        GeoModel geoModel = model.getModel(model.getModelLocation(part));
        model.setLivingAnimations(part, player.getEntityId(), newEvent(part, player, partialTicks));
        Minecraft.getMinecraft().renderEngine.bindTexture(model.getTextureLocation(part));

        GeoBone forearm = trackArmSwing ? findBone(geoModel.topLevelBones, FOREARM_BONE) : null;
        if (forearm == null) {
            renderPass(renderer, geoModel, part, player, partialTicks, trackArmSwing ? Anchor.UPPER_ARM : Anchor.TORSO,
                    rotation, translation, scale, tweak);
            return;
        }

        // Two passes over the one animated model: everything but the forearm
        // bone in the upper arm's frame, then only the forearm bone in the
        // forearm's frame. The animation pose is computed once above and
        // shared by both.
        List<BoneVisibility> saved = new ArrayList<>();
        saveVisibility(geoModel.topLevelBones, saved);
        try {
            forearm.setHidden(true, true);
            renderPass(renderer, geoModel, part, player, partialTicks, Anchor.UPPER_ARM, rotation, translation, scale,
                    tweak);

            showOnly(geoModel.topLevelBones, forearm);
            renderPass(renderer, geoModel, part, player, partialTicks, Anchor.FOREARM, rotation, translation, scale,
                    tweak);
        } finally {
            for (BoneVisibility v : saved) {
                v.restore();
            }
        }
    }

    private static <T extends IAnimatable> void renderPass(IGeoRenderer<T> renderer, GeoModel geoModel, T part,
            EntityPlayer player, float partialTicks, Anchor anchor, float[] rotation, float[] translation,
            float[] scale, double[] tweak) {
        GlStateManager.pushMatrix();
        try {
            applyHandAnchor(player, anchor);
            applyDisplayTransform(rotation, translation, scale);
            applyConfigTweak(tweak);
            renderer.render(geoModel, part, partialTicks, 1f, 1f, 1f, 1f);
        } finally {
            GlStateManager.popMatrix();
        }
    }

    /** Depth-first search for a bone by name, or null. */
    private static GeoBone findBone(List<GeoBone> bones, String name) {
        for (GeoBone bone : bones) {
            if (name.equals(bone.getName())) {
                return bone;
            }
            GeoBone found = findBone(bone.childBones, name);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * Hides everything except {@code target} and its subtree. Its ancestors
     * keep rendering their children (the only path down to {@code target})
     * but hide their own cubes; every other bone hides wholesale. Returns
     * whether {@code target} is inside {@code bones}.
     */
    private static boolean showOnly(List<GeoBone> bones, GeoBone target) {
        boolean containsTarget = false;
        for (GeoBone bone : bones) {
            if (bone == target) {
                bone.setHidden(false, false);
                containsTarget = true;
            } else if (showOnly(bone.childBones, target)) {
                bone.setHidden(true, false);
                containsTarget = true;
            } else {
                bone.setHidden(true, true);
            }
        }
        return containsTarget;
    }

    private static void saveVisibility(List<GeoBone> bones, List<BoneVisibility> out) {
        for (GeoBone bone : bones) {
            out.add(new BoneVisibility(bone));
            saveVisibility(bone.childBones, out);
        }
    }

    /**
     * A bone's hide flags from before the two-pass render. GeoModels are
     * cached and shared per model file, so the flags must go back exactly as
     * found or the next draw of this model inherits our hiding.
     */
    private static final class BoneVisibility {
        private final GeoBone bone;
        private final boolean cubesHidden;
        private final boolean childrenHidden;

        BoneVisibility(GeoBone bone) {
            this.bone = bone;
            this.cubesHidden = bone.isHidden();
            this.childrenHidden = bone.childBonesAreHiddenToo();
        }

        void restore() {
            bone.setHidden(cubesHidden, childrenHidden);
        }
    }

    /**
     * Moves to the left shoulder pivot, then applies LayerHeldItem's fixed
     * left-hand chain. {@code UPPER_ARM} follows the upper arm itself
     * (swinging, attacking, item-use poses, and Mo' Bends' bends);
     * {@code FOREARM} additionally follows Mo' Bends' elbow bend (on vanilla
     * it's identical to {@code UPPER_ARM}); {@code TORSO} anchors at the arm's
     * rest pivot in body space, so a chest badge leans with the body
     * (sneaking, attack twist, Mo' Bends torso bends) without swinging along
     * with the arm.
     */
    private static void applyHandAnchor(EntityPlayer player, Anchor anchor) {
        ModelBiped model = getBipedModel(player);
        if (model != null && anchor == Anchor.UPPER_ARM) {
            applyUpperArmTransform(model.bipedLeftArm);
        } else if (model != null && anchor == Anchor.FOREARM) {
            // Mo' Bends' full postRender ends in the forearm frame, and the
            // forearm's post offset (0,-4,-2) px cancels its elbow position
            // (0,4,2), so at rest this frame equals the upper arm's — pieces
            // modeled in the same Blockbench space line up with no correction.
            model.bipedLeftArm.postRender(MODEL_SCALE);
        } else {
            if (model != null) {
                model.bipedBody.postRender(MODEL_SCALE);
            }
            GlStateManager.translate(ARM_PIVOT_X, ARM_PIVOT_Y, ARM_PIVOT_Z);
        }
        GlStateManager.rotate(-90f, 1f, 0f, 0f);
        GlStateManager.rotate(180f, 0f, 1f, 0f);
        GlStateManager.translate(HAND_OFFSET_X, HAND_OFFSET_Y, HAND_OFFSET_Z);
    }

    /**
     * Moves into the upper arm's frame. On vanilla that's just
     * {@code postRender}. Mo' Bends' arm is a {@code ModelPartExtended} whose
     * {@code postRender} is {@code applyCharacterTransform} (parent chain +
     * own transform) followed by {@code applyPostTransform}, which descends
     * into its "extension" — the forearm — so held items land in the bent
     * hand. A pauldron wants the upper arm, so call
     * {@code applyCharacterTransform} alone, reflectively since Mo' Bends is
     * an optional runtime-only mod.
     */
    private static void applyUpperArmTransform(ModelRenderer arm) {
        Method characterTransform = getCharacterTransform(arm.getClass());
        if (characterTransform != null) {
            try {
                characterTransform.invoke(arm, MODEL_SCALE);
                return;
            } catch (ReflectiveOperationException | RuntimeException e) {
                // Fall through to postRender; worst case the pad rides the forearm.
            }
        }
        arm.postRender(MODEL_SCALE);
    }

    /** Mo' Bends' {@code IModelPart#applyCharacterTransform(float)} on this class, or null if absent. */
    private static Method getCharacterTransform(Class<?> partClass) {
        return CHARACTER_TRANSFORMS.computeIfAbsent(partClass, c -> {
            try {
                return Optional.of(c.getMethod("applyCharacterTransform", float.class));
            } catch (NoSuchMethodException e) {
                return Optional.empty();
            }
        }).orElse(null);
    }

    private static final Map<Class<?>, Optional<Method>> CHARACTER_TRANSFORMS = new HashMap<>();

    /** The player's currently-rendering biped model (vanilla or Mo' Bends-mutated), or null. */
    private static ModelBiped getBipedModel(EntityPlayer player) {
        Render<?> renderer = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(player);
        if (!(renderer instanceof RenderLivingBase)) {
            return null;
        }
        ModelBase model = ((RenderLivingBase<?>) renderer).getMainModel();
        return model instanceof ModelBiped ? (ModelBiped) model : null;
    }

    /** A model json "display.thirdperson_lefthand" block, applied in the usual T*R*S order. */
    private static void applyDisplayTransform(float[] rotation, float[] translation, float[] scale) {
        GlStateManager.translate(translation[0] / 16f, translation[1] / 16f, translation[2] / 16f);
        GlStateManager.rotate(rotation[2], 0f, 0f, 1f);
        GlStateManager.rotate(rotation[1], 0f, 1f, 0f);
        GlStateManager.rotate(rotation[0], 1f, 0f, 0f);
        GlStateManager.scale(scale[0], scale[1], scale[2]);
    }

    /**
     * Live-tunable nudge on top of the base transform: {@code offsetX, offsetY, offsetZ, rotationX,
     * rotationY, rotationZ, scale} as resolved by {@link BaublePlacement}.
     */
    private static void applyConfigTweak(double[] tweak) {
        GlStateManager.translate((float) tweak[0], (float) tweak[1], (float) tweak[2]);
        GlStateManager.rotate((float) tweak[5], 0f, 0f, 1f);
        GlStateManager.rotate((float) tweak[4], 0f, 1f, 0f);
        GlStateManager.rotate((float) tweak[3], 1f, 0f, 0f);
        GlStateManager.scale((float) tweak[6], (float) tweak[6], (float) tweak[6]);
    }

    private static <T extends IAnimatable> AnimationEvent<T> newEvent(T part, EntityPlayer player,
            float partialTicks) {
        float limbSwing = player.prevLimbSwingAmount
                + (player.limbSwing - player.prevLimbSwingAmount) * partialTicks;
        float limbSwingAmount = player.prevLimbSwingAmount
                + (player.limbSwingAmount - player.prevLimbSwingAmount) * partialTicks;
        return new AnimationEvent<>(part, limbSwing, limbSwingAmount, partialTicks, limbSwingAmount > 0.01f,
                Collections.emptyList());
    }
}
