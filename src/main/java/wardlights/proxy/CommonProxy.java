package wardlights.proxy;

import net.minecraft.entity.player.EntityPlayer;

public class CommonProxy {

    public void preInit() {
    }

    /**
     * No-op on the server/common side — the actual GeckoLib draw calls
     * ({@code AegisOfSacrificeBaubleRenderer}) live in client-only classes and
     * must never be referenced directly from the (common) Item class.
     */
    public void renderAegisOfSacrificeBauble(EntityPlayer player, float partialTicks) {
    }
}