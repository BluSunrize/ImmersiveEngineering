/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ComparableItemStack
{
	public ItemStack stack;
	public boolean useNBT;

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
		return create(stack, copy, stack.hasTag()&&!stack.getOrCreateTag().isEmpty());
	}

	public static ComparableItemStack create(ItemStack stack, boolean copy, boolean useNbt)
	{
		ComparableItemStack comp = new ComparableItemStack(stack, copy);
		comp.setUseNBT(useNbt);
		return comp;
	}

	public void copy()
	{
		stack = stack.copy();
	}

	public ComparableItemStack setUseNBT(boolean useNBT)
	{
		this.useNBT = useNBT;
		return this;
	}

	@Override
	public String toString()
	{
		return "ComparableStack: {"+this.stack.toString()+"}; checkNBT: "+this.useNBT;
	}

	@Override
	public int hashCode()
	{
		int hash = stack.getItem().hashCode();
		if(this.useNBT&&stack.hasTag())
			hash += stack.getOrCreateTag().hashCode()*31;
		return hash;
	}

	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof ComparableItemStack))
			return false;

		ItemStack otherStack = ((ComparableItemStack)object).stack;
		if(!ItemStack.isSame(stack, otherStack))
			return false;
		if(this.useNBT)
			return ItemStack.tagMatches(stack, otherStack);
		return true;
	}

	public CompoundTag writeToNBT(CompoundTag nbt)
	{
		nbt.put("stack", stack.save(new CompoundTag()));
		nbt.putBoolean("useNBT", useNBT);
		return nbt;
	}

	public static ComparableItemStack readFromNBT(CompoundTag nbt)
	{
		ComparableItemStack comp = new ComparableItemStack(ItemStack.of(nbt.getCompound("stack")), false);
		comp.useNBT = nbt.getBoolean("useNBT");
		return comp;
	}
}