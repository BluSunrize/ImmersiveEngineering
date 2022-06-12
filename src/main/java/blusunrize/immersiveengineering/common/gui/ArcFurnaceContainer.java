/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.metal.ArcFurnaceBlockEntity;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

public class ArcFurnaceContainer extends IEBaseContainerOld<ArcFurnaceBlockEntity>
{
	public ArcFurnaceContainer(MenuType<?> type, int id, Inventory inventoryPlayer, ArcFurnaceBlockEntity tile)
	{
		super(type, tile, id);
		this.tile = tile;
		for(int i = 0; i < 12; i++)
			this.addSlot(new IESlot.ArcInput(this, this.inv, i, 10+i%3*21, 34+i/3*18, tile.getLevel()));
		for(int i = 0; i < 4; i++)
			this.addSlot(new IESlot.ArcAdditive(this, this.inv, 12+i, 114+i%2*18, 34+i/2*18, tile.getLevel()));
		for(int i = 0; i < 6; i++)
			this.addSlot(new IESlot.Output(this, this.inv, 16+i, 78+i%3*18, 80+i/3*18));
		this.addSlot(new IESlot.Output(this, this.inv, 22, 132, 98));

		this.addSlot(new IESlot.ArcElectrode(this, this.inv, 23, 62, 10));
		this.addSlot(new IESlot.ArcElectrode(this, this.inv, 24, 80, 10));
		this.addSlot(new IESlot.ArcElectrode(this, this.inv, 25, 98, 10));

		ownSlotCount = 26;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 126+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 184));
		addGenericData(GenericContainerData.energy(tile.energyStorage));
	}
}