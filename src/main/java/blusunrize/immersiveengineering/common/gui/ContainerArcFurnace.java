package blusunrize.immersiveengineering.common.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityArcFurnace;

public class ContainerArcFurnace extends Container
{
	TileEntityArcFurnace tile;
	int slotCount;
	public ContainerArcFurnace(InventoryPlayer inventoryPlayer, TileEntityArcFurnace tile)
	{
		this.tile=tile;
		for(int i=0; i<12; i++)
			this.addSlotToContainer(new IESlot.ArcInput(this, tile, i, 10+i%3*21,34+i/3*18));
		for(int i=0; i<4; i++)
			this.addSlotToContainer(new IESlot.ArcAdditive(this, tile, 12+i, 114+i%2*18,34+i/2*18));
		for(int i=0; i<6; i++)
			this.addSlotToContainer(new IESlot.Output(this, tile, 16+i, 78+i%3*18,80+i/3*18));
		this.addSlotToContainer(new IESlot.Output(this, tile, 22, 132,98));

		this.addSlotToContainer(new IESlot.ArcElectrode(this, tile, 23, 62,10));
		this.addSlotToContainer(new IESlot.ArcElectrode(this, tile, 24, 80,10));
		this.addSlotToContainer(new IESlot.ArcElectrode(this, tile, 25, 98,10));

		slotCount=26;

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 126+i*18));
		for (int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 184));
	}

	@Override
	public boolean canInteractWith(EntityPlayer p_75145_1_)
	{
		return true;
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
				int i = -1;
				int j = -1;
				if(ArcFurnaceRecipe.isValidInput(stackInSlot))
				{
					i=0;
					j=12;
				}
				else if(ArcFurnaceRecipe.isValidAdditive(stackInSlot))
				{
					i=12;
					j=16;
				}
				else if(IEContent.itemGraphiteElectrode.equals(stack.getItem()))
				{
					i=23;
					j=26;
				}
				if(i!=-1 && j!=-1)
					if(!this.mergeItemStack(stackInSlot, i,j, false))
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