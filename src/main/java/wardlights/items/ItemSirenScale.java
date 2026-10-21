package wardlights.items;

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
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import wardlights.handlers.ModRegistry;
import wardlights.handlers.IceAndFireBaubleHandler;

/**
 * Worn in the Baubles AMULET slot, dropped by Ice and Fire's Sirens. Gives
 * Lycanites Mobs' Swiftswimming I while worn ({@link IceAndFireBaubleHandler}).
 */
public class ItemSirenScale extends Item implements IBauble {

    public ItemSirenScale(String registryName) {
        setRegistryName(registryName);
        setTranslationKey(registryName);
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.COMBAT);
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.AMULET;
    }

    @Override
    public void onEquipped(ItemStack itemStack, EntityLivingBase wearer) {
        IceAndFireBaubleHandler.onSirenScaleEquipped(wearer);
    }

    @Override
    public void onUnequipped(ItemStack itemStack, EntityLivingBase wearer) {
        IceAndFireBaubleHandler.onSirenScaleUnequipped(wearer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        if (Loader.isModLoaded(ModRegistry.LYCANITESMOBS_MODID)) {
            tooltip.add(I18n.format("tooltip.wardlights.siren_scale.swiftswimming"));
        }
    }
}
