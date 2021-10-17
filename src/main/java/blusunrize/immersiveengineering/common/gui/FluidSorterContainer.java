/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.wooden.FluidSorterBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class FluidSorterContainer extends IEBaseContainer<FluidSorterBlockEntity>
{
	int slotCount;

	public FluidSorterContainer(MenuType<?> type, int id, Inventory inventoryPlayer, FluidSorterBlockEntity tile)
	{
		super(type, tile, id);
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 163+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 221));
	}

	@Nonnull
	@Override
	public ItemStack quickMoveStack(Player player, int slot)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(@Nonnull Player player)
	{
		if(tile==null)
			return false;
		if(!((IInteractionObjectIE)tile).canUseGui(player))
			return false;
		return !tile.isRemoved()&&Vec3.atLowerCornerOf(tile.getBlockPos()).distanceToSqr(player.position()) < 64;
	}
}
