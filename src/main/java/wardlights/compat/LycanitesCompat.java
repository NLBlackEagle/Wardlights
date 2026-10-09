package wardlights.compat;

import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.spawner.trigger.OreBlockSpawnTrigger;
import com.lycanitesmobs.core.spawner.trigger.SpawnTrigger;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import wardlights.handlers.ForgeConfigHandler;
import wardlights.handlers.ForgeConfigHandler.ServerConfig.LanternConfig;
import wardlights.handlers.VapulaLanternHandler;
import wardlights.lantern.LanternType;
import wardlights.util.LanternUtil;

/**
 * Lycanites Mobs side of the Vapula Lantern. Only called from the Lycanites mixins,
 * which only load with Lycanites installed, so its classes are always there.
 */
public final class LycanitesCompat {

    private LycanitesCompat() {
    }

    /** True when this is a mining spawn (Lycanites' ore / gem spawners) for a player whose Vapula ward is active. */
    public static boolean isVapulaMiningSpawn(SpawnTrigger trigger, EntityPlayer player) {
        return trigger instanceof OreBlockSpawnTrigger && player != null
                && LanternUtil.hasWardedLantern(player, LanternType.VAPULA);
    }

    /** The mining spawn chance for this player: the lantern's configured one when it's higher. */
    public static double getMiningSpawnChance(OreBlockSpawnTrigger trigger, EntityPlayer player, double chance) {
        if (!isVapulaMiningSpawn(trigger, player)) {
            return chance;
        }
        LanternConfig config = ForgeConfigHandler.server.lanterns;
        double lanternChance = Math.max(trigger.gems ? config.vapulaGemSpawnChance : 0,
                trigger.ores ? config.vapulaOreSpawnChance : 0);
        return Math.max(chance, lanternChance);
    }

    /**
     * Turns a freshly spawned mob into the player's temporary ally, the same way
     * Lycanites sets up summoned minions: minion, temporary, owned by the player.
     * Lycanites fixates mining spawns on the miner, so that's cleared too.
     */
    public static void makeAlly(EntityLiving mob, EntityPlayer owner) {
        if (!(mob instanceof BaseCreatureEntity)) {
            return;
        }
        BaseCreatureEntity creature = (BaseCreatureEntity) mob;
        creature.setMinion(true);
        creature.setTemporary(ForgeConfigHandler.server.lanterns.vapulaAllySeconds * 20);
        if (creature instanceof TameableCreatureEntity) {
            ((TameableCreatureEntity) creature).setPlayerOwner(owner);
        }
        creature.setFixateTarget(null);
        creature.setAttackTarget(null);
        VapulaLanternHandler.markAlly(creature, owner);
    }
}
