package wardlights.compat;

import com.dhanantry.scapeandrunparasites.util.config.SRPConfig;

/**
 * Reads SRParasites' own config. Only call this when SRParasites is installed
 * (check {@code Loader.isModLoaded} first), or its classes won't be there.
 */
public final class SRParasitesCompat {

    private SRParasitesCompat() {
    }

    /** Armor Damage Types BlackList Mobs: player names and mob ids Living/Sentient Armor never adapts to. */
    public static String[] getArmorBlacklistMobs() {
        return SRPConfig.armorDamageTypeBlackListMob;
    }

    /** Armor Damage Types BlackList Else: damage types Living/Sentient Armor never adapts to. */
    public static String[] getArmorBlacklistDamageTypes() {
        return SRPConfig.armorDamageTypeBlackListElse;
    }

    /** Armor Damage Types BlackList Inverted: both lists are whitelists instead. */
    public static boolean isArmorBlacklistInverted() {
        return SRPConfig.armorDamageTypeBlackListWhite;
    }
}
