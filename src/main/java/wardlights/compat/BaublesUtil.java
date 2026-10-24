package wardlights.compat;

import baubles.api.BaublesApi;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

import javax.annotation.Nullable;

public class BaublesUtil {

    public static boolean isEquipped(EntityPlayer player, @Nullable Item bauble) {
        return BaublesApi.isBaubleEquipped(player, bauble) >= 0;
    }
}
