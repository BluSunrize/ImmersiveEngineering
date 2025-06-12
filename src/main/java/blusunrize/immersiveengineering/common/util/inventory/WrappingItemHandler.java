/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.inventory;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record WrappingItemHandler(
		IItemHandler wrapped,
		boolean allowInsert,
		boolean allowExtract,
		List<WrappingItemHandler.IntRange> allowedRanges
) implements IItemHandler
{

	public WrappingItemHandler(IItemHandler wrapped, boolean allowInsert, boolean allowExtract, IntRange allowed)
	{
		this(wrapped, allowInsert, allowExtract, List.of(allowed));
	}

	public WrappingItemHandler(IItemHandler wrapped, boolean allowInsert, boolean allowExtract)
	{
		this(wrapped, allowInsert, allowExtract, new IntRange(0, wrapped.getSlots()));
	}

	@Override
	public int getSlots()
	{
		return wrapped.getSlots();
	}

	@Override
	public @NotNull ItemStack getStackInSlot(int slot)
	{
		return wrapped.getStackInSlot(slot);
	}

	@Override
	public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate)
	{
		if(!allowInsert||!isAcessible(slot))
			return stack;
		return wrapped.insertItem(slot, stack, simulate);
	}

	@Override
	public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		if(!allowExtract||!isAcessible(slot))
			return ItemStack.EMPTY;
		return wrapped.extractItem(slot, amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return Math.min(64, wrapped.getSlotLimit(slot));
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack)
	{
		return isAcessible(slot)&&wrapped.isItemValid(slot, stack);
	}

	private boolean isAcessible(int slot)
	{
		for(final IntRange range : allowedRanges)
			if(range.contains(slot))
				return true;
		return false;
	}

	public record IntRange(int first, int firstAfter)
	{
		private boolean contains(int slotId)
		{
			return slotId >= first&&slotId < firstAfter;
		}
	}
}
