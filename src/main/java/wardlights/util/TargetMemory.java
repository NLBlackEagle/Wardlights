package wardlights.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import wardlights.Wardlights;

/**
 * Which players a mob has noticed, so a lantern stops hiding them from it for
 * good (it spotted them, or they hit it). Kept in Forge's persistent entity data,
 * so it survives saving and reloading.
 */
public final class TargetMemory {

    /** The players a mob has noticed, by UUID. */
    private static final String NOTICED_TAG = Wardlights.MODID + ":noticed";

    private TargetMemory() {
    }

    public static boolean hasNoticed(Entity mob, EntityPlayer player) {
        return mob.getEntityData().getCompoundTag(NOTICED_TAG).getBoolean(player.getUniqueID().toString());
    }

    public static void markNoticed(Entity mob, EntityPlayer player) {
        NBTTagCompound data = mob.getEntityData();
        NBTTagCompound noticed = data.getCompoundTag(NOTICED_TAG);
        noticed.setBoolean(player.getUniqueID().toString(), true);
        data.setTag(NOTICED_TAG, noticed);
    }
}
