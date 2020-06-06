/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.wooden.FluidSorterTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class FluidSorterContainer extends IEBaseContainer<FluidSorterTileEntity>
{
	int slotCount;

	public FluidSorterContainer(int id, PlayerInventory inventoryPlayer, FluidSorterTileEntity tile)
	{
		super(inventoryPlayer, tile, id);
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 163+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 221));
	}

	@Nonnull
	@Override
	public ItemStack transferStackInSlot(PlayerEntity player, int slot)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canInteractWith(@Nonnull PlayerEntity player)
	{
		if(tile instanceof IInteractionObjectIE&&!((IInteractionObjectIE)tile).canUseGui(player))
			return false;
		return !tile.isRemoved()&&tile.getDistanceSq(player.posX, player.posY, player.posZ) < 64;
	}
}
