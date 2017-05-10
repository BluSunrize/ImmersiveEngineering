package blusunrize.immersiveengineering.api.tool;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

/**
 * @author BluSunrize - 27.10.2015
 *
 * An item that contains an internal inventory, like drill or revolver
 */
public interface IInternalStorageItem
{
	NonNullList<ItemStack> getContainedItems(ItemStack stack);
	
	void setContainedItems(ItemStack stack, NonNullList<ItemStack> stackList);
	
	int getInternalSlots(ItemStack stack);
}