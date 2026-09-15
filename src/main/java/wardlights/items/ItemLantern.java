package wardlights.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

/**
 * Temporary stub for the Phase 3 model checkpoint — just enough to bind a
 * ModelResourceLocation and see the Regular lantern render in-game.
 * IBauble behaviour, LanternType data and the other 4 variants come later.
 */
public class ItemLantern extends Item {

    public ItemLantern(String registryName) {
        setRegistryName(registryName);
        setTranslationKey(registryName);
        setCreativeTab(CreativeTabs.MISC);
        setMaxStackSize(1);
    }
}
