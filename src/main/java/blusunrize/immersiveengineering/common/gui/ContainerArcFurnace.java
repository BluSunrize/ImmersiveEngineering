/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityArcFurnace;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ContainerArcFurnace extends ContainerIEBase<TileEntityArcFurnace>
{
	public ContainerArcFurnace(InventoryPlayer inventoryPlayer, TileEntityArcFurnace tile)
	{
		super(inventoryPlayer, tile);
		this.tile = tile;
		for(int i = 0; i < 12; i++)
			this.addSlotToContainer(new IESlot.ArcInput(this, this.inv, i, 10+i%3*21, 34+i/3*18));
		for(int i = 0; i < 4; i++)
			this.addSlotToContainer(new IESlot.ArcAdditive(this, this.inv, 12+i, 114+i%2*18, 34+i/2*18));
		for(int i = 0; i < 6; i++)
			this.addSlotToContainer(new IESlot.Output(this, this.inv, 16+i, 78+i%3*18, 80+i/3*18));
		this.addSlotToContainer(new IESlot.Output(this, this.inv, 22, 132, 98));

		this.addSlotToContainer(new IESlot.ArcElectrode(this, this.inv, 23, 62, 10));
		this.addSlotToContainer(new IESlot.ArcElectrode(this, this.inv, 24, 80, 10));
		this.addSlotToContainer(new IESlot.ArcElectrode(this, this.inv, 25, 98, 10));

		slotCount = 26;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 126+i*18));
		for(int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 184));
	}

	@Nonnull
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
	{
		return super.transferStackInSlot(player, slot);
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
//				int i = -1;
//				int j = -1;
//				if(ArcFurnaceRecipe.isValidRecipeInput(stackInSlot))
//				{
//					i=0;
//					j=12;
//				}
//				else if(ArcFurnaceRecipe.isValidRecipeAdditive(stackInSlot))
//				{
//					i=12;
//					j=16;
//				}
//				else if(IEContent.itemGraphiteElectrode.equals(stack.getItem()))
//				{
//					i=23;
//					j=26;
//				}
//				if(i!=-1 && j!=-1)
//					if(!this.mergeItemStack(stackInSlot, i,j, false))
//						return null;
//			}
//
//			if (stackInSlot.stackSize == 0)
//				slotObject.putStack(null);
//			else
//				slotObject.onSlotChanged();
//
//			if (stackInSlot.stackSize == stack.stackSize)
//				return null;
//			slotObject.onTake(player, stackInSlot);
//		}
//		return stack;
	}
}