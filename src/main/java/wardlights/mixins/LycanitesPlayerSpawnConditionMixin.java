package wardlights.mixins;

import com.lycanitesmobs.core.spawner.condition.PlayerSpawnCondition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import wardlights.lantern.LanternType;
import wardlights.lantern.LanternUtil;

/**
 * Regular Lantern: its light counts for Lycanites' player spawn conditions.
 *
 * <p>Lycanites' darkness spawner (Grue, Darkling, Shade) builds up while the
 * player stands in light level 3 or lower and resets at 4 or higher, but it
 * reads the world's real light, which a carried lantern doesn't change. Here
 * the player counts as standing in at least the lantern's own light while a
 * Regular Lantern's ward is active (carried, or a working one placed nearby),
 * so the darkness never builds up around them.
 *
 * <p>remap = false: Lycanites' names aren't obfuscated, but World#getLight is,
 * so only the {@code @At} is remapped.
 */
@Mixin(value = PlayerSpawnCondition.class, remap = false)
public abstract class LycanitesPlayerSpawnConditionMixin {

    /** The trailing parameters are isMet's own arguments, captured so the player is known. */
    @Redirect(method = "isMet",
            at = @At(value = "INVOKE", remap = true,
                    target = "Lnet/minecraft/world/World;getLight(Lnet/minecraft/util/math/BlockPos;)I"))
    private int wardlights$lanternLight(World world, BlockPos pos, World isMetWorld, EntityPlayer player,
            BlockPos spawnPos) {
        int light = world.getLight(pos);
        if (player != null && LanternUtil.hasWardedLantern(player, LanternType.REGULAR)) {
            return Math.max(light, LanternUtil.getLanternLightLevel());
        }
        return light;
    }
}
