package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class ContainerIEBase<T extends TileEntity> extends Container
{
	public T tile;
	public IInventory inv;
	public int slotCount;

	public ContainerIEBase(InventoryPlayer inventoryPlayer, T tile)
	{
		this.tile=tile;
		this.inv = new InventoryTile(tile);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return inv.isUseableByPlayer(player);
	}

	@Override
	public ItemStack slotClick(int id, int button, int modifier, EntityPlayer player)
	{
		Slot slot = id<0?null: (Slot)this.inventorySlots.get(id);
		if(!(slot instanceof IESlot.Ghost))
			return super.slotClick(id, button, modifier, player);
		//Spooky Ghost Slots!!!!
		ItemStack stack = null;
		ItemStack stackSlot = slot.getStack();
		if(stackSlot!=null)
			stack = stackSlot.copy();

		if (button==2)
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
				}
			}
			else if (stackHeld == null)
			{
				slot.putStack(null);
			}
			else if (slot.isItemValid(stackHeld))
			{
				slot.putStack(Utils.copyStackWithAmount(stackHeld, 1));
			}
		}
		else if (button == 5)
		{
			InventoryPlayer playerInv = player.inventory;
			ItemStack stackHeld = playerInv.getItemStack();
			if (!slot.getHasStack())
			{
				slot.putStack(Utils.copyStackWithAmount(stackHeld, 1));
			}
		}
		return stack;
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
	{
		ItemStack stack = null;
		Slot slotObject = (Slot) inventorySlots.get(slot);

		if(slotObject != null && slotObject.getHasStack())
		{
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();

			if(slot < slotCount)
			{
				if(!this.mergeItemStack(stackInSlot, slotCount, (slotCount + 36), true))
					return null;
			}
			else
			{
				boolean b = false;
				for(int i=0;i<slotCount;i++)
					if(this.getSlot(i).isItemValid(stackInSlot))
						if(this.mergeItemStack(stackInSlot, i,i+1, false))
						{
							b = true;
							break;
						}
				if(!b)
					return null;
			}

			if(stackInSlot.stackSize == 0)
				slotObject.putStack(null);
			else
				slotObject.onSlotChanged();

			if(stackInSlot.stackSize == stack.stackSize)
				return null;
			slotObject.onPickupFromSlot(player, stackInSlot);
		}
		return stack;
	}
}