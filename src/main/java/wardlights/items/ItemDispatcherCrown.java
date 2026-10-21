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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import wardlights.handlers.DispatcherCrownHandler;
import wardlights.handlers.ForgeConfigHandler;
import wardlights.handlers.ForgeConfigHandler.ServerConfig.BaubleConfig;

/**
 * Worn in the Baubles HEAD slot. While the wearer's health is below a configured
 * percentage they have SRParasites' Rage (by default Rage I below 85%, Rage II
 * below 35%), and they slowly adapt to whatever hurts them.
 * Both happen in {@link DispatcherCrownHandler}.
 *
 * <p>The adaptation lives in the crown's NBT and is cleared when it's taken off.
 */
public class ItemDispatcherCrown extends Item implements IBauble {

    public static final String ADAPTATION_TAG = "Adaptation";

    /** Rage is checked twice a second, plenty for something that lasts 15 seconds per top-up. */
    private static final int RAGE_CHECK_INTERVAL = 10;

    public ItemDispatcherCrown(String registryName) {
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
    public void onWornTick(ItemStack itemStack, EntityLivingBase wearer) {
        if (wearer.ticksExisted % RAGE_CHECK_INTERVAL == 0) {
            DispatcherCrownHandler.updateRage(wearer);
        }
    }

    @Override
    public void onUnequipped(ItemStack itemStack, EntityLivingBase wearer) {
        // Taking the crown off forgets everything; putting it back on starts fresh.
        itemStack.setTagCompound(null);
        DispatcherCrownHandler.removeRage(wearer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        BaubleConfig config = ForgeConfigHandler.server.baubles;
        addRageLine(tooltip, config.dispatcherCrownRageIHealth, 1);
        addRageLine(tooltip, config.dispatcherCrownRageIIHealth, 2);
        if (config.dispatcherCrownMaxAdaptation > 0) {
            tooltip.add(I18n.format("tooltip.wardlights.dispatcher_crown.adaptation",
                    Math.round(config.dispatcherCrownMaxAdaptation * 100) + "%"));
        }
    }

    @SideOnly(Side.CLIENT)
    private static void addRageLine(List<String> tooltip, int healthPercent, int level) {
        if (healthPercent > 0) {
            tooltip.add(I18n.format("tooltip.wardlights.dispatcher_crown.rage", healthPercent + "%",
                    I18n.format("enchantment.level." + level)));
        }
    }

    public static NBTTagCompound getOrCreateTag(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        return stack.getTagCompound();
    }
}
