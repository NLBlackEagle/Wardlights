package wardlights.mixins;

import gigaherz.toolbelt.client.LayerToolBelt;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import wardlights.items.ItemLanternBase;

/**
 * Makes lanterns on Tool Belt's belt hang straight down from the hip.
 *
 * <p>Tool Belt draws belt items with their third-person hand pose, tilted 40°
 * forward. A lantern's hand pose already tilts it 67°, so on the belt it ends
 * up sticking out sideways. For lanterns this swaps in the upright item-frame
 * pose, undoes the belt tilt, and drops the lantern so its handle sits at the
 * belt line.
 *
 * <p>remap = false: Tool Belt's own method names aren't obfuscated, but the
 * Minecraft method being redirected is, so only the {@code @At} is remapped.
 */
@Mixin(value = LayerToolBelt.class, remap = false)
public abstract class ToolBeltMixin {

    /** How far below the belt line the lantern's origin sits, in item units (16 = one block before belt scale). */
    @Unique
    private static final float wardlights$HANG_DROP = 6.5F / 16F;

    /** Pushes the lantern forward so it hangs in front of the leg instead of clipping into it. */
    @Unique
    private static final float wardlights$HANG_FORWARD = -1F / 16F;

    /** Degrees the lantern's bottom swings out sideways, away from the thigh, pivoting on its handle. */
    @Unique
    private static final float wardlights$SWING_OUT = 15.0F;

    @Redirect(method = "renderHeldItem",
            at = @At(value = "INVOKE", remap = true,
                    target = "Lnet/minecraft/client/renderer/ItemRenderer;renderItemSide(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;Z)V"))
    private void wardlights$hangLantern(ItemRenderer renderer, EntityLivingBase entity, ItemStack stack,
            TransformType transform, boolean leftHanded) {
        if (!(stack.getItem() instanceof ItemLanternBase)) {
            renderer.renderItemSide(entity, stack, transform, leftHanded);
            return;
        }
        // Undo Tool Belt's forward tilt.
        GlStateManager.rotate(-40.0F, 1.0F, 0.0F, 0.0F);
        // Player model space is upside down (y points down); flip back so the lantern is upright.
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        // Now y is up and -z is the player's front, with the origin at the belt line.
        // Tilt around the forward axis here, so the handle stays on the belt and the bottom
        // swings out. The flip mirrored x, so the left hip (leftHanded) swings toward +x.
        GlStateManager.rotate(leftHanded ? wardlights$SWING_OUT : -wardlights$SWING_OUT, 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(0.0F, -wardlights$HANG_DROP, -wardlights$HANG_FORWARD);
        renderer.renderItemSide(entity, stack, TransformType.FIXED, false);
    }
}
