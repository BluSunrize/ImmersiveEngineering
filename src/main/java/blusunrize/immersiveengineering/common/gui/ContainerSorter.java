package blusunrize.immersiveengineering.common.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConveyorSorter;
import blusunrize.immersiveengineering.common.util.Utils;

public class ContainerSorter extends Container
{
	TileEntityConveyorSorter tile;
	int slotCount;
	public ContainerSorter(InventoryPlayer inventoryPlayer, TileEntityConveyorSorter tile)
	{
		this.tile=tile;
		for(int side=0; side<6; side++)
			for(int i=0; i<TileEntityConveyorSorter.filterSlotsPerSide; i++)
			{
				int x = 4+ (side/2)*58 + (i<3?i*18: i>4?(i-5)*18: i==3?0: 36);
				int y = 4+ (side%2)*58 + (i<3?0: i>4?36: 18);
				int id = side*TileEntityConveyorSorter.filterSlotsPerSide+i;
				this.addSlotToContainer(new IESlot.Ghost(this, tile.filter, id, x, y));
			}

		//				this.addSlotToContainer(new Slot(tile, i, 24+i%3*18, 17+i/3*18));
		//		this.addSlotToContainer(new IESlot.FluidContainer(tile, 9, 133,19, true));
		//		this.addSlotToContainer(new IESlot.Output(tile, 10, 133,55));
		slotCount=6*TileEntityConveyorSorter.filterSlotsPerSide;

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 127+i*18));
		for (int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 185));
	}

	@Override
	public boolean canInteractWith(EntityPlayer p_75145_1_)
	{
		return true;
	}

	@Override
	public ItemStack slotClick(int id, int button, int modifier, EntityPlayer player)
	{
		Slot slot = id<0?null: (Slot)this.inventorySlots.get(id);
		if(!(slot instanceof IESlot.Ghost))
			return super.slotClick(id, button, modifier, player);
		
		ItemStack stack = null;
		ItemStack stackSlot = slot.getStack();
		if(stackSlot!=null)
			stack = stackSlot.copy();
		
		if (button==2)
//			fillPhantomSlot(slot, null, button);
			slot.putStack(null);
		else if(button==0||button==1)
		{
			InventoryPlayer playerInv = player.inventory;
			ItemStack stackHeld = playerInv.getItemStack();
			if (stackSlot == null)
			{
				if(stackHeld != null && slot.isItemValid(stackHeld))
				{
					slot.putStack(Utils.copyStackWithAmount(stackHeld, 1));
//					fillPhantomSlot(slot, stackHeld, button);
				}
			}
			else if (stackHeld == null)
			{
				slot.putStack(null);
//				adjustPhantomSlot(slot, button, modifier);
			}
			else if (slot.isItemValid(stackHeld))
			{
//				if (StackUtils.isIdenticalItem(stackSlot, stackHeld))
//				{
//					adjustPhantomSlot(slot, button, modifier);
//				}
//				else
//				{
//					fillPhantomSlot(slot, stackHeld, button);
					slot.putStack(Utils.copyStackWithAmount(stackHeld, 1));
//				}
			}
		}
		else if (button == 5)
		{
			InventoryPlayer playerInv = player.inventory;
			ItemStack stackHeld = playerInv.getItemStack();
			if (!slot.getHasStack())
			{
				slot.putStack(Utils.copyStackWithAmount(stackHeld, 1));
//				fillPhantomSlot(slot, stackHeld, button);
			}
		}
		return stack;

//		return super.slotClick(id, button, modifier, player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
	{
		return null;
//		ItemStack stack = null;
//		Slot slotObject = (Slot) inventorySlots.get(slot);
//
//		if (slotObject != null && slotObject.getHasStack())
//		{
//			ItemStack stackInSlot = slotObject.getStack();
//			stack = stackInSlot.copy();
//
//			if (slot < slotCount)
//			{
//				if(!this.mergeItemStack(stackInSlot, slotCount, (slotCount + 36), true))
//					return null;
//			}
//			else
//			{
//				if(!this.mergeItemStack(stackInSlot, 0,9, false))
//					return null;
//			}
//
//			if (stackInSlot.stackSize == 0)
//				slotObject.putStack(null);
//			else
//				slotObject.onSlotChanged();
//
//			if (stackInSlot.stackSize == stack.stackSize)
//				return null;
//			slotObject.onPickupFromSlot(player, stackInSlot);
//		}
//		return stack;
	}
}