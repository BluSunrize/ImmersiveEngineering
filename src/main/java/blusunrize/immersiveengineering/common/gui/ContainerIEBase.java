package blusunrize.immersiveengineering.common.gui;

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