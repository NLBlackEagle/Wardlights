package wardlights.mixins;

import com.lycanitesmobs.core.spawner.trigger.BlockSpawnTrigger;
import com.lycanitesmobs.core.spawner.trigger.OreBlockSpawnTrigger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import wardlights.compat.LycanitesCompat;

/**
 * Vapula Lantern: mining ores and gems triggers Lycanites' Geonach / Vapula
 * spawners more often while the miner's Vapula ward is active.
 *
 * <p>onBlockTriggered reads this.chance to roll the spawn; here that read gives
 * the lantern's chance instead, when it's higher. The trailing parameters are
 * onBlockTriggered's own arguments, captured so the miner is known.
 *
 * <p>remap = false: all of Lycanites' names, none obfuscated.
 */
@Mixin(value = BlockSpawnTrigger.class, remap = false)
public abstract class LycanitesMiningSpawnChanceMixin {

    @Redirect(method = "onBlockTriggered",
            at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
                    target = "Lcom/lycanitesmobs/core/spawner/trigger/BlockSpawnTrigger;chance:D"))
    private double wardlights$vapulaChance(BlockSpawnTrigger trigger, World world, EntityPlayer player, BlockPos pos,
            IBlockState state, int chain, int fortune) {
        if (trigger instanceof OreBlockSpawnTrigger) {
            return LycanitesCompat.getMiningSpawnChance((OreBlockSpawnTrigger) trigger, player, trigger.chance);
        }
        return trigger.chance;
    }
}
