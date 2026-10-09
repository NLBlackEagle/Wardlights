package wardlights.handlers;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import wardlights.Wardlights;
import wardlights.lantern.LanternType;
import wardlights.util.LanternUtil;

/**
 * Vapula Lantern: faster mining and extra Fortune while the ward is active, and
 * allies: mobs that Lycanites' mining spawners spawn for a player
 * whose Vapula Lantern ward is active become that player's temporary allies (set
 * up by the Lycanites mixins, see LycanitesCompat#makeAlly). Here they're marked,
 * drop nothing, give no XP, and their owner can't hurt them while carrying the lantern.
 *
 * <p>Only vanilla types here, so this is harmless without Lycanites.
 */
@Mod.EventBusSubscriber(modid = Wardlights.MODID)
public class VapulaLanternHandler {

    /** Mob data (Forge's persistent entity data): the UUID of the player it's an ally of. */
    private static final String ALLY_OWNER_TAG = Wardlights.MODID + ":vapula_ally_of";

    /**
     * The player whose mining spawn Lycanites is placing right now, while their
     * ward is active; null otherwise. Set and cleared around Spawner#doSpawn by the
     * mixin (it all happens on the server thread, in one go).
     */
    @Nullable
    public static EntityPlayer pendingAllyOwner;

    public static void markAlly(Entity mob, EntityPlayer owner) {
        mob.getEntityData().setString(ALLY_OWNER_TAG, owner.getUniqueID().toString());
    }

    public static boolean isAlly(Entity mob) {
        return mob.getEntityData().hasKey(ALLY_OWNER_TAG);
    }

    private static boolean isAllyOf(Entity mob, EntityPlayer player) {
        return player.getUniqueID().toString().equals(mob.getEntityData().getString(ALLY_OWNER_TAG));
    }

    /** Lycanites minions already skip their own drops; this also covers loot tables and other mods' extra drops. */
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (isAlly(event.getEntityLiving())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        if (isAlly(event.getEntityLiving())) {
            event.setCanceled(true);
        }
    }

    /**
     * True while the player's Vapula ward is active. Read from their WardLight
     * effect rather than {@link LanternUtil#hasWardedLantern}: the current lantern
     * is only known on the server, but effects are synced to the player's client,
     * and mining speed has to be worked out the same on both sides (the client
     * shows the cracks and decides when the block breaks, the server checks it).
     */
    private static boolean hasVapulaWard(EntityPlayer player) {
        return player.isPotionActive(ModRegistry.WARDLIGHTS.get(LanternType.VAPULA));
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        double multiplier = ForgeConfigHandler.server.lanterns.vapulaMiningSpeed;
        if (multiplier > 1 && hasVapulaWard(event.getEntityPlayer())) {
            event.setNewSpeed((float) (event.getNewSpeed() * multiplier));
        }
    }

    /**
     * Fortune: the drops are rolled again with the bonus added to the tool's own
     * Fortune. Runs first (HIGHEST) so other mods' extra drops get added on top
     * instead of being rolled away. Blocks with a tile entity are left alone, since
     * theirs is already gone here and their drops may depend on it.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
        EntityPlayer player = event.getHarvester();
        int bonus = ForgeConfigHandler.server.lanterns.vapulaFortuneBonus;
        IBlockState state = event.getState();
        if (player == null || event.getWorld().isRemote || bonus <= 0 || event.isSilkTouching()
                || state.getBlock().hasTileEntity(state) || !hasVapulaWard(player)) {
            return;
        }
        NonNullList<ItemStack> drops = NonNullList.create();
        state.getBlock().getDrops(drops, event.getWorld(), event.getPos(), state, event.getFortuneLevel() + bonus);
        event.getDrops().clear();
        event.getDrops().addAll(drops);
    }

    /** The owner can't hurt their allies (melee or projectile) while the Vapula Lantern is their current lantern. */
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        Entity attacker = event.getSource().getTrueSource();
        if (attacker instanceof EntityPlayer && isAllyOf(event.getEntityLiving(), (EntityPlayer) attacker)
                && LanternUtil.hasLantern((EntityPlayer) attacker, LanternType.VAPULA)) {
            event.setCanceled(true);
        }
    }
}
