/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.wooden.TileEntitySorter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ContainerSorter extends ContainerIEBase<TileEntitySorter>
{
	int slotCount;

	public ContainerSorter(InventoryPlayer inventoryPlayer, TileEntitySorter tile)
	{
		super(inventoryPlayer, tile);
		this.tile = tile;
		for(int side = 0; side < 6; side++)
			for(int i = 0; i < TileEntitySorter.filterSlotsPerSide; i++)
			{
				int x = 4+(side/2)*58+(i < 3?i*18: i > 4?(i-5)*18: i==3?0: 36);
				int y = 22+(side%2)*76+(i < 3?0: i > 4?36: 18);
				int id = side*TileEntitySorter.filterSlotsPerSide+i;
				this.addSlotToContainer(new IESlot.Ghost(this, tile.filter, id, x, y));
			}
		slotCount = 6*TileEntitySorter.filterSlotsPerSide;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 163+i*18));
		for(int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 221));
	}

	@Override
	public boolean canInteractWith(@Nonnull EntityPlayer player)
	{
		return tile!=null&&tile.getWorld().getTileEntity(tile.getPos())==tile&&player.getDistanceSq(tile.getPos().getX()+.5, tile.getPos().getY()+.5, tile.getPos().getZ()+.5) <= 64;
	}

	@Nonnull
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
	{
		return ItemStack.EMPTY;
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
		//			slotObject.onTake(player, stackInSlot);
		//		}
		//		return stack;
	}
}