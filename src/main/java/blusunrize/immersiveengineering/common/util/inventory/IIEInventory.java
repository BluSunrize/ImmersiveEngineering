package blusunrize.immersiveengineering.common.util.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public interface IIEInventory
{
	NonNullList<ItemStack> getInventory();
	boolean isStackValid(int slot, ItemStack stack);
	int getSlotLimit(int slot);
	void doGraphicalUpdates(int slot);
	default NonNullList<ItemStack> getDroppedItems(){return getInventory();}
	default int getComparatedSize(){return getInventory().size();}
}
