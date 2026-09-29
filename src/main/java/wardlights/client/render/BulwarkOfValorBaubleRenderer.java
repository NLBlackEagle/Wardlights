package wardlights.client.render;

import baubles.api.render.IRenderBauble;
import net.minecraft.entity.player.EntityPlayer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import wardlights.animation.BulwarkBadgePart;
import wardlights.client.model.BulwarkBadgeModel;
import wardlights.handlers.ForgeConfigHandler;

/**
 * Draws the Bulwark of Valor GeckoLib badge on the wearer when worn in
 * the Baubles body slot. Called from {@link wardlights.items.ItemBulwarkOfValor}
 * for {@link IRenderBauble.RenderType#BODY} only. The transform chain itself
 * lives in {@link BaubleRenderHelper}.
 */
public final class BulwarkOfValorBaubleRenderer {

    private BulwarkOfValorBaubleRenderer() {
    }

    private static final BulwarkBadgeModel BADGE_MODEL = new BulwarkBadgeModel();

    private static final IGeoRenderer<BulwarkBadgePart> BADGE_RENDERER = BaubleRenderHelper.geoRenderer(BADGE_MODEL);

    static {
        BaubleRenderHelper.registerModelFetcher(BulwarkBadgePart.class, BADGE_MODEL);
    }

    // The badge's models/item/*.json "display.thirdperson_lefthand" block —
    // must stay in sync with that file if re-exported.
    private static final float[] BADGE_ROTATION = { 90f, 90f, 0f };
    private static final float[] BADGE_TRANSLATION = { -3.75f, 0.5f, 7.25f };
    private static final float[] BADGE_SCALE = { 0.15f, 0.15f, 0.15f };

    private static final BaublePlacement BADGE_PLACEMENT = new BaublePlacement(
            "Bulwark of Valor Badge Placement",
            () -> ForgeConfigHandler.client.bulwarkBadgePlacement,
            () -> ForgeConfigHandler.client.bulwarkBadgeArmorPlacement);

    public static void render(EntityPlayer player, float partialTicks) {
        BaubleRenderHelper.renderBadge(BADGE_MODEL, BADGE_RENDERER, BulwarkBadgePart.INSTANCE, player, partialTicks,
                BADGE_ROTATION, BADGE_TRANSLATION, BADGE_SCALE, BADGE_PLACEMENT);
    }
}
