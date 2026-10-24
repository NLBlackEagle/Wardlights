package wardlights.handlers;

import javax.annotation.Nullable;

import baubles.api.BaublesApi;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import wardlights.Wardlights;

/**
 * Caps SRParasites' Viral for players wearing the Viral Ring (Viral II by
 * default) or Trinkets and Baubles' Poison Stone (Viral IV by default). Viral
 * multiplies the bonus damage parasites deal by (amplifier + 1) and each hit
 * stacks it one higher; when both are worn the lower cap wins.
 *
 * <p>SRP stacks Viral through addPotionEffect (SRPPotions#applyStackPotion),
 * which asks {@link PotionEvent.PotionApplicableEvent} first. A level above the
 * cap is denied there and the capped level added instead, so Viral never goes
 * past the cap, not even for the hit that applied it. Capping it on a tick
 * afterwards was too late: a hit could land while it was one level too high.
 *
 * <p>Viral picked up before the limiter was put on is knocked down by
 * {@link #onPlayerTick}.
 */
@Mod.EventBusSubscriber(modid = Wardlights.MODID)
public class ViralLimitHandler {

    private static final ResourceLocation VIRAL_ID = new ResourceLocation(ModRegistry.SRPARASITES_MODID, "viral");
    private static final ResourceLocation POISON_STONE_ID = new ResourceLocation(
            ModRegistry.TRINKETS_AND_BAUBLES_MODID, "poison_stone");

    /** No limiter worn. */
    private static final int NO_CAP = Integer.MAX_VALUE;

    /** Twice a second is plenty for Viral that was already there when the limiter went on. */
    private static final int CLAMP_INTERVAL = 10;

    @Nullable
    private static Potion viral;

    @Nullable
    private static Item poisonStone;

    @SubscribeEvent
    public static void onPotionApplicable(PotionEvent.PotionApplicableEvent event) {
        PotionEffect added = event.getPotionEffect();
        Potion potion = getViral();
        EntityLivingBase entity = event.getEntityLiving();
        if (potion == null || added.getPotion() != potion || entity.world.isRemote) {
            return;
        }
        int maxAmplifier = getMaxAmplifier(entity);
        if (added.getAmplifier() <= maxAmplifier) {
            return;
        }
        event.setResult(Event.Result.DENY);
        // Re-entering addPotionEffect is safe: the capped level passes this check,
        // and the outer call does nothing once it's denied.
        PotionEffect current = entity.getActivePotionEffect(potion);
        if (current != null && current.getAmplifier() > maxAmplifier) {
            entity.removePotionEffect(potion);
        }
        // Merges into the current Viral like SRP's own stacking would, keeping the longer duration.
        entity.addPotionEffect(new PotionEffect(potion, added.getDuration(), maxAmplifier,
                added.getIsAmbient(), added.doesShowParticles()));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase != TickEvent.Phase.END || player.world.isRemote || player.ticksExisted % CLAMP_INTERVAL != 0) {
            return;
        }
        Potion potion = getViral();
        if (potion == null) {
            return;
        }
        PotionEffect active = player.getActivePotionEffect(potion);
        if (active == null) {
            return;
        }
        int maxAmplifier = getMaxAmplifier(player);
        if (active.getAmplifier() > maxAmplifier) {
            // addPotionEffect never lowers an amplifier (it only merges upward),
            // so the old effect has to go first. The remaining duration is kept.
            player.removePotionEffect(potion);
            player.addPotionEffect(new PotionEffect(potion, active.getDuration(), maxAmplifier,
                    active.getIsAmbient(), active.doesShowParticles()));
        }
    }

    /** The highest Viral amplifier this entity may have, or {@link #NO_CAP}. Only players wear baubles. */
    private static int getMaxAmplifier(EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) {
            return NO_CAP;
        }
        EntityPlayer player = (EntityPlayer) entity;
        ForgeConfigHandler.ServerConfig.BaubleConfig config = ForgeConfigHandler.server.baubles;
        int max = NO_CAP;
        if (isWearing(player, ModRegistry.VIRAL_RING)) {
            max = config.viralRingMaxLevel - 1;
        }
        if (isWearing(player, getPoisonStone())) {
            max = Math.min(max, config.poisonStoneMaxViralLevel - 1);
        }
        return max;
    }

    private static boolean isWearing(EntityPlayer player, @Nullable Item bauble) {
        return bauble != null && BaublesApi.isBaubleEquipped(player, bauble) >= 0;
    }

    /** SRParasites' Viral potion, or null when it isn't installed. */
    @Nullable
    private static Potion getViral() {
        if (viral == null) {
            viral = ForgeRegistries.POTIONS.getValue(VIRAL_ID);
        }
        return viral;
    }

    /** Trinkets and Baubles' Poison Stone, or null when it isn't installed. */
    @Nullable
    public static Item getPoisonStone() {
        // The item registry hands back air for a missing name rather than null.
        if (poisonStone == null && ForgeRegistries.ITEMS.containsKey(POISON_STONE_ID)) {
            poisonStone = ForgeRegistries.ITEMS.getValue(POISON_STONE_ID);
        }
        return poisonStone;
    }
}
