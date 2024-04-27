/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class ComparableItemStack
{
	public ItemStack stack;
	public boolean compareComponents;

	public ComparableItemStack(ItemStack stack)
	{
		this(stack, false);
	}

	public ComparableItemStack(ItemStack stack, boolean copy)
	{
		if(stack==null)
			throw new RuntimeException("You cannot instantiate a ComparableItemStack with null for an Item!");
		this.stack = stack;
		if(copy)
			copy();
	}

	public static ComparableItemStack create(ItemStack stack, boolean copy)
	{
		return create(stack, copy, !stack.getComponents().isEmpty());
	}

	public static ComparableItemStack create(ItemStack stack, boolean copy, boolean useNbt)
	{
		ComparableItemStack comp = new ComparableItemStack(stack, copy);
		comp.setCompareComponents(useNbt);
		return comp;
	}

	public void copy()
	{
		stack = stack.copy();
	}

	public ComparableItemStack setCompareComponents(boolean compareComponents)
	{
		this.compareComponents = compareComponents;
		return this;
	}

	@Override
	public String toString()
	{
		return "ComparableStack: {"+this.stack.toString()+"}; checkNBT: "+this.compareComponents;
	}

	@Override
	public int hashCode()
	{
		return ItemStack.hashItemAndComponents(this.stack);
	}

	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof ComparableItemStack))
			return false;

		ItemStack otherStack = ((ComparableItemStack)object).stack;
		if(!ItemStack.isSameItem(stack, otherStack))
			return false;
		if(this.compareComponents)
			return Objects.equals(stack.getComponents(), otherStack.getComponents());
		return true;
	}

	public CompoundTag writeToNBT(HolderLookup.Provider provider, CompoundTag nbt)
	{
		nbt.put("stack", stack.save(provider));
		nbt.putBoolean("useNBT", compareComponents);
		return nbt;
	}

	public static ComparableItemStack readFromNBT(HolderLookup.Provider provider, CompoundTag nbt)
	{
		ComparableItemStack comp = new ComparableItemStack(
				ItemStack.parse(provider, nbt.getCompound("stack")).orElseThrow(), false
		);
		comp.compareComponents = nbt.getBoolean("useNBT");
		return comp;
	}
}