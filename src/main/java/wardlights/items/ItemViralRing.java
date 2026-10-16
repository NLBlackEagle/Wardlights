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
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import wardlights.handlers.ForgeConfigHandler;
import wardlights.handlers.ModRegistry;

/**
 * Worn in a Baubles RING slot, it keeps SRParasites' Viral from stacking past
 * a configured level. Viral multiplies the bonus damage parasites deal by
 * (amplifier + 1) and each hit stacks it one higher, so capping it at II keeps
 * that bonus at double at most.
 *
 * <p>The cap is applied every tick in {@link #onWornTick}: a hit may push
 * Viral one past the cap, but it's knocked back down before the next hit lands.
 */
public class ItemViralRing extends Item implements IBauble {

    private static final ResourceLocation VIRAL_ID = new ResourceLocation(ModRegistry.SRPARASITES_MODID, "viral");

    @Nullable
    private static Potion viral;

    public ItemViralRing(String registryName) {
        setRegistryName(registryName);
        setTranslationKey(registryName);
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.COMBAT);
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.RING;
    }

    @Override
    public void onWornTick(ItemStack itemStack, EntityLivingBase wearer) {
        Potion potion = getViral();
        if (wearer.world.isRemote || potion == null) {
            return;
        }
        int maxAmplifier = ForgeConfigHandler.server.baubles.viralRingMaxLevel - 1;
        PotionEffect active = wearer.getActivePotionEffect(potion);
        if (active != null && active.getAmplifier() > maxAmplifier) {
            // addPotionEffect never lowers an amplifier (it only merges upward),
            // so the old effect has to go first. The remaining duration is kept.
            wearer.removePotionEffect(potion);
            wearer.addPotionEffect(new PotionEffect(potion, active.getDuration(), maxAmplifier,
                    active.getIsAmbient(), active.doesShowParticles()));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(I18n.format("tooltip.wardlights.viral_ring.limit",
                I18n.format("enchantment.level." + ForgeConfigHandler.server.baubles.viralRingMaxLevel)));
    }

    /** SRParasites' Viral potion, or null when it isn't installed. */
    @Nullable
    private static Potion getViral() {
        if (viral == null) {
            viral = ForgeRegistries.POTIONS.getValue(VIRAL_ID);
        }
        return viral;
    }
}
