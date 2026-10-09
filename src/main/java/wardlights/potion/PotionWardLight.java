package wardlights.potion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import wardlights.Wardlights;

/**
 * Shown while the player's lantern is working (see WardLightHandler). It does
 * nothing by itself; it only tells the player the ward is up.
 *
 * <p>Vanilla effect icons come from one shared sheet, so this draws its own
 * 18x18 icon at the same spots vanilla would.
 */
public class PotionWardLight extends Potion {

    private static final ResourceLocation ICON = new ResourceLocation(Wardlights.MODID,
            "textures/gui/potion/wardlight.png");
    private static final int ICON_SIZE = 18;

    public PotionWardLight() {
        super(false, 0xFFE8A0);
        setRegistryName(Wardlights.MODID, "wardlight");
        setPotionName("effect.wardlights.wardlight");
        setBeneficial();
    }

    /**
     * Vanilla says "no icon" for any potion without a spot on its own sheet,
     * and some effect lists then skip the icon entirely instead of asking us
     * to draw it. Saying yes keeps ours showing there too.
     */
    @Override
    public boolean hasStatusIcon() {
        return true;
    }

    /*
     * These are the older (deprecated) signatures on purpose: Forge's newer
     * ones just call these by default, but some mods' effect lists still call
     * the old ones directly. Overriding these covers both.
     */

    /** Inventory screen: vanilla puts the icon 6 right and 7 down from the panel's corner. */
    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        drawIcon(x + 6, y + 7, 1.0F);
    }

    /** Top-right HUD: vanilla puts the icon 3 in from the frame's corner. */
    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha) {
        drawIcon(x + 3, y + 3, alpha);
    }

    @SideOnly(Side.CLIENT)
    private static void drawIcon(int x, int y, float alpha) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(ICON);
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
    }
}
