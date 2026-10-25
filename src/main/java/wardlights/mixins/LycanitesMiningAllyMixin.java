package wardlights.mixins;

import com.lycanitesmobs.ExtendedWorld;
import com.lycanitesmobs.core.spawner.MobSpawn;
import com.lycanitesmobs.core.spawner.Spawner;
import com.lycanitesmobs.core.spawner.trigger.SpawnTrigger;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wardlights.compat.LycanitesCompat;
import wardlights.lantern.handler.VapulaLanternHandler;

/**
 * Vapula Lantern: mobs a mining spawner spawns for a player whose Vapula ward is
 * active become that player's temporary allies.
 *
 * <p>doSpawn knows the trigger (was it mining?) but spawnEntity is where each mob
 * is placed, so doSpawn notes the player for the length of the call and
 * spawnEntity turns each mob into their ally once Lycanites has set it up
 * (MobSpawn#onSpawned fixates it on the miner, which makeAlly undoes).
 *
 * <p>remap = false: all of Lycanites' names, none obfuscated.
 */
@Mixin(value = Spawner.class, remap = false)
public abstract class LycanitesMiningAllyMixin {

    @Inject(method = "doSpawn", at = @At("HEAD"))
    private void wardlights$startSpawn(World world, EntityPlayer player, SpawnTrigger trigger, BlockPos pos,
            int level, int chain, CallbackInfoReturnable<Boolean> cir) {
        VapulaLanternHandler.pendingAllyOwner = LycanitesCompat.isVapulaMiningSpawn(trigger, player) ? player : null;
    }

    @Inject(method = "doSpawn", at = @At("RETURN"))
    private void wardlights$endSpawn(World world, EntityPlayer player, SpawnTrigger trigger, BlockPos pos,
            int level, int chain, CallbackInfoReturnable<Boolean> cir) {
        VapulaLanternHandler.pendingAllyOwner = null;
    }

    @Inject(method = "spawnEntity",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER,
                    target = "Lcom/lycanitesmobs/core/spawner/MobSpawn;onSpawned(Lnet/minecraft/entity/EntityLiving;"
                            + "Lnet/minecraft/entity/player/EntityPlayer;)V"))
    private void wardlights$makeAlly(World world, ExtendedWorld extendedWorld, EntityLiving entity, int level,
            MobSpawn mobSpawn, EntityPlayer player, int chain, CallbackInfo ci) {
        EntityPlayer owner = VapulaLanternHandler.pendingAllyOwner;
        if (owner != null) {
            LycanitesCompat.makeAlly(entity, owner);
        }
    }
}
