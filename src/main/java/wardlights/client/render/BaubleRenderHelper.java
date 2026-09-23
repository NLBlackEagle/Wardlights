package wardlights.client.render;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
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
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import wardlights.handlers.ForgeConfigHandler.ClientConfig.BaublePlacementConfig;

/**
 * Shared draw logic for the two-part (badge + shoulder) GeckoLib baubles —
 * {@link AegisOfSacrificeBaubleRenderer} and {@link BulwarkOfValorBaubleRenderer}.
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
 * pauldron (see {@link #applyHandAnchor(EntityPlayer, boolean)}).
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
            BaublePlacementConfig placement) {
        renderPart(model, renderer, part, player, partialTicks, false, rotation, translation, scale,
                placement.badgeOffsetX, placement.badgeOffsetY, placement.badgeOffsetZ,
                placement.badgeRotationX, placement.badgeRotationY, placement.badgeRotationZ,
                placement.badgeScale);
    }

    public static <T extends IAnimatable> void renderShoulder(AnimatedGeoModel<T> model, IGeoRenderer<T> renderer,
            T part, EntityPlayer player, float partialTicks, float[] rotation, float[] translation, float[] scale,
            BaublePlacementConfig placement) {
        renderPart(model, renderer, part, player, partialTicks, true, rotation, translation, scale,
                placement.shoulderOffsetX, placement.shoulderOffsetY, placement.shoulderOffsetZ,
                placement.shoulderRotationX, placement.shoulderRotationY, placement.shoulderRotationZ,
                placement.shoulderScale);
    }

    private static <T extends IAnimatable> void renderPart(AnimatedGeoModel<T> model, IGeoRenderer<T> renderer,
            T part, EntityPlayer player, float partialTicks, boolean trackArmSwing, float[] rotation,
            float[] translation, float[] scale, double offsetX, double offsetY, double offsetZ, double rotationX,
            double rotationY, double rotationZ, double scaleMultiplier) {
        GlStateManager.pushMatrix();
        try {
            applyHandAnchor(player, trackArmSwing);
            applyDisplayTransform(rotation, translation, scale);
            applyConfigTweak(offsetX, offsetY, offsetZ, rotationX, rotationY, rotationZ, scaleMultiplier);

            GeoModel geoModel = model.getModel(model.getModelLocation(part));
            model.setLivingAnimations(part, player.getEntityId(), newEvent(part, player, partialTicks));

            Minecraft.getMinecraft().renderEngine.bindTexture(model.getTextureLocation(part));
            renderer.render(geoModel, part, partialTicks, 1f, 1f, 1f, 1f);
        } finally {
            GlStateManager.popMatrix();
        }
    }

    /**
     * Moves to the left shoulder pivot, then applies LayerHeldItem's fixed
     * left-hand chain. {@code followArm} anchors to the upper arm itself
     * (swinging, attacking, item-use poses, and Mo' Bends' bends); otherwise
     * anchors to the torso at the arm's rest pivot, so a chest badge leans
     * with the body (sneaking, attack twist, Mo' Bends torso bends) without
     * swinging along with the arm.
     */
    private static void applyHandAnchor(EntityPlayer player, boolean followArm) {
        ModelBiped model = getBipedModel(player);
        if (model != null && followArm) {
            applyUpperArmTransform(model.bipedLeftArm);
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

    /** Live-tunable nudge on top of the base transform — see ForgeConfigHandler's placement sections. */
    private static void applyConfigTweak(double offsetX, double offsetY, double offsetZ, double rotationX,
            double rotationY, double rotationZ, double scale) {
        GlStateManager.translate((float) offsetX, (float) offsetY, (float) offsetZ);
        GlStateManager.rotate((float) rotationZ, 0f, 0f, 1f);
        GlStateManager.rotate((float) rotationY, 0f, 1f, 0f);
        GlStateManager.rotate((float) rotationX, 1f, 0f, 0f);
        GlStateManager.scale((float) scale, (float) scale, (float) scale);
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
