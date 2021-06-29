/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.metal.ArcFurnaceTileEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;

public class ArcFurnaceContainer extends IEBaseContainer<ArcFurnaceTileEntity>
{
	public ArcFurnaceContainer(ContainerType<?> type, int id, PlayerInventory inventoryPlayer, ArcFurnaceTileEntity tile)
	{
		super(type, inventoryPlayer, tile, id);
		this.tile = tile;
		for(int i = 0; i < 12; i++)
			this.addSlot(new IESlot.ArcInput(this, this.inv, i, 10+i%3*21, 34+i/3*18));
		for(int i = 0; i < 4; i++)
			this.addSlot(new IESlot.ArcAdditive(this, this.inv, 12+i, 114+i%2*18, 34+i/2*18));
		for(int i = 0; i < 6; i++)
			this.addSlot(new IESlot.Output(this, this.inv, 16+i, 78+i%3*18, 80+i/3*18));
		this.addSlot(new IESlot.Output(this, this.inv, 22, 132, 98));

		this.addSlot(new IESlot.ArcElectrode(this, this.inv, 23, 62, 10));
		this.addSlot(new IESlot.ArcElectrode(this, this.inv, 24, 80, 10));
		this.addSlot(new IESlot.ArcElectrode(this, this.inv, 25, 98, 10));

		slotCount = 26;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 126+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 184));
	}
}