/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityFluidSorter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class ContainerFluidSorter extends Container
{
	TileEntityFluidSorter tile;
	int slotCount;

	public ContainerFluidSorter(PlayerInventory inventoryPlayer, TileEntityFluidSorter tile)
	{
		this.tile = tile;
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 163+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 221));
	}

	@Override
	public boolean canInteractWith(PlayerEntity player)
	{
		return tile!=null&&tile.getWorld().getTileEntity(tile.getPos())==tile&&player.getDistanceSq(tile.getPos().getX()+.5, tile.getPos().getY()+.5, tile.getPos().getZ()+.5) <= 64;
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity player, int slot)
	{
		return ItemStack.EMPTY;
	}
}