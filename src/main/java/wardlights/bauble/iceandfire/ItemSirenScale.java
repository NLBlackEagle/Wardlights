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
 * Worn in any Baubles slot (TRINKET), dropped by Ice and Fire's Sirens. Raises swim
 * speed while worn, stacking with Lycanites' Swiftswimming
 * ({@link IceAndFireBaubleHandler#SIREN_SCALE_SWIM_SPEED}).
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
        return BaubleType.TRINKET;
    }

    @Override
    public void onWornTick(ItemStack itemStack, EntityLivingBase wearer) {
        IceAndFireBaubleHandler.SIREN_SCALE_SWIM_SPEED.apply(wearer);
    }

    @Override
    public void onUnequipped(ItemStack itemStack, EntityLivingBase wearer) {
        IceAndFireBaubleHandler.SIREN_SCALE_SWIM_SPEED.remove(wearer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        double swimSpeed = WardlightsConfig.server.baubles.sirenScaleSwimSpeed;
        if (swimSpeed > 0) {
            tooltip.add(I18n.format("tooltip.wardlights.siren_scale.swim_speed",
                    ItemStack.DECIMALFORMAT.format(swimSpeed * 100)));
        }
    }
}
