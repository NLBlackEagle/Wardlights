package wardlights.mixins;

import com.dhanantry.scapeandrunparasites.entity.ai.EntityAINearestAttackableTargetStatus;
import com.google.common.base.Predicate;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import wardlights.lantern.handler.ParasiteLanternHandler;

/**
 * Parasite Lantern: a parasite's target search skips players it's ignoring, so it
 * picks someone else instead. ParasiteLanternHandler drops ignored targets set any
 * other way; this just keeps the main AI from choosing them over and over.
 *
 * <p>shouldExecute searches players with getNearest, or any other target class
 * with World#getEntitiesWithinAABB; both get the filter wrapped.
 *
 * <p>Extends EntityAITarget so its taskOwner field (the parasite) can be used here.
 * remap = false: SRParasites' names aren't obfuscated, but shouldExecute overrides
 * Minecraft's EntityAIBase method and World#getEntitiesWithinAABB is Minecraft's,
 * so those two are remapped.
 */
@Mixin(value = EntityAINearestAttackableTargetStatus.class, remap = false)
public abstract class SRParasitesTargetMixin extends EntityAITarget {

    private SRParasitesTargetMixin(EntityCreature creature, boolean checkSight) {
        super(creature, checkSight);
    }

    @ModifyArg(method = "shouldExecute", remap = true, index = 5,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lcom/dhanantry/scapeandrunparasites/entity/ai/EntityAINearestAttackableTargetStatus;"
                            + "getNearest(DDDDDLcom/google/common/base/Predicate;)Lnet/minecraft/entity/player/EntityPlayer;"))
    private Predicate<EntityPlayer> wardlights$skipIgnoredPlayers(Predicate<EntityPlayer> selector) {
        EntityCreature parasite = taskOwner;
        return player -> (selector == null || selector.apply(player))
                && !ParasiteLanternHandler.isIgnoring(parasite, player);
    }

    @ModifyArg(method = "shouldExecute", remap = true, index = 2,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getEntitiesWithinAABB(Ljava/lang/Class;"
                            + "Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"))
    private Predicate<EntityLivingBase> wardlights$skipIgnoredTargets(Predicate<EntityLivingBase> selector) {
        EntityCreature parasite = taskOwner;
        return target -> (selector == null || selector.apply(target))
                && !(target instanceof EntityPlayer && ParasiteLanternHandler.isIgnoring(parasite, (EntityPlayer) target));
    }
}
