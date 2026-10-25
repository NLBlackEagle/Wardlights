package wardlights.bauble.iceandfire;

import java.util.List;

import javax.annotation.Nullable;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import wardlights.config.WardlightsConfig;

/**
 * Worn in the Baubles HEAD slot, dropped by Ice and Fire's Cyclops. Gives
 * one extra Strength level (Strength I on its own) and extra Looting while worn, like
 * Trinkets and Baubles' Dragon's Eye. Both are handled in {@link IceAndFireBaubleHandler}.
 */
public class ItemCyclopsEye extends Item implements IBauble {

    public ItemCyclopsEye(String registryName) {
        setRegistryName(registryName);
        setTranslationKey(registryName);
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.COMBAT);
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.HEAD;
    }

    @Override
    public void onEquipped(ItemStack itemStack, EntityLivingBase wearer) {
        IceAndFireBaubleHandler.onCyclopsEyeEquipped(wearer);
    }

    @Override
    public void onUnequipped(ItemStack itemStack, EntityLivingBase wearer) {
        IceAndFireBaubleHandler.onCyclopsEyeUnequipped(wearer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(I18n.format("tooltip.wardlights.cyclops_eye.strength"));
        int looting = WardlightsConfig.server.baubles.cyclopsEyeLooting;
        if (looting > 0) {
            tooltip.add(I18n.format("tooltip.wardlights.cyclops_eye.looting", I18n.format("enchantment.level." + looting)));
        }
    }
}
