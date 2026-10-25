package wardlights.bauble.chest;

import net.minecraft.entity.player.EntityPlayer;
import wardlights.Wardlights;
import wardlights.config.WardlightsConfig;

/**
 * Worn in the Baubles BODY slot, it renders the GeckoLib chest badge on the
 * wearer. See {@link wardlights.client.render.BulwarkOfValorBaubleRenderer}
 * for the actual draw calls, reached through {@link Wardlights#PROXY} so this
 * (common) class never references client-only rendering classes directly.
 * Bonuses come from {@link ItemChestBauble}.
 */
public class ItemBulwarkOfValor extends ItemChestBauble {

    public ItemBulwarkOfValor(String registryName) {
        super(registryName);
    }

    @Override
    protected int getResistanceLevel() {
        return WardlightsConfig.server.chestBaubles.bulwarkResistanceLevel;
    }

    @Override
    protected void renderOnPlayer(EntityPlayer player, float partialTicks) {
        Wardlights.PROXY.renderBulwarkOfValorBauble(player, partialTicks);
    }
}
