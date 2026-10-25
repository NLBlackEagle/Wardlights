package wardlights.bauble.chest;

import net.minecraft.entity.player.EntityPlayer;
import wardlights.Wardlights;
import wardlights.config.WardlightsConfig;

/**
 * The only obtainable item of this set — worn in the Baubles BODY slot, it
 * renders both the badge (chest) and shoulder (idle-animated) GeckoLib parts
 * on the wearer at once. See {@link wardlights.client.render.AegisOfSacrificeBaubleRenderer}
 * for the actual draw calls, reached through {@link Wardlights#PROXY} so this
 * (common) class never references client-only rendering classes directly.
 * Bonuses come from {@link ItemChestBauble}.
 */
public class ItemAegisOfSacrifice extends ItemChestBauble {

    public ItemAegisOfSacrifice(String registryName) {
        super(registryName);
    }

    @Override
    protected int getResistanceLevel() {
        return WardlightsConfig.server.chestBaubles.aegisResistanceLevel;
    }

    @Override
    protected void renderOnPlayer(EntityPlayer player, float partialTicks) {
        Wardlights.PROXY.renderAegisOfSacrificeBauble(player, partialTicks);
    }
}
