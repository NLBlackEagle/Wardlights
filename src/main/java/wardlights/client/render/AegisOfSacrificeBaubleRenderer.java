package wardlights.client.render;

import baubles.api.render.IRenderBauble;
import net.minecraft.entity.player.EntityPlayer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import wardlights.animation.AegisBadgePart;
import wardlights.animation.AegisShoulderPart;
import wardlights.client.model.AegisBadgeModel;
import wardlights.client.model.AegisShoulderModel;
import wardlights.handlers.ForgeConfigHandler;

/**
 * Draws the two Aegis of Sacrifice GeckoLib parts on the wearer when worn in
 * the Baubles body slot. Called from {@link wardlights.items.ItemAegisOfSacrifice}
 * for {@link IRenderBauble.RenderType#BODY} only. The transform chain itself
 * lives in {@link BaubleRenderHelper}.
 */
public final class AegisOfSacrificeBaubleRenderer {

    private AegisOfSacrificeBaubleRenderer() {
    }

    private static final AegisShoulderModel SHOULDER_MODEL = new AegisShoulderModel();
    private static final AegisBadgeModel BADGE_MODEL = new AegisBadgeModel();

    private static final IGeoRenderer<AegisShoulderPart> SHOULDER_RENDERER = BaubleRenderHelper
            .geoRenderer(SHOULDER_MODEL);
    private static final IGeoRenderer<AegisBadgePart> BADGE_RENDERER = BaubleRenderHelper.geoRenderer(BADGE_MODEL);

    static {
        BaubleRenderHelper.registerModelFetcher(AegisShoulderPart.class, SHOULDER_MODEL);
        BaubleRenderHelper.registerModelFetcher(AegisBadgePart.class, BADGE_MODEL);
    }

    // Each part's own models/item/*.json "display.thirdperson_lefthand"
    // block — must stay in sync with those files if re-exported.
    private static final float[] BADGE_ROTATION = { 90f, 90f, 0f };
    private static final float[] BADGE_TRANSLATION = { -3.5f, 0.25f, 7f };
    private static final float[] BADGE_SCALE = { 0.15f, 0.15f, 0.15f };

    private static final float[] SHOULDER_ROTATION = { 90f, 90f, 0f };
    private static final float[] SHOULDER_TRANSLATION = { 0.75f, -2f, 6.25f };
    private static final float[] SHOULDER_SCALE = { 0.4f, 0.4f, 0.4f };

    private static final BaublePlacement BADGE_PLACEMENT = new BaublePlacement(
            "Aegis of Sacrifice Badge Placement",
            () -> ForgeConfigHandler.client.aegisBadgePlacement,
            () -> ForgeConfigHandler.client.aegisBadgeArmorPlacement);
    private static final BaublePlacement SHOULDER_PLACEMENT = new BaublePlacement(
            "Aegis of Sacrifice Shoulder Placement",
            () -> ForgeConfigHandler.client.aegisShoulderPlacement,
            () -> ForgeConfigHandler.client.aegisShoulderArmorPlacement);

    public static void render(EntityPlayer player, float partialTicks) {
        BaubleRenderHelper.renderBadge(BADGE_MODEL, BADGE_RENDERER, AegisBadgePart.INSTANCE, player, partialTicks,
                BADGE_ROTATION, BADGE_TRANSLATION, BADGE_SCALE, BADGE_PLACEMENT);
        BaubleRenderHelper.renderShoulder(SHOULDER_MODEL, SHOULDER_RENDERER, AegisShoulderPart.INSTANCE, player,
                partialTicks, SHOULDER_ROTATION, SHOULDER_TRANSLATION, SHOULDER_SCALE,
                SHOULDER_PLACEMENT);
    }
}
