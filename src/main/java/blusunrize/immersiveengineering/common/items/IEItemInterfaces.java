package blusunrize.immersiveengineering.common.items;

import net.minecraft.item.ItemStack;

public class IEItemInterfaces
{
	public interface IColouredItem
	{
		default boolean hasCustomItemColours()
		{
			return false;
		}
		default int getColourForIEItem(ItemStack stack, int pass)
		{
			return 16777215;
		}
	}
	public interface IGuiItem
	{
		int getGuiID(ItemStack stack);
	}
}
