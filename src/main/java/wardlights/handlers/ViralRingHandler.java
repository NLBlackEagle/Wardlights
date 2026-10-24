package wardlights.handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import wardlights.Wardlights;
import wardlights.compat.BaublesUtil;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = Wardlights.MODID)
public class ViralRingHandler {

    private static final ResourceLocation VIRAL_ID = new ResourceLocation(ModRegistry.SRPARASITES_MODID, "viral");

    private static Potion viral;
    private static boolean handlingCuring = false; // Loop protection

    @SubscribeEvent()
    public static void onPotionApplicable(PotionEvent.PotionApplicableEvent event) {
        if(!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if(player.getEntityWorld().isRemote) return;

        if(handlingCuring) return;
        PotionEffect incomingEffect = event.getPotionEffect();
        Potion viral = getViral();
        if(viral == null) return;
        if(incomingEffect.getPotion() != viral) return;

        if(BaublesUtil.isEquipped(player, ModRegistry.VIRAL_RING)) {
            handlingCuring = true;

            int maxAmp = ForgeConfigHandler.server.baubles.viralRingMaxLevel - 1;
            if(incomingEffect.getAmplifier() > maxAmp){
                event.setResult(Event.Result.DENY);
                player.addPotionEffect(new PotionEffect(
                        incomingEffect.getPotion(),
                        incomingEffect.getDuration(),
                        maxAmp,
                        incomingEffect.getIsAmbient(),
                        incomingEffect.doesShowParticles())
                );
            }

            handlingCuring = false;
        }
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
