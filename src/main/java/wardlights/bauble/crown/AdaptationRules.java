package wardlights.bauble.crown;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraftforge.fml.common.Loader;
import wardlights.compat.SRPMixinsCompat;
import wardlights.compat.SRParasitesCompat;
import wardlights.config.WardlightsConfig;
import wardlights.config.WardlightsConfig.ServerConfig.BaubleConfig;
import wardlights.init.ModRegistry;

/**
 * The rules the Blooming Crown adapts by, the same ones Sentient Armor follows
 * in the pack: SRParasites' armor blacklists and SRPMixins' adaptation overhaul
 * settings. Whichever of those mods is installed is the source of truth; our own
 * config copies (set to the pack's values) are only the fallback without them.
 */
final class AdaptationRules {

    /** An override to this means "never adapt to it", as in SRPMixins. */
    static final String NEVER = "NONE";

    private static Boolean hasSrp;
    private static Boolean hasSrpMixins;

    /** Our parsed fallback lists, kept until the config hands us new arrays. */
    private static String[] parsedOverridesFrom;
    private static Map<String, String> parsedOverrides = Collections.emptyMap();
    private static String[] parsedFireTypesFrom;
    private static Set<String> parsedFireTypes = Collections.emptySet();

    private AdaptationRules() {
    }

    /** Player names and mob ids it never adapts to. */
    static String[] blacklistMobs() {
        return hasSrp() ? SRParasitesCompat.getArmorBlacklistMobs() : config().dispatcherCrownBlacklistMobs;
    }

    /** Damage types it never adapts to. */
    static String[] blacklistDamageTypes() {
        return hasSrp() ? SRParasitesCompat.getArmorBlacklistDamageTypes() : config().dispatcherCrownBlacklist;
    }

    /** Both blacklists are whitelists instead. */
    static boolean blacklistInverted() {
        return hasSrp() ? SRParasitesCompat.isArmorBlacklistInverted() : config().dispatcherCrownBlacklistInverted;
    }

    /**
     * When true, player and mob names are only checked against the Mobs list.
     * Base SRP also checks them against the damage type list, a bug SRPMixins fixes.
     */
    static boolean blacklistCheckFixed() {
        return !hasSrpMixins() || SRPMixinsCompat.isBlacklistCheckFixed();
    }

    /** The source this one counts as, {@link #NEVER}, or the source itself. */
    static String override(String source) {
        Map<String, String> overrides;
        if (hasSrpMixins()) {
            overrides = SRPMixinsCompat.getTypeOverrides();
        } else {
            String[] lines = config().dispatcherCrownTypeOverrides;
            if (lines != parsedOverridesFrom) {
                parsedOverrides = parseOverrides(lines);
                parsedOverridesFrom = lines;
            }
            overrides = parsedOverrides;
        }
        return overrides.getOrDefault(source, source);
    }

    /** Damage types counted as fire; "isBurning" stands for any hit while on fire. */
    static Set<String> fireTypes() {
        if (hasSrpMixins()) {
            return SRPMixinsCompat.getFireTypes();
        }
        String[] types = config().dispatcherCrownFireTypes;
        if (types != parsedFireTypesFrom) {
            parsedFireTypes = new HashSet<>(Arrays.asList(types));
            parsedFireTypesFrom = types;
        }
        return parsedFireTypes;
    }

    /** How well adaptation works against fire hits, 0 to 1. */
    static double burningEffectiveness() {
        return hasSrpMixins() ? SRPMixinsCompat.getBurningEffectiveness() : config().dispatcherCrownBurningEffectiveness;
    }

    /** "from, to" lines, trimmed, the format SRPMixins reads. Lines that aren't two parts are skipped. */
    private static Map<String, String> parseOverrides(String[] lines) {
        Map<String, String> overrides = new HashMap<>();
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length == 2) {
                overrides.put(parts[0].trim(), parts[1].trim());
            }
        }
        return overrides;
    }

    private static BaubleConfig config() {
        return WardlightsConfig.server.baubles;
    }

    private static boolean hasSrp() {
        if (hasSrp == null) {
            hasSrp = Loader.isModLoaded(ModRegistry.SRPARASITES_MODID);
        }
        return hasSrp;
    }

    private static boolean hasSrpMixins() {
        if (hasSrpMixins == null) {
            hasSrpMixins = Loader.isModLoaded(ModRegistry.SRPMIXINS_MODID);
        }
        return hasSrpMixins;
    }
}
