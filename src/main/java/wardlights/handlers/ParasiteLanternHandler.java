package wardlights.handlers;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import wardlights.Wardlights;
import wardlights.handlers.ForgeConfigHandler.ServerConfig.LanternConfig;
import wardlights.lantern.LanternType;
import wardlights.util.LanternUtil;
import wardlights.util.TargetMemory;

/**
 * Parasite Lantern: SRParasites mobs tend to ignore its carrier while the ward
 * is active (darkness / underground), when SRParasites' Fear lasts shorter on
 * them too; and day and night Call of the Hive (COTH) is cleared from everything
 * around it.
 *
 * <p>Parasites are recognized by their registry namespace, so nothing here
 * touches an SRParasites class and it's harmless without the mod.
 */
@Mod.EventBusSubscriber(modid = Wardlights.MODID)
public class ParasiteLanternHandler {

    /** Same cadence as the WardLight check: twice a second. */
    private static final int CHECK_INTERVAL = 10;

    private static final ResourceLocation COTH_ID = new ResourceLocation(ModRegistry.SRPARASITES_MODID, "coth");
    private static final ResourceLocation FEAR_ID = new ResourceLocation(ModRegistry.SRPARASITES_MODID, "fear");

    @Nullable
    private static Potion coth;
    @Nullable
    private static Potion fear;

    /**
     * Catches every way a parasite can pick a target (its AI, summons, being
     * alerted by others...), since they all go through EntityLiving#setAttackTarget.
     * A target it should ignore is dropped again; any other player it targets
     * counts as noticed from now on.
     */
    @SubscribeEvent
    public static void onSetAttackTarget(LivingSetAttackTargetEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!(event.getTarget() instanceof EntityPlayer) || !(entity instanceof EntityLiving)
                || entity.world.isRemote || !isParasite(entity)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getTarget();
        if (isIgnoring(entity, player)) {
            ((EntityLiving) entity).setAttackTarget(null);
        } else {
            TargetMemory.markNoticed(entity, player);
        }
    }

    /**
     * True when this parasite should leave the player alone right now: the player
     * has a Parasite Lantern, hasn't hit it, it hasn't noticed them before, and
     * the current roll says ignore.
     */
    public static boolean isIgnoring(EntityLivingBase parasite, EntityPlayer player) {
        LanternConfig config = ForgeConfigHandler.server.lanterns;
        return config.parasiteIgnoreChance > 0
                && parasite.getRevengeTarget() != player
                && !TargetMemory.hasNoticed(parasite, player)
                && LanternUtil.hasWardedLantern(player, LanternType.PARASITE)
                && roll(parasite, player, config.parasiteRerollSeconds * 20L) < config.parasiteIgnoreChance;
    }

    public static boolean isParasite(Entity entity) {
        ResourceLocation id = EntityList.getKey(entity);
        return id != null && ModRegistry.SRPARASITES_MODID.equals(id.getNamespace());
    }

    /**
     * A number from 0 to 1 for this parasite and player that stays the same for one
     * re-roll period, then changes. It's worked out from their UUIDs and the world
     * time instead of stored, so nothing needs saving. Each pair's periods start
     * at a different moment, so parasites don't all notice someone at once.
     */
    private static double roll(Entity parasite, EntityPlayer player, long period) {
        long pair = mix(hash(parasite.getUniqueID()) ^ mix(hash(player.getUniqueID())));
        long window = (parasite.world.getTotalWorldTime() + Math.floorMod(pair, period)) / period;
        return (mix(pair + window) >>> 11) * 0x1.0p-53;
    }

    private static long hash(UUID id) {
        return id.getMostSignificantBits() * 31 + id.getLeastSignificantBits();
    }

    /** Scrambles the bits (the SplitMix64 finalizer), so nearby inputs give unrelated outputs. */
    private static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    /** True while this handler is adding the shortened Fear itself, so that one isn't shortened again. */
    private static boolean addingShortenedFear;

    /**
     * Weaker Fear while the ward is active: every way SRParasites gives Fear goes
     * through addPotionEffect, which asks this event first. The event can only
     * allow or deny, not change the duration, so the original is denied and a
     * shortened copy added instead. Server only: the client receives the result.
     */
    @SubscribeEvent
    public static void onPotionApplicable(PotionEvent.PotionApplicableEvent event) {
        Potion potion = getFear();
        PotionEffect effect = event.getPotionEffect();
        double multiplier = ForgeConfigHandler.server.lanterns.parasiteFearDuration;
        if (addingShortenedFear || potion == null || effect.getPotion() != potion || multiplier >= 1
                || !(event.getEntityLiving() instanceof EntityPlayer) || event.getEntityLiving().world.isRemote) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (!LanternUtil.hasWardedLantern(player, LanternType.PARASITE)) {
            return;
        }
        event.setResult(Event.Result.DENY);
        int duration = (int) Math.round(effect.getDuration() * multiplier);
        if (duration > 0) {
            addingShortenedFear = true;
            try {
                player.addPotionEffect(new PotionEffect(potion, duration, effect.getAmplifier(), effect.getIsAmbient(),
                        effect.doesShowParticles()));
            } finally {
                addingShortenedFear = false;
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase != TickEvent.Phase.END || player.world.isRemote || player.ticksExisted % CHECK_INTERVAL != 0) {
            return;
        }
        // Carried only (placed ones clear around themselves), and day and night, so no ward check.
        if (LanternUtil.hasLantern(player, LanternType.PARASITE)
                && LanternUtil.getActiveLanternType(player) == LanternType.PARASITE) {
            clearCoth(player.world, new BlockPos(player));
        }
    }

    /** Removes COTH from every living entity within the lantern radius of center, the carrier included. */
    public static void clearCoth(World world, BlockPos center) {
        Potion potion = getCoth();
        if (potion == null) {
            return;
        }
        AxisAlignedBB area = new AxisAlignedBB(center).grow(ForgeConfigHandler.server.lanterns.lanternRadius);
        for (EntityLivingBase entity : world.getEntitiesWithinAABB(EntityLivingBase.class, area,
                living -> living.isPotionActive(potion) && LanternUtil.isWithinLanternRadius(center, new BlockPos(living)))) {
            entity.removePotionEffect(potion);
        }
    }

    /** SRParasites' COTH potion, or null when it isn't installed. Looked up by id, so no SRParasites class is needed. */
    @Nullable
    private static Potion getCoth() {
        if (coth == null) {
            coth = ForgeRegistries.POTIONS.getValue(COTH_ID);
        }
        return coth;
    }

    /** SRParasites' Fear potion, or null when it isn't installed. */
    @Nullable
    private static Potion getFear() {
        if (fear == null) {
            fear = ForgeRegistries.POTIONS.getValue(FEAR_ID);
        }
        return fear;
    }
}
