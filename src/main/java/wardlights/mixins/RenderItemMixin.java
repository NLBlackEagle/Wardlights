package wardlights.mixins;

import java.util.function.IntSupplier;

import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wardlights.items.ItemMaterial;

/**
 * Recolors the enchantment glint per item. Vanilla's glint texture is grayscale
 * and {@code renderEffect} tints both of its scrolling layers with one hardcoded
 * purple; that method only gets the model, so the stack being drawn is
 * remembered from {@code renderItem}, which calls it.
 */
@Mixin(RenderItem.class)
public abstract class RenderItemMixin {

    /** Vanilla's glint tint, 0xFF8040CC. */
    @Unique
    private static final int wardlights$VANILLA_GLINT = -8372020;

    @Unique
    private ItemStack wardlights$renderingStack = ItemStack.EMPTY;

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/IBakedModel;)V",
            at = @At("HEAD"))
    private void wardlights$rememberStack(ItemStack stack, IBakedModel model, CallbackInfo ci) {
        wardlights$renderingStack = stack;
    }

    /**
     * require = 0: if another mod (e.g. OptiFine) rewrites the glint code, items
     * just keep the purple glint instead of the game crashing.
     */
    @ModifyConstant(method = "renderEffect", constant = @Constant(intValue = wardlights$VANILLA_GLINT), require = 0)
    private int wardlights$glintColor(int vanillaColor) {
        Item item = wardlights$renderingStack.getItem();
        if (item instanceof ItemMaterial) {
            IntSupplier glintColor = ((ItemMaterial) item).getGlintColor();
            if (glintColor != null) {
                return 0xFF000000 | glintColor.getAsInt();
            }
        }
        return vanillaColor;
    }
}
