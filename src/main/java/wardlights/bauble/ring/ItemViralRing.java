package wardlights.bauble.ring;

import java.util.List;

import javax.annotation.Nullable;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import wardlights.config.WardlightsConfig;

/**
 * Worn in a Baubles RING slot, it keeps SRParasites' Viral from stacking past
 * a configured level. Viral multiplies the bonus damage parasites deal by
 * (amplifier + 1) and each hit stacks it one higher, so capping it at II keeps
 * that bonus at double at most.
 *
 * <p>The cap itself is applied in {@link ViralLimitHandler}, shared with
 * Trinkets and Baubles' Poison Stone.
 */
public class ItemViralRing extends Item implements IBauble {

    public ItemViralRing(String registryName) {
        setRegistryName(registryName);
        setTranslationKey(registryName);
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.COMBAT);
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.RING;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(I18n.format("tooltip.wardlights.viral_ring.limit",
                I18n.format("enchantment.level." + WardlightsConfig.server.baubles.viralRingMaxLevel)));
    }
}
