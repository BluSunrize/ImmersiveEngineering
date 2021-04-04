/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.common.blocks.wooden.CircuitTableTileEntity;
import invtweaks.api.container.ChestContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;

@ChestContainer
public class CircuitTableContainer extends IEBaseContainer<CircuitTableTileEntity>
{
	private final PlayerEntity player;

	public CircuitTableContainer(int id, PlayerInventory inventoryPlayer, CircuitTableTileEntity tile)
	{
		super(inventoryPlayer, tile, id);
		this.player = inventoryPlayer.player;

		int slotCount = 0;

		this.addSlot(new IESlot.Tagged(this, this.inv, slotCount++, 8, 7, IETags.circuitPCB));
		this.addSlot(new IESlot.Tagged(this, this.inv, slotCount++, 8, 25, IETags.circuitLogic));
		this.addSlot(new IESlot.Tagged(this, this.inv, slotCount++, 8, 43, IETags.circuitTraces));
		this.addSlot(new IESlot.Tagged(this, this.inv, slotCount++, 8, 61, IETags.circuitSolder));

		this.addSlot(new IESlot.Output(this, this.inv, slotCount, 194, 56));

		this.slotCount = 1;
		this.tile = tile;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 143));
	}
}