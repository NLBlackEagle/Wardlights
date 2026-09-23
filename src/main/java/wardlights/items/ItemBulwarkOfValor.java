package wardlights.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

/**
 * Test version: non-animated model/texture, just a plain holdable item for now.
 */
public class ItemBulwarkOfValor extends Item {

    public ItemBulwarkOfValor(String registryName) {
        setRegistryName(registryName);
        setTranslationKey(registryName);
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.COMBAT);
    }
}
