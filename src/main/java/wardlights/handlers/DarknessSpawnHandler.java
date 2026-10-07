package wardlights.handlers;

import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import wardlights.Wardlights;
import wardlights.util.LanternUtil;

/**
 * Cancels natural spawns of vanilla hostiles ("darkness entities" for now —
 * content-mod mobs like Lycanites' Darkness-type spawns come later, likely
 * via their own mixin since they don't all route through this event) within
 * range of a player whose lantern is active (worn or held, and the
 * darkness / underground condition holds, see {@link LanternUtil}). Independent of the lanterns' visual
 * glow (OptiFine's dynamic_lights.properties, assets/wardlights/optifine/)
 * — this is the real, server-authoritative mechanic; the glow is just
 * visual flavor and only shows for players with OptiFine installed.
 */
@Mod.EventBusSubscriber(modid = Wardlights.MODID)
public class DarknessSpawnHandler {

    @SubscribeEvent
    public static void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
        if (!(event.getEntityLiving() instanceof IMob)) {
            return;
        }

        World world = event.getWorld();
        double radius = ForgeConfigHandler.server.lanterns.spawnSuppressionRadius;
        double radiusSq = radius * radius;

        for (EntityPlayer player : world.playerEntities) {
            if (!LanternUtil.isWardActive(player)) {
                continue;
            }

            double dx = player.posX - event.getX();
            double dy = player.posY - event.getY();
            double dz = player.posZ - event.getZ();
            if (dx * dx + dy * dy + dz * dz <= radiusSq) {
                event.setResult(Event.Result.DENY);
                return;
            }
        }
    }
}
