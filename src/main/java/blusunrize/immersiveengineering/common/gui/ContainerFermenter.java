package blusunrize.immersiveengineering.common.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFermenter;

public class ContainerFermenter extends Container
{
	TileEntityFermenter tile;
	int slotCount;
	public ContainerFermenter(InventoryPlayer inventoryPlayer, TileEntityFermenter tile)
	{
		this.tile=tile;
		for(int i=0; i<9; i++)
			this.addSlotToContainer(new Slot(tile, i, 24+i%3*18, 17+i/3*18));
		this.addSlotToContainer(new IESlot.Output(this, tile, 11, 91,35));
		this.addSlotToContainer(new IESlot.FluidContainer(this, tile, 9, 133,19, true));
		this.addSlotToContainer(new IESlot.Output(this, tile, 10, 133,55));
		slotCount=11;

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18));
		for (int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 142));
	}

	@Override
	public boolean canInteractWith(EntityPlayer p_75145_1_)
	{
		return tile.isUseableByPlayer(p_75145_1_);
	}


	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
	{
		ItemStack stack = null;
		Slot slotObject = (Slot) inventorySlots.get(slot);

		if (slotObject != null && slotObject.getHasStack())
		{
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();

			if (slot < slotCount)
			{
				if(!this.mergeItemStack(stackInSlot, slotCount, (slotCount + 36), true))
					return null;
			}
			else
			{
				if(!this.mergeItemStack(stackInSlot, 0,9, false))
					return null;
			}

			if (stackInSlot.stackSize == 0)
				slotObject.putStack(null);
			else
				slotObject.onSlotChanged();

			if (stackInSlot.stackSize == stack.stackSize)
				return null;
			slotObject.onPickupFromSlot(player, stackInSlot);
		}
		return stack;
	}
}