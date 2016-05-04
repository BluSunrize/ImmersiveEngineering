package blusunrize.immersiveengineering.common.util.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

public class IEInventoryHandler implements IItemHandlerModifiable
{
	int slots;
	IIEInventory inv;
	int slotOffset;
	boolean[] canInsert;
	boolean[] canExtract;

	public IEInventoryHandler(int slots, IIEInventory inventory, int slotOffset, boolean[] canInsert, boolean[] canExtract)
	{
		this.slots = slots;
		this.inv = inventory;
		this.slotOffset = slotOffset;
		this.canInsert = canInsert;
		this.canExtract = canExtract;
	}
	public IEInventoryHandler(int slots, IIEInventory inventory)
	{
		this(slots,inventory,0, new boolean[slots], new boolean[slots]);
		for(int i=0; i<slots; i++)
			this.canExtract[i] = this.canInsert[i] = true;
	}
	public IEInventoryHandler(int slots, IIEInventory inventory, int slotOffset, boolean canInsert, boolean canExtract)
	{
		this(slots,inventory,slotOffset, new boolean[slots], new boolean[slots]);
		for(int i=0; i<slots; i++)
		{
			this.canInsert[i] = canInsert;
			this.canExtract[i] = canExtract;
		}
	}

	@Override
	public int getSlots()
	{
		return slots;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return this.inv.getInventory()[this.slotOffset + slot];
	}
	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
//		System.out.println("attempt: "+stack);
		if(!canInsert[slot] || stack==null)
			return stack;
//		System.out.println("1");

		if(!inv.isStackValid(this.slotOffset + slot, stack))
			return stack;
//		System.out.println("2");
		
		int offsetSlot = this.slotOffset+slot;
		ItemStack currentStack = inv.getInventory()[offsetSlot];

		if(currentStack==null)
		{
			int accepted = Math.min(stack.getMaxStackSize(), inv.getSlotLimit(offsetSlot));
			if(accepted<stack.stackSize)
			{
				if(!simulate)
				{
					inv.getInventory()[offsetSlot] = stack.splitStack(accepted);
					inv.doGraphicalUpdates(offsetSlot);
					return stack;
				}
				else
				{
					stack.stackSize -= accepted;
					return stack;
				}
			}
			else
			{
				if(!simulate)
				{
					inv.getInventory()[offsetSlot] = stack;
					inv.doGraphicalUpdates(offsetSlot);
				}
				return null;
			}
		}
		else
		{
			if(!ItemHandlerHelper.canItemStacksStack(stack, currentStack))
				return stack;

			int accepted = Math.min(stack.getMaxStackSize(), inv.getSlotLimit(offsetSlot)) - currentStack.stackSize;
			if(accepted<stack.stackSize)
			{
				if(!simulate)
				{
					ItemStack newStack = stack.splitStack(accepted);
					newStack.stackSize += currentStack.stackSize;
					inv.getInventory()[offsetSlot] = newStack;
					inv.doGraphicalUpdates(offsetSlot);
					return stack;
				}
				else
				{
					stack.stackSize -= accepted;
					return stack;
				}
			}
			else
			{
				if(!simulate)
				{
					ItemStack newStack = stack.copy();
					newStack.stackSize += currentStack.stackSize;
					inv.getInventory()[offsetSlot] = newStack;
					inv.doGraphicalUpdates(offsetSlot);
				}
				return null;
			}
		}
	}
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		if(!canExtract[slot] || amount==0)
			return null;

		int offsetSlot = this.slotOffset+slot;
		ItemStack currentStack = inv.getInventory()[offsetSlot];

		if(currentStack==null)
			return null;

		int extracted = Math.min(currentStack.stackSize, amount);

		ItemStack copy = currentStack.copy();
		copy.stackSize = extracted;
		if(!simulate)
		{
			if(extracted<currentStack.stackSize)
				currentStack.stackSize -= extracted;
			else
				currentStack = null;
			inv.getInventory()[offsetSlot] = currentStack;
			inv.doGraphicalUpdates(offsetSlot);
		}
		return copy;
	}
	@Override
	public void setStackInSlot(int slot, ItemStack stack)
	{
		inv.getInventory()[this.slotOffset+slot] = stack;
		inv.doGraphicalUpdates(this.slotOffset+slot);
	}
}