/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityBlastFurnace;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerBlastFurnace extends ContainerIEBase<TileEntityBlastFurnace>
{
	public ContainerBlastFurnace(InventoryPlayer inventoryPlayer, TileEntityBlastFurnace tile)
	{
		super(inventoryPlayer, tile);

		this.addSlotToContainer(new IESlot(this, this.inv, 0, 52, 17)
		{
			@Override
			public boolean isItemValid(ItemStack itemStack)
			{
				return BlastFurnaceRecipe.findRecipe(itemStack)!=null;
			}
		});
		this.addSlotToContainer(new IESlot.BlastFuel(this, this.inv, 1, 52, 53));
		this.addSlotToContainer(new IESlot.Output(this, this.inv, 2, 112, 17));
		this.addSlotToContainer(new IESlot.Output(this, this.inv, 3, 112, 53));
		slotCount = 4;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18));
		for(int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 142));
	}
//
//	@Override
//	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
//	{
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
//				if(BlastFurnaceRecipe.findRecipe(stackInSlot)!=null)
//					i=0;
//				else if(BlastFurnaceRecipe.isValidBlastFuel(stackInSlot))
//					i=1;
//				if(i!=-1)
//					if(!this.mergeItemStack(stackInSlot, i,i+1, false))
//						return null;
//
//				//				for(int i=0;i<slotCount;i++)
//				//					if(this.getSlot(i).isItemValid(stackInSlot))
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
//	}
}