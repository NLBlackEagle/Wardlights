package wardlights.bauble.chest;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.render.IRenderBauble;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import wardlights.compat.EnhancedVisualsCompat;
import wardlights.compat.FirstAidCompat;
import wardlights.config.WardlightsConfig;
import wardlights.config.WardlightsConfig.ServerConfig.ChestBaubleConfig;

/**
 * Shared base for the Baubles BODY-slot chest baubles (Bulwark of Valor, Aegis
 * of Sacrifice). Both grant the same bonuses, differing only in their
 * Resistance level. Everything they do lives in {@link ChestBaubleHandler};
 * the Baubles hooks here only pass the call on.
 */
public abstract class ItemChestBauble extends Item implements IBauble, IRenderBauble {

    private static final String TOOLTIP_PREFIX = "tooltip.wardlights.chest_bauble.";

    protected ItemChestBauble(String registryName) {
        setRegistryName(registryName);
        setTranslationKey(registryName);
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.COMBAT);
    }

    /** Resistance level granted while worn, from config (1 = Resistance I, 0 = none). */
    protected abstract int getResistanceLevel();

    /** Draws the worn model; only called for {@link RenderType#BODY}. */
    protected abstract void renderOnPlayer(EntityPlayer player, float partialTicks);

    /** The Resistance amplifier granted while worn, or -1 for none. */
    public int getResistanceAmplifier() {
        return getResistanceLevel() - 1;
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.BODY;
    }

    @Override
    public void onWornTick(ItemStack itemStack, EntityLivingBase wearer) {
        ChestBaubleHandler.updateResistance(wearer, getResistanceAmplifier());
    }

    @Override
    public void onEquipped(ItemStack itemStack, EntityLivingBase wearer) {
    }

    @Override
    public void onUnequipped(ItemStack itemStack, EntityLivingBase wearer) {
        ChestBaubleHandler.removeResistance(wearer);
    }

    @Override
    public boolean canEquip(ItemStack itemStack, EntityLivingBase wearer) {
        return true;
    }

    @Override
    public boolean canUnequip(ItemStack itemStack, EntityLivingBase wearer) {
        return true;
    }

    @Override
    public boolean willAutoSync(ItemStack itemStack, EntityLivingBase wearer) {
        return true;
    }

    @Override
    public void onPlayerBaubleRender(ItemStack itemStack, EntityPlayer player, RenderType type, float partialTicks) {
        if (type == RenderType.BODY) {
            renderOnPlayer(player, partialTicks);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        ChestBaubleConfig config = WardlightsConfig.server.chestBaubles;
        if (getResistanceLevel() > 0) {
            tooltip.add(line("resistance", I18n.format(MobEffects.RESISTANCE.getName())));
        }
        if (config.explosionResistance) {
            tooltip.add(line("explosion", percent(1 - config.explosionDamageMultiplier)));
        }
        if (config.ignoreDamage) {
            tooltip.add(line("ignore_hit", config.requiredHits));
        }

        // Bonuses that only exist alongside another mod, tucked behind Ctrl.
        List<String> compat = new ArrayList<>();
        if (config.headshotSave && Loader.isModLoaded(FirstAidCompat.MODID)) {
            compat.add(line("headshot", percent(1.0 / config.headshotSaveChance)));
        }
        if (config.disableExplosionBlur && Loader.isModLoaded(EnhancedVisualsCompat.MODID)) {
            compat.add(line("explosion_blur"));
        }
        if (compat.isEmpty()) {
            return;
        }
        if (GuiScreen.isCtrlKeyDown()) {
            tooltip.addAll(compat);
        } else {
            tooltip.add(line("hold_ctrl"));
        }
    }

    /** A tooltip line; its colors come from the lang file. */
    @SideOnly(Side.CLIENT)
    private static String line(String key, Object... args) {
        return I18n.format(TOOLTIP_PREFIX + key, args);
    }

    /** 0.75 -> "75.0%". */
    private static String percent(double fraction) {
        return String.format("%.1f%%", fraction * 100);
    }
}
