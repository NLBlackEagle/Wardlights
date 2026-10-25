package wardlights.compat;

import gigaherz.toolbelt.BeltFinder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import wardlights.lantern.item.ItemLanternBase;

/**
 * Lets a lantern stored in gigaherz's Tool Belt count as carried.
 *
 * <p>Only call this when Tool Belt is loaded (check {@link #MODID}), since it
 * references Tool Belt's classes and would crash on class load without it.
 */
public class ToolBeltCompat {

    public static final String MODID = "toolbelt";

    /**
     * The first lantern inside the player's belt, or empty. Tool Belt's own
     * finder covers every place a belt can be: inventory, Baubles slot or its
     * custom belt slot.
     */
    public static ItemStack findLanternInBelt(EntityPlayer player) {
        BeltFinder.BeltGetter getter = BeltFinder.findBelt(player);
        if (getter == null) {
            return ItemStack.EMPTY;
        }
        ItemStack belt = getter.getBelt();
        IItemHandler contents = belt.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (contents == null) {
            return ItemStack.EMPTY;
        }
        for (int slot = 0; slot < contents.getSlots(); slot++) {
            ItemStack stack = contents.getStackInSlot(slot);
            if (stack.getItem() instanceof ItemLanternBase) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
