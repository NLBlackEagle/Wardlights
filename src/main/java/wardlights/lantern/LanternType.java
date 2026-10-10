package wardlights.lantern;

/**
 * The five lanterns. Each lantern item knows its type, so gameplay code asks
 * "which lantern is active?" instead of comparing item instances.
 */
public enum LanternType {
    /** Lantern 1: keeps darkness mobs from spawning around you. */
    REGULAR(false, 3),
    /** Lantern 2: pixie theft protection and mimics (day and night), luck. */
    PIXIE(true, 2),
    /** Lantern 3: mining speed, Vapula / Geonach allies. */
    VAPULA(false, 3),
    /** Lantern 4: cancels targeting for listed mobs, Pale Lung. */
    PALE(false, 3),
    /** Lantern 5: SRParasites ignore you, shorter Fear; COTH clearing (day and night). */
    PARASITE(true, 1);

    private final boolean daylightTraits;
    private final int placedPriority;

    LanternType(boolean daylightTraits, int placedPriority) {
        this.daylightTraits = daylightTraits;
        this.placedPriority = placedPriority;
    }

    /**
     * True when some of this lantern's traits work regardless of the activation
     * condition (in broad daylight too). Such a lantern always counts as working
     * when picking a player's current lantern, so it never gives way to another.
     */
    public boolean hasDaylightTraits() {
        return daylightTraits;
    }

    /**
     * Which placed lantern becomes a player's ward when several reach them at
     * once (logging in between them, say): lower goes first, equal ones by
     * distance. Parasite, then Pixie, then the rest.
     */
    public int getPlacedPriority() {
        return placedPriority;
    }
}
