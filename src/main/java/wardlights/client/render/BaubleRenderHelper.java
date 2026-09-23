package wardlights.client.render;

import java.util.Collections;

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
 * Badges stay anchored to the arm's rest pivot only; shoulder pads
 * additionally track the arm's live swing rotation (see
 * {@link #applyHandAnchor(EntityPlayer, boolean)}) so they move like a real
 * pauldron.
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

    // ModelBiped#bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F) (pixels),
    // converted to block units the same way ModelRenderer#postRender does
    // (rotationPoint * 0.0625F).
    private static final float ARM_PIVOT_X = 5.0f * 0.0625f;
    private static final float ARM_PIVOT_Y = 2.0f * 0.0625f;
    private static final float ARM_PIVOT_Z = 0.0f;

    // LayerHeldItem#renderHeldItem's fixed chain for the LEFT hand, after
    // ModelBiped#postRenderArm's pivot translate above:
    // rotate(-90, X); rotate(180, Y); translate((-1)/16, 0.125, -0.625).
    private static final float HAND_OFFSET_X = -1.0f / 16.0f;
    private static final float HAND_OFFSET_Y = 0.125f;
    private static final float HAND_OFFSET_Z = -0.625f;

    private static final float RAD_TO_DEG = 57.295776f;

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
     * ModelBiped#postRenderArm(0.0625F, LEFT) + LayerHeldItem's fixed left-hand
     * chain. When {@code trackArmSwing} is true, also replicates the swing
     * rotation half of postRenderArm/ModelRenderer#postRender (the arm's live
     * rotateAngleX/Y/Z — swimming, attacking, item-use poses, etc.) by reading
     * it straight off the player's own currently-rendering ModelBiped, so the
     * shoulder pad moves with the arm the same way a real pauldron would.
     */
    private static void applyHandAnchor(EntityPlayer player, boolean trackArmSwing) {
        GlStateManager.translate(ARM_PIVOT_X, ARM_PIVOT_Y, ARM_PIVOT_Z);
        if (trackArmSwing) {
            float[] rot = getLeftArmRotationDegrees(player);
            if (rot != null) {
                if (rot[2] != 0f) {
                    GlStateManager.rotate(rot[2], 0f, 0f, 1f);
                }
                if (rot[1] != 0f) {
                    GlStateManager.rotate(rot[1], 0f, 1f, 0f);
                }
                if (rot[0] != 0f) {
                    GlStateManager.rotate(rot[0], 1f, 0f, 0f);
                }
            }
        }
        GlStateManager.rotate(-90f, 1f, 0f, 0f);
        GlStateManager.rotate(180f, 0f, 1f, 0f);
        GlStateManager.translate(HAND_OFFSET_X, HAND_OFFSET_Y, HAND_OFFSET_Z);
    }

    /** The left arm's current rotateAngleX/Y/Z (degrees), or null off-thread/wrong model. */
    private static float[] getLeftArmRotationDegrees(EntityPlayer player) {
        Render<?> renderer = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(player);
        if (!(renderer instanceof RenderLivingBase)) {
            return null;
        }
        ModelBase model = ((RenderLivingBase<?>) renderer).getMainModel();
        if (!(model instanceof ModelBiped)) {
            return null;
        }
        ModelRenderer arm = ((ModelBiped) model).bipedLeftArm;
        return new float[] { arm.rotateAngleX * RAD_TO_DEG, arm.rotateAngleY * RAD_TO_DEG,
                arm.rotateAngleZ * RAD_TO_DEG };
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
