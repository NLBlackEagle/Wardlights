package wardlights.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import wardlights.lantern.LanternUtil;

/**
 * Makes a worn lantern (Baubles slot) or one stored in a Tool Belt glow with
 * OptiFine's dynamic lights, like a held one.
 *
 * <p>OptiFine's {@code DynamicLights.getLightLevel(Entity)} only looks at a
 * living entity's main hand, off hand and helmet, ending in
 * {@code Math.max(Math.max(main, off), helmet)}. This raises that final value
 * (the second {@code Math.max}) to the lantern light level when the player has
 * an active lantern. Hooking only that line keeps OptiFine's earlier
 * early-outs intact (spectators, "Dynamic Hand Light" off for yourself).
 *
 * <p>{@code @Pseudo}: OptiFine isn't on the compile classpath and may not be
 * installed; the mixin is simply skipped then. remap = false: OptiFine's own
 * names aren't obfuscated, and {@code Math.max} isn't a Minecraft method.
 */
@Pseudo
@Mixin(targets = "net.optifine.DynamicLights", remap = false)
public abstract class OptiFineDynamicLightsMixin {

    @ModifyExpressionValue(method = "getLightLevel(Lnet/minecraft/entity/Entity;)I",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I", ordinal = 1))
    private static int wardlights$carriedLanternLight(int level, @Local(argsOnly = true) Entity entity) {
        if (entity instanceof EntityPlayer && !LanternUtil.getActiveLantern((EntityPlayer) entity).isEmpty()) {
            return Math.max(level, LanternUtil.getLanternLightLevel());
        }
        return level;
    }
}
