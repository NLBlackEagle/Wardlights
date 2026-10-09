package wardlights.handlers;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import wardlights.Wardlights;
import wardlights.handlers.ForgeConfigHandler.ServerConfig.LanternConfig;
import wardlights.lantern.LanternType;
import wardlights.util.LanternUtil;
import wardlights.util.TargetMemory;

/**
 * Pale Lantern: the configured pale mobs ignore its carrier while the ward is
 * active, unless they've been hit by them, and hostile mobs may catch Pale Lung
 * from it: coming near a placed one, or fighting its carrier.
 *
 * <p>Mobs and Pale Lung are looked up by id, so nothing here touches a Pale Bloom
 * class and it's harmless without those mods.
 */
@Mod.EventBusSubscriber(modid = Wardlights.MODID)
public class PaleLanternHandler {

    private static final ResourceLocation PALE_LUNG_ID = new ResourceLocation("dynamictreespalebloom", "pale_lung");

    @Nullable
    private static Potion paleLung;

    /** The ignored entity ids, parsed from the config array they came from (rebuilt when the config changes it). */
    private static String[] parsedFrom;
    private static Set<ResourceLocation> ignoredEntities = new HashSet<>();

    /**
     * Catches the ways of picking a target the AI mixin doesn't (other mods' AI,
     * commands...). Dropping it here can make a pale mob play its "calms" sound,
     * which is why the mixin stops the usual way before it gets this far.
     * A mob targeting the player it was hit by remembers them from now on.
     */
    @SubscribeEvent
    public static void onSetAttackTarget(LivingSetAttackTargetEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!(event.getTarget() instanceof EntityPlayer) || !(entity instanceof EntityLiving) || entity.world.isRemote) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getTarget();
        if (entity.getRevengeTarget() == player) {
            if (isIgnoredEntity(entity)) {
                TargetMemory.markNoticed(entity, player);
            }
        } else if (isIgnoring(entity, player)) {
            ((EntityLiving) entity).setAttackTarget(null);
        }
    }

    /**
     * True when this mob should leave the player alone: it's on the ignore list,
     * a Pale Lantern's ward is active for the player, and the player hasn't hit it.
     */
    public static boolean isIgnoring(EntityLivingBase mob, EntityPlayer player) {
        return isIgnoredEntity(mob)
                && mob.getRevengeTarget() != player
                && !TargetMemory.hasNoticed(mob, player)
                && LanternUtil.hasWardedLantern(player, LanternType.PALE);
    }

    private static boolean isIgnoredEntity(Entity entity) {
        String[] configured = ForgeConfigHandler.server.lanterns.paleIgnoredEntities;
        if (configured != parsedFrom) {
            Set<ResourceLocation> ids = new HashSet<>();
            for (String id : configured) {
                ids.add(new ResourceLocation(id.trim()));
            }
            ignoredEntities = ids;
            parsedFrom = configured;
        }
        ResourceLocation id = EntityList.getKey(entity);
        return id != null && ignoredEntities.contains(id);
    }

    /**
     * Fights involving a carrier whose Pale Lantern ward is active: a hostile mob
     * hitting them, or being hit by them (melee or projectile), may catch Pale Lung.
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LanternConfig config = ForgeConfigHandler.server.lanterns;
        EntityLivingBase hurt = event.getEntityLiving();
        Entity attacker = event.getSource().getTrueSource();
        if (hurt.world.isRemote) {
            return;
        }
        if (hurt instanceof EntityPlayer && attacker instanceof EntityLivingBase
                && hasCarriedPaleWard((EntityPlayer) hurt)) {
            tryPaleLung((EntityLivingBase) attacker, config.paleLungHitByChance);
        }
        if (attacker instanceof EntityPlayer && hasCarriedPaleWard((EntityPlayer) attacker)) {
            tryPaleLung(hurt, config.paleLungHittingChance);
        }
    }

    private static boolean hasCarriedPaleWard(EntityPlayer player) {
        return LanternUtil.hasCarriedWardedLantern(player, LanternType.PALE);
    }

    /**
     * Called by a working placed Pale Lantern with the hostile mobs now within its
     * radius and the ids of those that already were last time; only newcomers roll.
     * Returns the ids to remember for next time.
     */
    public static Set<Integer> rollNewcomers(World world, BlockPos lanternPos, Set<Integer> alreadyInside) {
        double chance = ForgeConfigHandler.server.lanterns.paleLungPlacedChance;
        AxisAlignedBB area = new AxisAlignedBB(lanternPos).grow(ForgeConfigHandler.server.lanterns.lanternRadius);
        Set<Integer> inside = new HashSet<>();
        for (EntityLivingBase mob : world.getEntitiesWithinAABB(EntityLivingBase.class, area,
                living -> living instanceof IMob && LanternUtil.isWithinLanternRadius(lanternPos, new BlockPos(living)))) {
            inside.add(mob.getEntityId());
            if (!alreadyInside.contains(mob.getEntityId())) {
                tryPaleLung(mob, chance);
            }
        }
        return inside;
    }

    /** Gives a hostile mob Pale Lung with the given chance. */
    private static void tryPaleLung(EntityLivingBase mob, double chance) {
        LanternConfig config = ForgeConfigHandler.server.lanterns;
        Potion potion = getPaleLung();
        if (potion != null && mob instanceof IMob && chance > 0 && mob.getRNG().nextDouble() < chance) {
            mob.addPotionEffect(new PotionEffect(potion, config.paleLungSeconds * 20, config.paleLungLevel - 1));
        }
    }

    /** Dynamic Trees Pale Bloom's Pale Lung, or null when it isn't installed. */
    @Nullable
    public static Potion getPaleLung() {
        if (paleLung == null) {
            paleLung = ForgeRegistries.POTIONS.getValue(PALE_LUNG_ID);
        }
        return paleLung;
    }
}
