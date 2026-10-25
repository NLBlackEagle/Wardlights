package wardlights.bauble.ring;

import javax.annotation.Nullable;

import baubles.api.BaublesApi;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import wardlights.Wardlights;
import wardlights.config.WardlightsConfig;
import wardlights.config.WardlightsConfig.ServerConfig.BaubleConfig;
import wardlights.init.ModRegistry;
import wardlights.lantern.handler.ParasiteLanternHandler;

/**
 * Ring of Piercing: a player wearing it inflicts SRParasites' Immalleable on
 * any parasite they hit, melee or ranged (the shooter counts for arrows).
 */
@Mod.EventBusSubscriber(modid = Wardlights.MODID)
public class PiercingRingHandler {

    private static final ResourceLocation IMMALLEABLE_ID = new ResourceLocation(ModRegistry.SRPARASITES_MODID, "antimall");

    @Nullable
    private static Potion immalleable;

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (ModRegistry.PIERCING_RING == null || !(event.getSource().getTrueSource() instanceof EntityPlayer)) {
            return;
        }
        EntityLivingBase target = event.getEntityLiving();
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        if (target.world.isRemote || !ParasiteLanternHandler.isParasite(target)
                || BaublesApi.isBaubleEquipped(player, ModRegistry.PIERCING_RING) < 0) {
            return;
        }
        Potion potion = getImmalleable();
        if (potion == null) {
            return;
        }
        BaubleConfig config = WardlightsConfig.server.baubles;
        target.addPotionEffect(new PotionEffect(potion, config.piercingRingDuration * 20, config.piercingRingLevel - 1));
    }

    /** SRParasites' Immalleable potion, or null when it isn't installed. */
    @Nullable
    private static Potion getImmalleable() {
        if (immalleable == null) {
            immalleable = ForgeRegistries.POTIONS.getValue(IMMALLEABLE_ID);
        }
        return immalleable;
    }
}
