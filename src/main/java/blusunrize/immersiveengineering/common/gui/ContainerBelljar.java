/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBelljar;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

import static blusunrize.immersiveengineering.common.blocks.metal.TileEntityBelljar.*;

public class ContainerBelljar extends ContainerIEBase
{
	public ContainerBelljar(InventoryPlayer inventoryPlayer, TileEntityBelljar tile)
	{
		super(inventoryPlayer, tile);
		this.addSlotToContainer(new IESlot.Belljar(SLOT_SOIL, this, this.inv, SLOT_SOIL, 62, 54));
		this.addSlotToContainer(new IESlot.Belljar(SLOT_SEED, this, this.inv, SLOT_SEED, 62, 34));
		this.addSlotToContainer(new IESlot.Belljar(SLOT_FERTILIZER, this, this.inv, SLOT_FERTILIZER, 8, 59));

		for(int i = 0; i < 4; i++)
			this.addSlotToContainer(new IESlot.Output(this, this.inv, 3+i, 116+i%2*18, 34+i/2*18));

		this.slotCount = 7;
		this.tile = tile;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));
		for(int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 143));
	}
}