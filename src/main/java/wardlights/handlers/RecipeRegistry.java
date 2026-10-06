package wardlights.handlers;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import wardlights.Wardlights;

/**
 * Recipes built from {@link ForgeConfigHandler.ServerConfig} instead of static
 * JSON, so pack authors can reassign ingredients per crafting-grid slot
 * without needing a datapack/resource override. Each recipe is one
 * {@code {1, 2, 3, 4, 5, 6, 7, 8, 9}} string of item ids.
 */
@Mod.EventBusSubscriber(modid = Wardlights.MODID)
public class RecipeRegistry {

    private static final int GRID_SLOTS = 9;

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        if (ModRegistry.hasChestBaubles()) {
            register(event, buildShapedRecipe("bulwark_of_valor", "Bulwark of Valor",
                    ForgeConfigHandler.server.bulwarkOfValorRecipe, ModRegistry.BULWARK_OF_VALOR));
            register(event, buildShapedRecipe("aegis_of_sacrifice", "Aegis of Sacrifice",
                    ForgeConfigHandler.server.aegisOfSacrificeRecipe, ModRegistry.AEGIS_OF_SACRIFICE));
        }
        if (ForgeConfigHandler.server.dormantBadgeRecipeEnabled && ModRegistry.DORMANT_BADGE != null) {
            register(event, buildShapedRecipe("dormant_badge", "Dormant Badge",
                    ForgeConfigHandler.server.dormantBadgeRecipe, ModRegistry.DORMANT_BADGE));
        }
        if (ForgeConfigHandler.server.badgeOfBraveryRecipeEnabled) {
            register(event, buildShapelessRecipe("badge_of_bravery", "Badge of Bravery",
                    ForgeConfigHandler.server.badgeOfBraveryRecipe, ModRegistry.BADGE_OF_BRAVERY));
        }
    }

    private static void register(RegistryEvent.Register<IRecipe> event, IRecipe recipe) {
        if (recipe != null) {
            event.getRegistry().register(recipe);
        }
    }

    /**
     * Null (recipe skipped) if any slot names an item that doesn't exist, e.g.
     * because its mod isn't installed; dropping just that ingredient would
     * quietly make the recipe cheaper.
     */
    private static ShapedRecipes buildShapedRecipe(String name, String displayName, String config, Item result) {
        String[] slots = splitSlots(config);
        if (slots.length != GRID_SLOTS) {
            Wardlights.LOGGER.warn("{} recipe: expected {} slots but found {} in '{}'; missing slots are empty, "
                    + "extra ones ignored", displayName, GRID_SLOTS, slots.length, config);
        }
        NonNullList<Ingredient> ingredients = NonNullList.withSize(GRID_SLOTS, Ingredient.EMPTY);
        for (int i = 0; i < GRID_SLOTS && i < slots.length; i++) {
            Ingredient ingredient = resolveIngredient(slots[i], displayName);
            if (ingredient == null) {
                Wardlights.LOGGER.warn("{} recipe disabled: slot {} ('{}') could not be resolved", displayName, i + 1,
                        slots[i]);
                return null;
            }
            ingredients.set(i, ingredient);
        }

        ShapedRecipes recipe = new ShapedRecipes(Wardlights.MODID, 3, 3, ingredients, new ItemStack(result));
        recipe.setRegistryName(Wardlights.MODID, name);
        return recipe;
    }

    /**
     * Like {@link #buildShapedRecipe}, but the ingredients go anywhere in the
     * grid. Blank entries are ignored rather than read as empty cells.
     */
    private static ShapelessRecipes buildShapelessRecipe(String name, String displayName, String config, Item result) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (String slot : splitSlots(config)) {
            Ingredient ingredient = resolveIngredient(slot, displayName);
            if (ingredient == null) {
                Wardlights.LOGGER.warn("{} recipe disabled: '{}' could not be resolved", displayName, slot);
                return null;
            }
            if (ingredient != Ingredient.EMPTY) {
                ingredients.add(ingredient);
            }
        }
        if (ingredients.isEmpty() || ingredients.size() > GRID_SLOTS) {
            Wardlights.LOGGER.warn("{} recipe disabled: needs 1 to {} ingredients but found {} in '{}'", displayName,
                    GRID_SLOTS, ingredients.size(), config);
            return null;
        }

        ShapelessRecipes recipe = new ShapelessRecipes(Wardlights.MODID, new ItemStack(result), ingredients);
        recipe.setRegistryName(Wardlights.MODID, name);
        return recipe;
    }

    /** {@code "{a, b, , c}"} (braces optional) to its trimmed comma-separated slots. */
    private static String[] splitSlots(String config) {
        String s = config == null ? "" : config.trim();
        if (s.startsWith("{")) {
            s = s.substring(1);
        }
        if (s.endsWith("}")) {
            s = s.substring(0, s.length() - 1);
        }
        String[] slots = s.split(",", -1);
        for (int i = 0; i < slots.length; i++) {
            slots[i] = slots[i].trim();
        }
        return slots;
    }

    /**
     * {@code modid:item}, or {@code modid:item:meta} where meta may be {@code *}
     * for any; CraftTweaker-style {@code <modid:item:meta>} brackets are accepted
     * too. Blank is an empty cell; null means the item doesn't exist.
     */
    private static Ingredient resolveIngredient(String rawSlot, String displayName) {
        String slot = rawSlot;
        if (slot.startsWith("<") && slot.endsWith(">")) {
            slot = slot.substring(1, slot.length() - 1).trim();
        }
        if (slot.isEmpty() || slot.equals("minecraft:air")) {
            return Ingredient.EMPTY;
        }
        String id = slot;
        int meta = 0;
        String[] parts = slot.split(":");
        if (parts.length == 3) {
            id = parts[0] + ":" + parts[1];
            try {
                meta = parts[2].equals("*") ? OreDictionary.WILDCARD_VALUE : Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                Wardlights.LOGGER.warn("{} recipe: bad metadata in '{}'", displayName, slot);
                return null;
            }
        }
        // The item registry falls back to air for unknown ids instead of returning null.
        ResourceLocation location = new ResourceLocation(id);
        Item item = ForgeRegistries.ITEMS.containsKey(location) ? ForgeRegistries.ITEMS.getValue(location) : null;
        if (item == null) {
            Wardlights.LOGGER.warn("{} recipe: unknown item '{}'", displayName, slot);
            return null;
        }
        return Ingredient.fromStacks(new ItemStack(item, 1, meta));
    }
}
