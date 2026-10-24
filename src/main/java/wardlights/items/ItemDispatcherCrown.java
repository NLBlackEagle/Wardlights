package wardlights.items;

import java.math.RoundingMode;
import java.text.DecimalFormat;
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
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import wardlights.handlers.DispatcherCrownHandler;
import wardlights.handlers.ForgeConfigHandler;
import wardlights.handlers.ForgeConfigHandler.ServerConfig.BaubleConfig;

/**
 * The Blooming Crown (registered as dispatcher_crown, its original name), worn
 * in the Baubles HEAD slot. While the wearer's health is below a configured
 * percentage they have SRParasites' Rage (by default Rage I below 85%, Rage II
 * below 35%), and they slowly adapt to whatever hurts them, like SRP's
 * Sentient Armor. Both happen in {@link DispatcherCrownHandler}.
 *
 * <p>The adaptation lives in the crown's NBT as a list of source/points entries,
 * shown in the tooltip like SRP's armor, and is cleared when it's taken off.
 */
public class ItemDispatcherCrown extends Item implements IBauble {

    public static final String ADAPTATION_TAG = "Adaptation";
    public static final String SOURCE_TAG = "Source";
    public static final String POINTS_TAG = "Points";

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

    /** Sends the crown to the client whenever its NBT changes, so the tooltip shows what it has learned. */
    @Override
    public boolean willAutoSync(ItemStack itemStack, EntityLivingBase wearer) {
        return true;
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
        if (config.dispatcherCrownSourceCap > 0) {
            tooltip.add(I18n.format("tooltip.wardlights.dispatcher_crown.adaptation", config.dispatcherCrownSourceCap,
                    formatPercent(getReduction(config.dispatcherCrownPointCap))));
            addAdaptationLines(stack, tooltip);
        }
    }

    /** What the crown has learned so far, laid out like SRP's Sentient Armor tooltip. */
    @SideOnly(Side.CLIENT)
    private static void addAdaptationLines(ItemStack stack, List<String> tooltip) {
        if (!stack.hasTagCompound()) {
            return;
        }
        NBTTagList adaptation = stack.getTagCompound().getTagList(ADAPTATION_TAG, Constants.NBT.TAG_COMPOUND);
        if (adaptation.tagCount() == 0) {
            return;
        }
        tooltip.add(I18n.format("tooltip.wardlights.dispatcher_crown.current"));
        for (int i = 0; i < adaptation.tagCount(); i++) {
            NBTTagCompound entry = adaptation.getCompoundTagAt(i);
            tooltip.add(I18n.format("tooltip.wardlights.dispatcher_crown.source", entry.getString(SOURCE_TAG)));
            tooltip.add(I18n.format("tooltip.wardlights.dispatcher_crown.reduction",
                    formatPercent(getReduction(entry.getInteger(POINTS_TAG)))));
        }
    }

    /** Two decimals at most, rounded down, the way SRP shows it. */
    @SideOnly(Side.CLIENT)
    private static String formatPercent(float reduction) {
        DecimalFormat format = new DecimalFormat("##.##");
        format.setRoundingMode(RoundingMode.DOWN);
        return format.format(reduction * 100.0) + "%";
    }

    @SideOnly(Side.CLIENT)
    private static void addRageLine(List<String> tooltip, int healthPercent, int level) {
        if (healthPercent > 0) {
            tooltip.add(I18n.format("tooltip.wardlights.dispatcher_crown.rage", healthPercent + "%",
                    I18n.format("enchantment.level." + level)));
        }
    }

    /** The points learned for this source, or 0. */
    public static int getPoints(NBTTagList adaptation, String source) {
        for (int i = 0; i < adaptation.tagCount(); i++) {
            NBTTagCompound entry = adaptation.getCompoundTagAt(i);
            if (entry.getString(SOURCE_TAG).equals(source)) {
                return entry.getInteger(POINTS_TAG);
            }
        }
        return 0;
    }

    /** Share of damage taken off for this many points, capped like SRP's Sentient Armor Point Cap. */
    public static float getReduction(int points) {
        BaubleConfig config = ForgeConfigHandler.server.baubles;
        return (float) (Math.min(points, config.dispatcherCrownPointCap) * config.dispatcherCrownPointReduction);
    }

    public static NBTTagCompound getOrCreateTag(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        return stack.getTagCompound();
    }
}
