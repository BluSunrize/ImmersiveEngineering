/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.stone.AlloySmelterTileEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class AlloySmelterContainer extends IEBaseContainer<AlloySmelterTileEntity>
{
	public AlloySmelterContainer(int id, Inventory inventoryPlayer, AlloySmelterTileEntity tile)
	{
		super(inventoryPlayer, tile, id);

		this.addSlot(new Slot(this.inv, 0, 38, 17));
		this.addSlot(new Slot(this.inv, 1, 66, 17));
		this.addSlot(new IESlot.IEFurnaceSFuelSlot(this, this.inv, 2, 52, 53));
		this.addSlot(new IESlot.Output(this, this.inv, 3, 120, 35));
		slotCount = 4;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 142));
		addDataSlots(tile.guiState);
	}
}