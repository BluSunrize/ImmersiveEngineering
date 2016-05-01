package blusunrize.immersiveengineering.api.tool;

import net.minecraft.item.ItemStack;

/**
 * @author BluSunrize - 27.10.2015
 *
 * An item that contains an internal inventory, like drill or revolver
 */
public interface IInternalStorageItem
{
	public ItemStack[] getContainedItems(ItemStack stack);
	
	public void setContainedItems(ItemStack stack, ItemStack[] stackList);
	
	public int getInternalSlots(ItemStack stack);
}