package wardlights.items;

import java.util.List;
import java.util.function.IntSupplier;

import javax.annotation.Nullable;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A plain crafting ingredient (alloys, essences, the Badge of Bravery...). These
 * have no behavior of their own; they only exist to be crafted into baubles.
 */
public class ItemMaterial extends Item {

    /** No color multiplier: the texture is drawn as-is. */
    public static final int NO_TINT = -1;

    private final int tintColor;
    @Nullable
    private IntSupplier glintColor;

    public ItemMaterial(String registryName, int tintColor) {
        setRegistryName(registryName);
        setTranslationKey(registryName);
        setCreativeTab(CreativeTabs.MATERIALS);
        this.tintColor = tintColor;
    }

    /**
     * 0xRRGGBB multiplied over the texture's colors, or {@link #NO_TINT}. Lets
     * items reuse a vanilla texture (e.g. the iron ingot) in their own color.
     */
    public int getTintColor() {
        return tintColor;
    }

    /**
     * Gives this item an enchantment-style glint in the supplied 0xRRGGBB color
     * instead of the vanilla purple (recolored by {@code RenderItemMixin}).
     */
    public ItemMaterial setGlintColor(IntSupplier glintColor) {
        this.glintColor = glintColor;
        return this;
    }

    /** The glint color, or null when this item has no glint of its own. */
    @Nullable
    public IntSupplier getGlintColor() {
        return glintColor;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return glintColor != null || super.hasEffect(stack);
    }

    /**
     * Shows {@code tooltip.wardlights.<item>} from the lang file, if there is one,
     * so items without a recipe can say where they come from.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        String key = "tooltip.wardlights." + getRegistryName().getPath();
        if (I18n.hasKey(key)) {
            tooltip.add(I18n.format(key));
        }
    }
}
