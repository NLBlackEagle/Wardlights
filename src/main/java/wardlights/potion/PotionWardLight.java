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
import wardlights.lantern.LanternType;

/**
 * Shown while a lantern's ward works for the player (see WardLightHandler), one
 * per lantern type, so the player sees which wards are up: their carried
 * lantern's, plus those of working placed lanterns nearby. It does nothing by itself.
 *
 * <p>Registry name and icon: {@code wardlight} for the Regular Lantern (the
 * original name, so existing saves keep it), {@code wardlight_<type>} for the
 * others. The icon is {@code textures/gui/potion/<registry name>.png}; they all
 * share the name "WardLight".
 *
 * <p>Vanilla effect icons come from one shared sheet, so this draws its own
 * 18x18 icon at the same spots vanilla would.
 */
public class PotionWardLight extends Potion {

    private static final int ICON_SIZE = 18;

    private final ResourceLocation icon;

    public PotionWardLight(LanternType type, int color) {
        super(false, color);
        String name = type == LanternType.REGULAR ? "wardlight" : "wardlight_" + type.name().toLowerCase();
        setRegistryName(Wardlights.MODID, name);
        // Same name for every type; the icon tells them apart.
        setPotionName("effect.wardlights.wardlight");
        setBeneficial();
        icon = new ResourceLocation(Wardlights.MODID, "textures/gui/potion/" + name + ".png");
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
        drawIcon(icon, x + 6, y + 7, 1.0F);
    }

    /** Top-right HUD: vanilla puts the icon 3 in from the frame's corner. */
    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha) {
        drawIcon(icon, x + 3, y + 3, alpha);
    }

    @SideOnly(Side.CLIENT)
    private static void drawIcon(ResourceLocation icon, int x, int y, float alpha) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(icon);
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
    }
}
