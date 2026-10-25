package wardlights.client.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import wardlights.Wardlights;

/**
 * One bauble part's live-tunable placement, parsed from its config strings
 * (see the placement entries in {@code WardlightsConfig.ClientConfig}):
 * <ul>
 * <li>base: {@code {offsetX, offsetY, offsetZ, rotationX, rotationY, rotationZ, scale}}</li>
 * <li>armor list, one entry per line: {@code {type, offsetX, offsetY, offsetZ, scale}}, where
 * {@code type} is {@code default} or a chest armor registry id ({@code *} wildcards allowed).
 * The first matching id entry wins; {@code default} covers any other {@link ItemArmor}.</li>
 * </ul>
 * The armor offsets add to the base offsets and the armor scale multiplies the base scale.
 *
 * <p>The raw strings are re-read through suppliers every call (Forge's config sync replaces the
 * field values) but only re-parsed when they actually change. An unparsable base value logs a
 * warning and keeps the last valid one, so half-typed edits in the config GUI don't snap the
 * model around; unparsable armor lines are skipped.
 */
public final class BaublePlacement {

    private static final String DEFAULT_TYPE = "default";
    private static final double[] NEUTRAL = { 0, 0, 0, 0, 0, 0, 1 };
    private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

    private final String name;
    private final Supplier<String> baseSource;
    private final Supplier<String[]> armorSource;

    private String parsedBaseRaw;
    private double[] base = NEUTRAL;
    private String[] parsedArmorRaw;
    private List<ArmorEntry> armorEntries = new ArrayList<>();
    private double[] armorDefault;

    public BaublePlacement(String name, Supplier<String> baseSource, Supplier<String[]> armorSource) {
        this.name = name;
        this.baseSource = baseSource;
        this.armorSource = armorSource;
    }

    /**
     * The final {@code offsetX, offsetY, offsetZ, rotationX, rotationY, rotationZ, scale} for a
     * player wearing {@code chest} in the chest slot.
     */
    public double[] resolve(ItemStack chest) {
        refresh();
        double[] armor = armorAdjustment(chest);
        if (armor == null) {
            return base;
        }
        double[] result = base.clone();
        result[0] += armor[0];
        result[1] += armor[1];
        result[2] += armor[2];
        result[6] *= armor[3];
        return result;
    }

    /**
     * Whether a registry id matches a config pattern, where {@code *} matches any run of
     * characters (e.g. {@code iceandfire:armor_*_chestplate}). Case-insensitive.
     */
    public static boolean matchesId(String pattern, String id) {
        String trimmed = pattern.trim().toLowerCase(Locale.ROOT);
        if (trimmed.isEmpty()) {
            return false;
        }
        return PATTERN_CACHE.computeIfAbsent(trimmed, BaublePlacement::compileWildcard)
                .matcher(id.toLowerCase(Locale.ROOT)).matches();
    }

    private static Pattern compileWildcard(String pattern) {
        StringBuilder regex = new StringBuilder();
        String[] literals = pattern.split("\\*", -1);
        for (int i = 0; i < literals.length; i++) {
            if (i > 0) {
                regex.append(".*");
            }
            if (!literals[i].isEmpty()) {
                regex.append(Pattern.quote(literals[i]));
            }
        }
        return Pattern.compile(regex.toString());
    }

    private double[] armorAdjustment(ItemStack chest) {
        if (chest.isEmpty()) {
            return null;
        }
        ResourceLocation id = chest.getItem().getRegistryName();
        if (id != null) {
            String idString = id.toString();
            for (ArmorEntry entry : armorEntries) {
                if (entry.pattern.matcher(idString).matches()) {
                    return entry.values;
                }
            }
        }
        // Checked for ItemArmor so an elytra (which leaves the torso bare) doesn't count.
        return chest.getItem() instanceof ItemArmor ? armorDefault : null;
    }

    private void refresh() {
        String baseRaw = baseSource.get();
        if (!Objects.equals(baseRaw, parsedBaseRaw)) {
            parsedBaseRaw = baseRaw;
            double[] values = parseNumbers(baseRaw, 0, 7);
            if (values != null) {
                base = values;
            } else {
                Wardlights.LOGGER.warn("{}: invalid value '{}', expected {{offsetX, offsetY, offsetZ, rotationX, "
                        + "rotationY, rotationZ, scale}}; keeping the previous placement", name, baseRaw);
            }
        }

        String[] armorRaw = armorSource.get();
        if (!Arrays.equals(armorRaw, parsedArmorRaw)) {
            parsedArmorRaw = armorRaw == null ? null : armorRaw.clone();
            parseArmor(armorRaw == null ? new String[0] : armorRaw);
        }
    }

    private void parseArmor(String[] lines) {
        List<ArmorEntry> entries = new ArrayList<>();
        double[] fallback = null;
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] tokens = splitBraced(line);
            double[] values = tokens.length == 5 ? parseNumbers(tokens, 1, 4) : null;
            String type = tokens[0].toLowerCase(Locale.ROOT);
            if (values == null || type.isEmpty()) {
                Wardlights.LOGGER.warn("{}: invalid entry '{}', expected {{default or armor id, offsetX, offsetY, "
                        + "offsetZ, scale}}; skipping it", name, line);
            } else if (type.equals(DEFAULT_TYPE)) {
                if (fallback == null) {
                    fallback = values;
                }
            } else {
                entries.add(new ArmorEntry(PATTERN_CACHE.computeIfAbsent(type, BaublePlacement::compileWildcard),
                        values));
            }
        }
        armorEntries = entries;
        armorDefault = fallback;
    }

    /** Parses {@code count} numbers from a {@code {a, b, c}} string (braces optional), or null if malformed. */
    private static double[] parseNumbers(String raw, int skip, int count) {
        if (raw == null) {
            return null;
        }
        String[] tokens = splitBraced(raw);
        return tokens.length == skip + count ? parseNumbers(tokens, skip, count) : null;
    }

    private static double[] parseNumbers(String[] tokens, int skip, int count) {
        double[] values = new double[count];
        try {
            for (int i = 0; i < count; i++) {
                values[i] = Double.parseDouble(tokens[skip + i]);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        // Scale is always last; a non-positive one would invert or collapse the model.
        return values[count - 1] > 0 ? values : null;
    }

    private static String[] splitBraced(String raw) {
        String s = raw.trim();
        if (s.startsWith("{")) {
            s = s.substring(1);
        }
        if (s.endsWith("}")) {
            s = s.substring(0, s.length() - 1);
        }
        String[] tokens = s.split(",", -1);
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokens[i].trim();
        }
        return tokens;
    }

    private static final class ArmorEntry {
        final Pattern pattern;
        final double[] values;

        ArmorEntry(Pattern pattern, double[] values) {
            this.pattern = pattern;
            this.values = values;
        }
    }
}
