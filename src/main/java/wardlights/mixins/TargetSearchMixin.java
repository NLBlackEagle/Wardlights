package wardlights.mixins;

import com.google.common.base.Predicate;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import wardlights.handlers.PaleLanternHandler;

/**
 * Pale Lantern: a mob's usual "find the nearest target" search skips players it's
 * ignoring, so it never targets them at all. Dropping the target afterwards
 * (PaleLanternHandler's event) would make pale mobs play their target and calm
 * sounds and blink their eyes every time the AI tried again.
 *
 * <p>shouldExecute searches players with World#getNearestAttackablePlayer, or any
 * other target class with World#getEntitiesWithinAABB; both get the filter wrapped.
 * Extends EntityAITarget so its taskOwner field (the mob) can be used here.
 */
@Mixin(EntityAINearestAttackableTarget.class)
public abstract class TargetSearchMixin extends EntityAITarget {

    private TargetSearchMixin(EntityCreature creature, boolean checkSight) {
        super(creature, checkSight);
    }

    @ModifyArg(method = "shouldExecute", index = 6,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getNearestAttackablePlayer(DDDDDLcom/google/common/base/Function;"
                            + "Lcom/google/common/base/Predicate;)Lnet/minecraft/entity/player/EntityPlayer;"))
    private Predicate<EntityPlayer> wardlights$skipIgnoredPlayers(Predicate<EntityPlayer> selector) {
        EntityCreature mob = taskOwner;
        return player -> (selector == null || selector.apply(player))
                && !PaleLanternHandler.isIgnoring(mob, player);
    }

    @ModifyArg(method = "shouldExecute", index = 2,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getEntitiesWithinAABB(Ljava/lang/Class;"
                            + "Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"))
    private Predicate<EntityLivingBase> wardlights$skipIgnoredTargets(Predicate<EntityLivingBase> selector) {
        EntityCreature mob = taskOwner;
        return target -> (selector == null || selector.apply(target))
                && !(target instanceof EntityPlayer && PaleLanternHandler.isIgnoring(mob, (EntityPlayer) target));
    }
}
