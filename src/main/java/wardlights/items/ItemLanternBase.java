package wardlights.items;

import java.util.List;

import javax.annotation.Nullable;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import wardlights.Wardlights;
import wardlights.compat.ArtifactsCompat;
import wardlights.handlers.ForgeConfigHandler;
import wardlights.handlers.ForgeConfigHandler.ServerConfig.LanternConfig;
import wardlights.handlers.ModRegistry;
import wardlights.handlers.PaleLanternHandler;
import wardlights.lantern.LanternType;
import wardlights.util.LanternUtil;

/**
 * Shared base of the five lanterns: placeable as a block (via the inherited
 * ItemBlock#onItemUse), animated with GeckoLib, and wearable as a bauble.
 * Subclasses only pick their block, type and animation controllers.
 */
public abstract class ItemLanternBase extends ItemBlock implements IAnimatable, IBauble {

    private final AnimationFactory factory = new AnimationFactory(this);
    private final LanternType type;

    protected ItemLanternBase(Block block, String registryName, LanternType type) {
        super(block);
        setRegistryName(registryName);
        setMaxStackSize(1);
        this.type = type;
    }

    public LanternType getLanternType() {
        return type;
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        String slot = ForgeConfigHandler.server.lanterns.baubleSlot.trim();
        try {
            return BaubleType.valueOf(slot.toUpperCase());
        } catch (IllegalArgumentException e) {
            Wardlights.LOGGER.warn("Unknown lantern bauble slot '{}', using BELT", slot);
            return BaubleType.BELT;
        }
    }

    private static final String TOOLTIP_PREFIX = "tooltip.wardlights.lantern.";

    /**
     * Lines for what this lantern does, behind Ctrl since there's a lot, under a
     * header naming the configured activation condition. Numbers come from the
     * config, and a line for another mod's feature only shows when that mod is
     * installed. Colors come from the lang file.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        LanternConfig config = ForgeConfigHandler.server.lanterns;
        tooltip.add(line("header." + config.activationCondition.name().toLowerCase()));
        if (!GuiScreen.isCtrlKeyDown()) {
            tooltip.add(line("hold_ctrl"));
            return;
        }
        tooltip.add(line("at_bay"));
        switch (type) {
            case PIXIE:
                if (Loader.isModLoaded(ModRegistry.ICEANDFIRE_MODID)) {
                    tooltip.add(line("pixie.steal"));
                }
                if (Loader.isModLoaded(ArtifactsCompat.MODID) && config.pixieMimicChance > 0) {
                    tooltip.add(line("pixie.mimic", Math.round(config.pixieMimicChance * 100), config.pixieMimicMaxY));
                }
                tooltip.add(line("pixie.luck"));
                break;
            case VAPULA:
                tooltip.add(line("vapula.mining"));
                tooltip.add(line("vapula.allies"));
                tooltip.add(line("vapula.fortune"));
                break;
            case PALE:
                if (Loader.isModLoaded(ModRegistry.PALEBLOOM_MODID)) {
                    tooltip.add(line("pale.ignore"));
                }
                if (PaleLanternHandler.getPaleLung() != null) {
                    if (config.paleLungHittingChance > 0) {
                        tooltip.add(line("pale.pale_lung.hitting", Math.round(config.paleLungHittingChance * 100)));
                    }
                    if (config.paleLungHitByChance > 0) {
                        tooltip.add(line("pale.pale_lung.hit_by", Math.round(config.paleLungHitByChance * 100)));
                    }
                    if (config.paleLungPlacedChance > 0) {
                        tooltip.add(line("pale.pale_lung.placed", Math.round(config.paleLungPlacedChance * 100),
                                config.lanternRadius));
                    }
                }
                break;
            case PARASITE:
                if (Loader.isModLoaded(ModRegistry.SRPARASITES_MODID)) {
                    if (config.parasiteIgnoreChance > 0) {
                        tooltip.add(line("srp.ignore", Math.round(config.parasiteIgnoreChance * 100)));
                    }
                    tooltip.add(line("srp.coth", config.lanternRadius));
                    if (config.parasiteFearDuration < 1) {
                        tooltip.add(line("srp.fear", Math.round((1 - config.parasiteFearDuration) * 100)));
                    }
                }
                break;
            default:
                break;
        }
    }

    @SideOnly(Side.CLIENT)
    private static String line(String key, Object... args) {
        return I18n.format(TOOLTIP_PREFIX + key, args);
    }

    /** Only one lantern can be worn at a time. */
    @Override
    public boolean canEquip(ItemStack itemStack, EntityLivingBase wearer) {
        return !(wearer instanceof EntityPlayer) || LanternUtil.getWornLantern((EntityPlayer) wearer).isEmpty();
    }
}
