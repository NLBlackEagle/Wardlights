package wardlights.items;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import wardlights.Wardlights;
import wardlights.handlers.ForgeConfigHandler;
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

    /** Only one lantern can be worn at a time. */
    @Override
    public boolean canEquip(ItemStack itemStack, EntityLivingBase wearer) {
        return !(wearer instanceof EntityPlayer) || LanternUtil.getWornLantern((EntityPlayer) wearer).isEmpty();
    }
}
