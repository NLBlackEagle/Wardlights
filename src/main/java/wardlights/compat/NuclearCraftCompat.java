package wardlights.compat;

import nc.recipe.NCRecipes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import wardlights.Wardlights;
import wardlights.config.WardlightsConfig;
import wardlights.config.WardlightsConfig.ServerConfig.AlloyFurnaceRecipeConfig;
import wardlights.init.ModRegistry;

/**
 * Adds Wardlights recipes to NuclearCraft's machines.
 *
 * <p>Only called when NuclearCraft is loaded (see {@link Wardlights#init}),
 * since this class references NuclearCraft's code and would crash on class
 * load without it.
 */
public class NuclearCraftCompat {

    /**
     * NuclearCraft builds its recipe lists while registering recipes and only
     * caches them when a world loads, so recipes added during init are picked up.
     */
    public static void addRecipes() {
        addAlloyFurnaceRecipe("Plutonium Alloy", WardlightsConfig.server.plutoniumAlloy, ModRegistry.PLUTONIUM_ALLOY);
        addAlloyFurnaceRecipe("Plutonium Blood Alloy", WardlightsConfig.server.plutoniumBloodAlloy,
                ModRegistry.PLUTONIUM_BLOOD_ALLOY);
    }

    private static void addAlloyFurnaceRecipe(String displayName, AlloyFurnaceRecipeConfig config, Item result) {
        if (!config.enabled || result == null) {
            return;
        }
        String[] inputs = config.inputs.split(",");
        if (inputs.length != 2) {
            Wardlights.LOGGER.warn("{} Alloy Furnace recipe disabled: expected 2 inputs but found {} in '{}'",
                    displayName, inputs.length, config.inputs);
            return;
        }
        Object first = resolveInput(inputs[0].trim(), displayName);
        Object second = resolveInput(inputs[1].trim(), displayName);
        if (first == null || second == null) {
            return;
        }
        // Same argument order NuclearCraft uses for its own alloys: inputs, output, time, power.
        NCRecipes.alloy_furnace.addRecipe(first, second, new ItemStack(result, config.outputCount),
                config.timeMultiplier, config.powerMultiplier);
    }

    /**
     * An ore dictionary name (no colon) stays a String, which NuclearCraft
     * treats as "any item with this ore name"; an item id becomes an ItemStack.
     * Null when it doesn't exist.
     */
    private static Object resolveInput(String input, String displayName) {
        if (!input.contains(":")) {
            if (!OreDictionary.doesOreNameExist(input)) {
                Wardlights.LOGGER.warn("{} Alloy Furnace recipe disabled: unknown ore name '{}'", displayName, input);
                return null;
            }
            return input;
        }
        String[] parts = input.split(":");
        int meta = 0;
        if (parts.length == 3) {
            try {
                meta = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                Wardlights.LOGGER.warn("{} Alloy Furnace recipe disabled: bad metadata in '{}'", displayName, input);
                return null;
            }
        }
        ResourceLocation id = new ResourceLocation(parts[0], parts[1]);
        // The item registry falls back to air for unknown ids instead of returning null.
        if (!ForgeRegistries.ITEMS.containsKey(id)) {
            Wardlights.LOGGER.warn("{} Alloy Furnace recipe disabled: unknown item '{}'", displayName, input);
            return null;
        }
        return new ItemStack(ForgeRegistries.ITEMS.getValue(id), 1, meta);
    }
}
