package wardlights.items;

import java.util.ArrayList;
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
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import wardlights.Wardlights;
import wardlights.handlers.DispatcherCrownHandler;
import wardlights.handlers.ForgeConfigHandler;
import wardlights.handlers.ForgeConfigHandler.ServerConfig.BaubleConfig;
import wardlights.handlers.ModRegistry;

/**
 * Worn in the Baubles HEAD slot. When the wearer's health drops below a
 * configured fraction they get a burst of SRParasites' Rage (by default Rage I
 * below 85%, Rage II below 35%), and they slowly adapt to whatever hurts them
 * ({@link DispatcherCrownHandler}).
 *
 * <p>Each Rage tier fires once when health crosses below it, and is armed again
 * once health is back above it. That state and the adaptation live in the
 * crown's NBT and are cleared when it's taken off.
 */
public class ItemDispatcherCrown extends Item implements IBauble {

    public static final String ADAPTATION_TAG = "Adaptation";
    private static final String RAGE_USED_TAG = "RageUsed";

    private static final ResourceLocation RAGE_ID = new ResourceLocation(ModRegistry.SRPARASITES_MODID, "rage");

    @Nullable
    private static Potion rage;

    /** Parsed {@link BaubleConfig#dispatcherCrownRage}, redone whenever that array is replaced. */
    private static String[] parsedFrom;
    private static List<RageTier> tiers = new ArrayList<>();

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
        Potion potion = getRage();
        if (wearer.world.isRemote || potion == null) {
            return;
        }
        float health = wearer.getHealth() / wearer.getMaxHealth();
        List<RageTier> rageTiers = getTiers();
        for (int i = 0; i < rageTiers.size(); i++) {
            RageTier tier = rageTiers.get(i);
            boolean below = health < tier.healthFraction;
            boolean used = itemStack.hasTagCompound() && itemStack.getTagCompound().getBoolean(RAGE_USED_TAG + i);
            if (below && !used) {
                wearer.addPotionEffect(new PotionEffect(potion, tier.seconds * 20, tier.level - 1));
                getOrCreateTag(itemStack).setBoolean(RAGE_USED_TAG + i, true);
            } else if (!below && used) {
                itemStack.getTagCompound().removeTag(RAGE_USED_TAG + i);
            }
        }
    }

    @Override
    public void onUnequipped(ItemStack itemStack, EntityLivingBase wearer) {
        // Taking the crown off forgets everything; putting it back on starts fresh.
        itemStack.setTagCompound(null);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        for (RageTier tier : getTiers()) {
            tooltip.add(I18n.format("tooltip.wardlights.dispatcher_crown.rage", percent(tier.healthFraction),
                    I18n.format("enchantment.level." + tier.level), tier.seconds));
        }
        double maxAdaptation = ForgeConfigHandler.server.baubles.dispatcherCrownMaxAdaptation;
        if (maxAdaptation > 0) {
            tooltip.add(I18n.format("tooltip.wardlights.dispatcher_crown.adaptation", percent(maxAdaptation)));
        }
    }

    public static NBTTagCompound getOrCreateTag(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        return stack.getTagCompound();
    }

    /** 0.85 -> "85%". */
    private static String percent(double fraction) {
        return Math.round(fraction * 100) + "%";
    }

    private static List<RageTier> getTiers() {
        String[] config = ForgeConfigHandler.server.baubles.dispatcherCrownRage;
        if (config != parsedFrom) {
            parsedFrom = config;
            tiers = parseTiers(config);
        }
        return tiers;
    }

    /** {@code healthFraction;rageLevel;seconds} lines; malformed ones are skipped with a warning. */
    private static List<RageTier> parseTiers(String[] lines) {
        List<RageTier> parsed = new ArrayList<>();
        for (String line : lines) {
            String[] parts = line.split(";");
            try {
                if (parts.length != 3) {
                    throw new NumberFormatException();
                }
                parsed.add(new RageTier(Float.parseFloat(parts[0].trim()), Integer.parseInt(parts[1].trim()),
                        Integer.parseInt(parts[2].trim())));
            } catch (NumberFormatException e) {
                Wardlights.LOGGER.warn("Dispatcher Crown Rage: '{}' should be healthFraction;rageLevel;seconds "
                        + "(e.g. 0.85;1;15)", line);
            }
        }
        return parsed;
    }

    /** SRParasites' Rage potion, or null when it isn't installed. */
    @Nullable
    private static Potion getRage() {
        if (rage == null) {
            rage = ForgeRegistries.POTIONS.getValue(RAGE_ID);
        }
        return rage;
    }

    private static final class RageTier {
        final float healthFraction;
        final int level;
        final int seconds;

        RageTier(float healthFraction, int level, int seconds) {
            this.healthFraction = healthFraction;
            this.level = level;
            this.seconds = seconds;
        }
    }
}
