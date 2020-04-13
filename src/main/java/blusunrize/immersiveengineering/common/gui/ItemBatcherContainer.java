/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.wooden.ItemBatcherTileEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;

public class ItemBatcherContainer extends IEBaseContainer<ItemBatcherTileEntity>
{
	public ItemBatcherContainer(int id, PlayerInventory inventoryPlayer, ItemBatcherTileEntity tile)
	{
		super(inventoryPlayer, tile, id);
		for(int i = 0; i < 9; i++)
			this.addSlot(new Slot(this.inv, i, 8+i*18, 31));
		for(int i = 0; i < 9; i++)
			this.addSlot(new Slot(this.inv, 9+i, 8+i*18, 63));

		this.slotCount = tile.getInventory().size();
		this.tile = tile;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 118+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 176));
	}

}