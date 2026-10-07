package wardlights.lantern;

/**
 * The five lanterns. Each lantern item knows its type, so gameplay code asks
 * "which lantern is active?" instead of comparing item instances.
 */
public enum LanternType {
    /** Lantern 1: keeps darkness mobs from spawning around you. */
    REGULAR,
    /** Lantern 2: pixie theft protection, luck, mimics. */
    PIXIE,
    /** Lantern 3: mining speed, Vapula / Geonach allies. */
    VAPULA,
    /** Lantern 4: cancels targeting for listed mobs, Pale Bloom effect. */
    PALE,
    /** Lantern 5: SRParasites ignore you. */
    PARASITE
}
