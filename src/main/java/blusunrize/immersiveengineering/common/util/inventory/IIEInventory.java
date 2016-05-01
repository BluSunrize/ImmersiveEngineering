package blusunrize.immersiveengineering.common.util.inventory;

import net.minecraft.item.ItemStack;

public interface IIEInventory
{
	public ItemStack[] getInventory();
	public boolean isStackValid(int slot, ItemStack stack);
	public int getSlotLimit(int slot);
	public void doGraphicalUpdates(int slot);
}
