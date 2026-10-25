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
 * Worn in a Baubles RING slot. Hitting a parasite inflicts SRParasites'
 * Immalleable on it, see {@link wardlights.bauble.ring.PiercingRingHandler}.
 */
public class ItemPiercingRing extends Item implements IBauble {

    public ItemPiercingRing(String registryName) {
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
        tooltip.add(I18n.format("tooltip.wardlights.piercing_ring.immalleable",
                WardlightsConfig.server.baubles.piercingRingDuration));
    }
}
