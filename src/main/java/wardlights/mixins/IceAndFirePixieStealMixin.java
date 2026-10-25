package wardlights.mixins;

import com.github.alexthe666.iceandfire.entity.ai.PixieAISteal;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wardlights.lantern.LanternType;
import wardlights.lantern.LanternUtil;

/**
 * Pixie Lantern: Ice and Fire pixies won't steal from its carrier, day or night.
 *
 * <p>The steal AI picks the closest player within 10 blocks in shouldExecute;
 * if that player carries a Pixie Lantern, or stands near a placed one, the pixie leaves them alone.
 * shouldContinueExecuting calls shouldExecute, so a steal already under way stops too.
 *
 * <p>remap = false: the class and its fields are Ice and Fire's own, never obfuscated.
 */
@Mixin(value = PixieAISteal.class, remap = false)
public abstract class IceAndFirePixieStealMixin {

    @Shadow
    private EntityPlayer temptingPlayer;

    /** remap = true: shouldExecute overrides Minecraft's EntityAIBase method, so its name is obfuscated. */
    @Inject(method = "shouldExecute", at = @At("RETURN"), cancellable = true, remap = true)
    private void wardlights$pixieLantern(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && temptingPlayer != null
                && LanternUtil.hasLantern(temptingPlayer, LanternType.PIXIE)) {
            cir.setReturnValue(false);
        }
    }
}
