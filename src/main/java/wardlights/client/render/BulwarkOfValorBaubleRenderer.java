package wardlights.client.render;

import baubles.api.render.IRenderBauble;
import net.minecraft.entity.player.EntityPlayer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import wardlights.animation.BulwarkBadgePart;
import wardlights.animation.BulwarkShoulderPart;
import wardlights.client.model.BulwarkBadgeModel;
import wardlights.client.model.BulwarkShoulderModel;
import wardlights.handlers.ForgeConfigHandler;

/**
 * Draws the two Bulwark of Valor GeckoLib parts on the wearer when worn in
 * the Baubles body slot. Called from {@link wardlights.items.ItemBulwarkOfValor}
 * for {@link IRenderBauble.RenderType#BODY} only. The transform chain itself
 * lives in {@link BaubleRenderHelper}.
 */
public final class BulwarkOfValorBaubleRenderer {

    private BulwarkOfValorBaubleRenderer() {
    }

    private static final BulwarkShoulderModel SHOULDER_MODEL = new BulwarkShoulderModel();
    private static final BulwarkBadgeModel BADGE_MODEL = new BulwarkBadgeModel();

    private static final IGeoRenderer<BulwarkShoulderPart> SHOULDER_RENDERER = BaubleRenderHelper
            .geoRenderer(SHOULDER_MODEL);
    private static final IGeoRenderer<BulwarkBadgePart> BADGE_RENDERER = BaubleRenderHelper.geoRenderer(BADGE_MODEL);

    static {
        BaubleRenderHelper.registerModelFetcher(BulwarkShoulderPart.class, SHOULDER_MODEL);
        BaubleRenderHelper.registerModelFetcher(BulwarkBadgePart.class, BADGE_MODEL);
    }

    // Each part's own models/item/*.json "display.thirdperson_lefthand"
    // block — must stay in sync with those files if re-exported.
    private static final float[] BADGE_ROTATION = { 90f, 90f, 0f };
    private static final float[] BADGE_TRANSLATION = { -3.5f, 0.25f, 7f };
    private static final float[] BADGE_SCALE = { 0.15f, 0.15f, 0.15f };

    private static final float[] SHOULDER_ROTATION = { 90f, 90f, 0f };
    private static final float[] SHOULDER_TRANSLATION = { 0.65f, -1.8f, 3.8f };
    private static final float[] SHOULDER_SCALE = { 0.48f, 0.42f, 0.48f };

    public static void render(EntityPlayer player, float partialTicks) {
        BaubleRenderHelper.renderBadge(BADGE_MODEL, BADGE_RENDERER, BulwarkBadgePart.INSTANCE, player, partialTicks,
                BADGE_ROTATION, BADGE_TRANSLATION, BADGE_SCALE, ForgeConfigHandler.client.bulwarkPlacement);
        BaubleRenderHelper.renderShoulder(SHOULDER_MODEL, SHOULDER_RENDERER, BulwarkShoulderPart.INSTANCE, player,
                partialTicks, SHOULDER_ROTATION, SHOULDER_TRANSLATION, SHOULDER_SCALE,
                ForgeConfigHandler.client.bulwarkPlacement);
    }
}
