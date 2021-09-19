/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.wooden.LogicUnitTileEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class LogicUnitContainer extends IEBaseContainer<LogicUnitTileEntity>
{
	public LogicUnitContainer(int id, Inventory inventoryPlayer, LogicUnitTileEntity tile)
	{
		super(inventoryPlayer, tile, id);
		for(int i = 0; i < tile.getInventory().size(); i++)
			this.addSlot(new Slot(this.inv, i, 44+(i%5)*18, 19+(i/5)*18));
		this.slotCount = tile.getInventory().size();

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 143));
	}
}