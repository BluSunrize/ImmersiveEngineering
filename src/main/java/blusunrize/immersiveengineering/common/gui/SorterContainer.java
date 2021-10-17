/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.wooden.SorterTileEntity;
import javax.annotation.Nonnull;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SorterContainer extends IEBaseContainer<SorterTileEntity>
{
	int slotCount;

	public SorterContainer(int windowId, Inventory inventoryPlayer, SorterTileEntity tile)
	{
		super(tile, windowId);
		this.tile = tile;
		for(int side = 0; side < 6; side++)
			for(int i = 0; i < SorterTileEntity.filterSlotsPerSide; i++)
			{
				int x = 4+(side/2)*58+(i < 3?i*18: i > 4?(i-5)*18: i==3?0: 36);
				int y = 22+(side%2)*76+(i < 3?0: i > 4?36: 18);
				int id = side*SorterTileEntity.filterSlotsPerSide+i;
				this.addSlot(new IESlot.ItemHandlerGhost(tile.filter, id, x, y));
			}
		slotCount = 6*SorterTileEntity.filterSlotsPerSide;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 163+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 221));
	}

	@Override
	public boolean stillValid(@Nonnull Player player)
	{
		return tile!=null&&tile.getWorldNonnull().getBlockEntity(tile.getBlockPos())==tile&&player.distanceToSqr(tile.getBlockPos().getX()+.5, tile.getBlockPos().getY()+.5, tile.getBlockPos().getZ()+.5) <= 64;
	}

	@Nonnull
	@Override
	public ItemStack quickMoveStack(Player player, int slot)
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