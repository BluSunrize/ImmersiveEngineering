package blusunrize.immersiveengineering.common.gui;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import blusunrize.immersiveengineering.api.IBullet;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityBlastFurnace;

public abstract class IESlot extends Slot
{

	public IESlot(IInventory inv, int id, int x, int y)
	{
		super(inv, id, x, y);
	}

	public boolean isItemValid(ItemStack itemStack)
	{
		return true;
	}

	public static class Output extends IESlot
	{
		public Output(IInventory inv, int id, int x, int y)
		{
			super(inv, id, x, y);
		}
		public boolean isItemValid(ItemStack itemStack)
		{
			return false;
		}
	}
	public static class FluidContainer extends IESlot
	{
		boolean empty;
		public FluidContainer(IInventory inv, int id, int x, int y, boolean empty)
		{
			super(inv, id, x, y);
			this.empty=empty;
		}
		public boolean isItemValid(ItemStack itemStack)
		{
			if(empty)
				return FluidContainerRegistry.isEmptyContainer(itemStack);
			else
				return FluidContainerRegistry.isFilledContainer(itemStack);
		}
	}
	public static class BlastFuel extends IESlot
	{
		public BlastFuel(IInventory inv, int id, int x, int y)
		{
			super(inv, id, x, y);
		}
		public boolean isItemValid(ItemStack itemStack)
		{
			return TileEntityBlastFurnace.isValidFuel(itemStack);
		}
	}
	public static class Bullet extends IESlot
	{
		int limit;
		public Bullet(IInventory inv, int id, int x, int y, int limit)
		{
			super(inv, id, x, y);
			this.limit=limit;
		}
		public boolean isItemValid(ItemStack itemStack)
		{
//			return true;
			return itemStack!=null && itemStack.getItem() instanceof IBullet;
			//itemStack!=null && itemStack.getItem().equals(IEContent.itemBullet);
		}
	    public int getSlotStackLimit()
	    {
	    	return limit;
	    }
	}
}