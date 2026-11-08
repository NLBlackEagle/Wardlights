package wardlights.compat;

import java.util.Map;
import java.util.Set;

import srpmixins.config.SRPMixinsConfigHandler;
import srpmixins.config.SRPMixinsConfigProvider;

/**
 * Reads SRPMixins' adaptation config (its overhaul of Living/Sentient Armor
 * adaptation). Only call this when SRPMixins is installed (check
 * {@code Loader.isModLoaded} first), or its classes won't be there.
 */
public final class SRPMixinsCompat {

    private SRPMixinsCompat() {
    }

    /** Fire Multiplier Dmg Types, as SRPMixins parsed them. */
    public static Set<String> getFireTypes() {
        return SRPMixinsConfigProvider.fireMultiDmgTypes;
    }

    /** Burning Adaptation Effectiveness. */
    public static double getBurningEffectiveness() {
        return SRPMixinsConfigHandler.adaptation.burningAdaptationEffectiveness;
    }

    /** Adaptation Type Overrides, as SRPMixins parsed them (from -> to). */
    public static Map<String, String> getTypeOverrides() {
        return SRPMixinsConfigProvider.adaptationOverrides;
    }

    /** Fix Blacklist Check: player and mob names are only checked against the Mobs list. */
    public static boolean isBlacklistCheckFixed() {
        return SRPMixinsConfigHandler.adaptation.fixBlacklistCheck;
    }
}
