package wardlights.items;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.render.IRenderBauble;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import wardlights.Wardlights;

/**
 * The only obtainable item of this set — worn in the Baubles BODY slot, it
 * renders both the badge (chest) and shoulder (idle-animated) GeckoLib parts
 * on the wearer at once. See {@link wardlights.client.render.AegisOfSacrificeBaubleRenderer}
 * for the actual draw calls, reached through {@link Wardlights#PROXY} so this
 * (common) class never references client-only rendering classes directly.
 */
public class ItemAegisOfSacrifice extends Item implements IBauble, IRenderBauble {

    public ItemAegisOfSacrifice(String registryName) {
        setRegistryName(registryName);
        setTranslationKey(registryName);
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.COMBAT);
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.BODY;
    }

    @Override
    public void onWornTick(ItemStack itemStack, EntityLivingBase wearer) {
    }

    @Override
    public void onEquipped(ItemStack itemStack, EntityLivingBase wearer) {
    }

    @Override
    public void onUnequipped(ItemStack itemStack, EntityLivingBase wearer) {
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
            Wardlights.PROXY.renderAegisOfSacrificeBauble(player, partialTicks);
        }
    }
}
