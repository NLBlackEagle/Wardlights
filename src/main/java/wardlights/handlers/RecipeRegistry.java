package wardlights.handlers;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import wardlights.Wardlights;

/**
 * Recipes built from {@link ForgeConfigHandler.ServerConfig} instead of static
 * JSON, so pack authors can reassign ingredients per crafting-grid slot
 * without needing a datapack/resource override.
 */
@Mod.EventBusSubscriber(modid = Wardlights.MODID)
public class RecipeRegistry {

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        event.getRegistry().register(buildAegisOfSacrificeRecipe());
    }

    private static ShapedRecipes buildAegisOfSacrificeRecipe() {
        String[] slots = ForgeConfigHandler.server.aegisOfSacrificeRecipe;
        NonNullList<Ingredient> ingredients = NonNullList.withSize(9, Ingredient.EMPTY);
        for (int i = 0; i < 9 && i < slots.length; i++) {
            ingredients.set(i, resolveIngredient(slots[i]));
        }

        ShapedRecipes recipe = new ShapedRecipes(Wardlights.MODID, 3, 3, ingredients,
                new ItemStack(ModRegistry.AEGIS_OF_SACRIFICE));
        recipe.setRegistryName(Wardlights.MODID, "aegis_of_sacrifice");
        return recipe;
    }

    private static Ingredient resolveIngredient(String itemId) {
        if (itemId == null || itemId.trim().isEmpty()) {
            return Ingredient.EMPTY;
        }
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId.trim()));
        if (item == null) {
            Wardlights.LOGGER.warn("Aegis of Sacrifice recipe: unknown item '{}', treating slot as empty", itemId);
            return Ingredient.EMPTY;
        }
        return Ingredient.fromStacks(new ItemStack(item));
    }
}
