/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.wooden.ItemBatcherTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemBatcherContainer extends IEBaseContainer<ItemBatcherTileEntity>
{
	public ItemBatcherContainer(int id, PlayerInventory inventoryPlayer, ItemBatcherTileEntity tile)
	{
		super(inventoryPlayer, tile, id);
		for(int i = 0; i < 9; i++)
			this.addSlot(new IESlot.Ghost(this, this.inv, i, 8+i*18, 30)
			{
				@Override
				public int getSlotStackLimit()
				{
					return 64;
				}
			});
		for(int i = 0; i < 9; i++)
			this.addSlot(new Slot(this.inv, 9+i, 8+i*18, 59));

		this.slotCount = tile.getInventory().size();
		this.tile = tile;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 118+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 176));
	}

	@Nonnull
	@Override
	public ItemStack transferStackInSlot(PlayerEntity player, int slot)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slotObject = this.inventorySlots.get(slot);
		if(slotObject!=null&&slotObject.getHasStack())
		{
			ItemStack itemstack1 = slotObject.getStack();
			itemstack = itemstack1.copy();
			if(slot < slotCount)
			{
				if(!this.mergeItemStack(itemstack1, slotCount, this.inventorySlots.size(), true))
					return ItemStack.EMPTY;
			}
			// exclude ghost slots from shiftclick
			else if(!this.mergeItemStack(itemstack1, 9, slotCount, false))
			{
				return ItemStack.EMPTY;
			}

			if(itemstack1.isEmpty())
				slotObject.putStack(ItemStack.EMPTY);
			else
				slotObject.onSlotChanged();
		}
		return itemstack;
	}
}