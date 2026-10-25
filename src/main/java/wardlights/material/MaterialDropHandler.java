package wardlights.material;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.RandomChance;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import wardlights.Wardlights;
import wardlights.config.WardlightsConfig;

/**
 * Where the crafting ingredients that aren't crafted come from: the Beckon
 * Pearl and Dispatcher Bloom drop from Beckons and Dispatchers (a chance per
 * stage), the Ancient Ring and Crown are added to chest loot tables. Both are listed in config by registry id, so nothing here
 * touches another mod's classes.
 */
@Mod.EventBusSubscriber(modid = Wardlights.MODID)
public class MaterialDropHandler {

    /**
     * Rolls every {@code itemId;mobId;chance} line naming the mob that died, so
     * one mob can drop several items and one item can come from several mobs.
     */
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote) {
            return;
        }
        ResourceLocation id = EntityList.getKey(entity);
        if (id == null) {
            return;
        }
        String mobId = id.toString();
        for (String entry : WardlightsConfig.server.baubles.mobDrops) {
            String[] parts = entry.split(";");
            if (parts.length != 3) {
                Wardlights.LOGGER.warn("Mob Drops: '{}' should be itemId;mobId;chance", entry);
                continue;
            }
            if (!parts[1].trim().equals(mobId)) {
                continue;
            }
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(parts[0].trim()));
            if (item == null || item == Items.AIR) {
                continue;
            }
            float chance;
            try {
                chance = Float.parseFloat(parts[2].trim());
            } catch (NumberFormatException e) {
                Wardlights.LOGGER.warn("Mob Drops: '{}' should be itemId;mobId;chance", entry);
                continue;
            }
            if (entity.world.rand.nextFloat() < chance) {
                event.getDrops().add(new EntityItem(entity.world, entity.posX, entity.posY, entity.posZ,
                        new ItemStack(item)));
            }
        }
    }

    /**
     * Adds items (the Ancient Ring and Crown by default) to the loot tables
     * listed in config, in one of two ways: {@code itemId;tableId;chance} gives
     * the table an extra pool of its own (one roll, behind a random-chance
     * condition, on top of the usual loot), while
     * {@code itemId;tableId;poolName;weight} adds the item as one more entry of an
     * existing pool, competing with that pool's other entries by weight.
     */
    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        String tableId = event.getName().toString();
        for (String entry : WardlightsConfig.server.baubles.lootTableAdditions) {
            String[] parts = entry.split(";");
            if (parts.length < 2 || !parts[1].trim().equals(tableId)) {
                continue;
            }
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(parts[0].trim()));
            if (item == null || item == Items.AIR) {
                // Unknown ids resolve to air; e.g. the item's mod isn't installed.
                continue;
            }
            try {
                if (parts.length == 3) {
                    addPool(event, item, Float.parseFloat(parts[2].trim()));
                } else if (parts.length == 4) {
                    addToPool(event, parts[2].trim(), item, Integer.parseInt(parts[3].trim()));
                } else {
                    warnMalformed(entry);
                }
            } catch (NumberFormatException e) {
                warnMalformed(entry);
            }
        }
    }

    private static void addPool(LootTableLoadEvent event, Item item, float chance) {
        String name = entryName(item);
        // Pool and entry names must be unique; adding a duplicate throws.
        if (event.getTable().getPool(name) != null) {
            Wardlights.LOGGER.warn("Loot table {} already has a {} pool; listed twice?", event.getName(), name);
            return;
        }
        LootEntry entry = new LootEntryItem(item, 1, 0, new LootFunction[0], new LootCondition[0], name);
        event.getTable().addPool(new LootPool(new LootEntry[] { entry },
                new LootCondition[] { new RandomChance(chance) }, new RandomValueRange(1), new RandomValueRange(0),
                name));
    }

    private static void addToPool(LootTableLoadEvent event, String poolName, Item item, int weight) {
        LootPool pool = event.getTable().getPool(poolName);
        if (pool == null) {
            Wardlights.LOGGER.warn("Loot table {} has no pool named '{}'", event.getName(), poolName);
            return;
        }
        String name = entryName(item);
        if (pool.getEntry(name) != null) {
            Wardlights.LOGGER.warn("Pool '{}' of loot table {} already has {}; listed twice?", poolName,
                    event.getName(), name);
            return;
        }
        pool.addEntry(new LootEntryItem(item, weight, 0, new LootFunction[0], new LootCondition[0], name));
    }

    private static void warnMalformed(String entry) {
        Wardlights.LOGGER.warn("Loot Table Additions: '{}' should be itemId;tableId;chance or "
                + "itemId;tableId;poolName;weight", entry);
    }

    /** Entries and pools are named after the item, prefixed so they can't clash with the table's own. */
    private static String entryName(Item item) {
        return Wardlights.MODID + ":" + item.getRegistryName().toString();
    }
}
