package powercrystals.minefactoryreloaded.api;

import net.minecraft.item.ItemStack;

public abstract interface IDeepStorageUnit
{
	public abstract ItemStack getStoredItemType();

	public abstract void setStoredItemCount(int paramInt);

	public abstract void setStoredItemType(ItemStack paramItemStack, int paramInt);

	public abstract int getMaxStoredCount();
}