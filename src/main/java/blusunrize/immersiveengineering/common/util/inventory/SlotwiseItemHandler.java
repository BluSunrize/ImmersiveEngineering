/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.inventory;

import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class SlotwiseItemHandler implements IItemHandlerModifiable, Iterable<ItemStack>
{
	private final ItemStackHandler rawHandler;
	private final List<IOConstraint> slotConstraints;

	public static SlotwiseItemHandler makeWithGroups(Runnable onChanged, IOConstraintGroup... constraintGroups)
	{
		return makeWithGroups(Arrays.asList(constraintGroups), onChanged);
	}

	public static SlotwiseItemHandler onSlotRange(IOConstraint constraint, int min, int count, Runnable onChanged)
	{
		return SlotwiseItemHandler.makeWithGroups(
				onChanged,
				new IOConstraintGroup(IOConstraint.BLOCKED, min),
				new IOConstraintGroup(IOConstraint.ANY_INPUT, count)
		);
	}

	public static SlotwiseItemHandler makeWithGroups(List<IOConstraintGroup> constraintGroups, Runnable onChanged)
	{
		List<IOConstraint> slotConstraints = new ArrayList<>();
		for(final IOConstraintGroup group : constraintGroups)
			for(int i = 0; i < group.slotCount; ++i)
				slotConstraints.add(group.constraint);
		return new SlotwiseItemHandler(slotConstraints, onChanged);
	}

	public SlotwiseItemHandler(List<IOConstraint> slotConstraints, Runnable onChanged)
	{
		this.rawHandler = new ItemStackHandler(slotConstraints.size())
		{
			@Override
			protected void onContentsChanged(int slot)
			{
				super.onContentsChanged(slot);
				onChanged.run();
			}
		};
		this.slotConstraints = slotConstraints;
	}

	@Override
	public int getSlots()
	{
		return rawHandler.getSlots();
	}

	@Override
	public @NotNull ItemStack getStackInSlot(int slot)
	{
		return rawHandler.getStackInSlot(slot);
	}

	@Override
	@NotNull
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate)
	{
		if(slot >= this.slotConstraints.size()||!this.slotConstraints.get(slot).allowInsert.test(stack))
			return stack;
		return rawHandler.insertItem(slot, stack, simulate);
	}

	@Override
	@NotNull
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		if(slot >= this.slotConstraints.size()||!this.slotConstraints.get(slot).allowExtract())
			return ItemStack.EMPTY;
		return rawHandler.extractItem(slot, amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return Math.min(64, rawHandler.getSlotLimit(slot));
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack)
	{
		// TODO may not be entirely correct
		return rawHandler.isItemValid(slot, stack);
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack)
	{
		rawHandler.setStackInSlot(slot, stack);
	}

	public Tag serializeNBT(Provider provider)
	{
		return rawHandler.serializeNBT(provider);
	}

	public void deserializeNBT(Provider provider, CompoundTag nbt)
	{
		rawHandler.deserializeNBT(provider, nbt);
	}

	public ItemStackHandler getRawHandler()
	{
		return rawHandler;
	}

	@Nonnull
	@Override
	public Iterator<ItemStack> iterator()
	{
		return new Iterator<>()
		{
			private int slot = 0;

			@Override
			public boolean hasNext()
			{
				return slot < getSlots();
			}

			@Override
			public ItemStack next()
			{
				final ItemStack next = getStackInSlot(slot);
				++slot;
				return next;
			}
		};
	}

	public record IOConstraint(boolean allowExtract, Predicate<ItemStack> allowInsert)
	{
		public static final IOConstraint OUTPUT = new IOConstraint(true, $ -> false);
		public static final IOConstraint ANY_INPUT = new IOConstraint(false, $ -> true);
		public static final IOConstraint FLUID_INPUT = IOConstraint.input(Utils::isFluidRelatedItemStack);
		public static final IOConstraint NO_CONSTRAINT = new IOConstraint(true, $ -> true);
		public static final IOConstraint BLOCKED = new IOConstraint(false, $ -> false);

		public static IOConstraint input(Predicate<ItemStack> allow)
		{
			return new IOConstraint(false, allow);
		}
	}

	public record IOConstraintGroup(IOConstraint constraint, int slotCount)
	{
	}
}
