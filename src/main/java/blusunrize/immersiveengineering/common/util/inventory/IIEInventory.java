package blusunrize.immersiveengineering.common.util.inventory;

import net.minecraft.item.ItemStack;

public interface IIEInventory
{
	ItemStack[] getInventory();
	boolean isStackValid(int slot, ItemStack stack);
	int getSlotLimit(int slot);
	void doGraphicalUpdates(int slot);
	default ItemStack[] getDroppedItems(){return getInventory();}
}
