package wardlights.lantern.tileentity;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/**
 * Every placed lantern remembers the NBT of the lantern item it was placed from
 * (Quality Tools' quality, a custom name...), so breaking it gives that same item
 * back. Without this a placed lantern would drop a blank item, and Quality Tools
 * would roll it a new quality: a free reforge.
 */
public abstract class TileEntityLanternBase extends TileEntity {

    private static final String ITEM_TAG = "ItemTag";

    @Nullable
    private NBTTagCompound itemTag;

    public void setItemTag(@Nullable NBTTagCompound tag) {
        itemTag = tag == null ? null : tag.copy();
        markDirty();
    }

    @Nullable
    public NBTTagCompound getItemTag() {
        return itemTag == null ? null : itemTag.copy();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        itemTag = compound.hasKey(ITEM_TAG) ? compound.getCompoundTag(ITEM_TAG) : null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (itemTag != null) {
            compound.setTag(ITEM_TAG, itemTag.copy());
        }
        return compound;
    }
}
