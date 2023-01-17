/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic.arcfurnace;

import blusunrize.immersiveengineering.common.util.inventory.InsertOnlyInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static blusunrize.immersiveengineering.common.blocks.multiblocks.logic.arcfurnace.ArcFurnaceLogic.FIRST_IN_SLOT;
import static blusunrize.immersiveengineering.common.blocks.multiblocks.logic.arcfurnace.ArcFurnaceLogic.IN_SLOT_COUNT;

public class ArcFurnaceInputHandler extends InsertOnlyInventory
{
	private final IItemHandlerModifiable wrapped;
	private final Runnable onChanged;

	public ArcFurnaceInputHandler(IItemHandlerModifiable wrapped, Runnable onChanged)
	{
		this.wrapped = wrapped;
		this.onChanged = onChanged;
	}

	@Override
	protected ItemStack insert(ItemStack stack, boolean simulate)
	{
		if(stack.isEmpty())
			return stack;
		stack = stack.copy();
		List<Integer> possibleSlots = new ArrayList<>(IN_SLOT_COUNT);
		for(int i = FIRST_IN_SLOT; i < IN_SLOT_COUNT; i++)
		{
			ItemStack here = wrapped.getStackInSlot(i);
			if(here.isEmpty())
			{
				if(!simulate)
					wrapped.setStackInSlot(i, stack);
				onChanged.run();
				return ItemStack.EMPTY;
			}
			else if(ItemHandlerHelper.canItemStacksStack(stack, here)&&here.getCount() < here.getMaxStackSize())
				possibleSlots.add(i);
		}
		possibleSlots.sort(Comparator.comparingInt(a -> wrapped.getStackInSlot(a).getCount()));
		for(int i : possibleSlots)
		{
			ItemStack here = wrapped.getStackInSlot(i);
			int fillCount = Math.min(here.getMaxStackSize()-here.getCount(), stack.getCount());
			if(!simulate)
				here.grow(fillCount);
			stack.shrink(fillCount);
			if(stack.isEmpty())
			{
				onChanged.run();
				return ItemStack.EMPTY;
			}
		}
		onChanged.run();
		return stack;
	}

	@Override
	public int getSlots()
	{
		return 26;
	}
}
