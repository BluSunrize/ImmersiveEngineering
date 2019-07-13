/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.stone.AlloySmelterTileEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.FurnaceFuelSlot;
import net.minecraft.inventory.container.Slot;

public class ContainerAlloySmelter extends ContainerIEBase<AlloySmelterTileEntity>
{
	public ContainerAlloySmelter(PlayerInventory inventoryPlayer, AlloySmelterTileEntity tile)
	{
		super(inventoryPlayer, tile);

		this.addSlot(new Slot(this.inv, 0, 38, 17));
		this.addSlot(new Slot(this.inv, 1, 66, 17));
		this.addSlot(new FurnaceFuelSlot(this.inv, 2, 52, 53));
		this.addSlot(new IESlot.Output(this, this.inv, 3, 120, 35));
		slotCount = 4;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 142));
	}
}