/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

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
		if(!ItemStack.areItemsEqual(stack, otherStack))
			return false;
		if(this.useNBT)
			return ItemStack.areItemStackTagsEqual(stack, otherStack);
		return true;
	}

	public CompoundNBT writeToNBT(CompoundNBT nbt)
	{
		nbt.put("stack", stack.write(new CompoundNBT()));
		nbt.putBoolean("useNBT", useNBT);
		return nbt;
	}

	public static ComparableItemStack readFromNBT(CompoundNBT nbt)
	{
		ComparableItemStack comp = new ComparableItemStack(ItemStack.read(nbt.getCompound("stack")), false);
		comp.useNBT = nbt.getBoolean("useNBT");
		return comp;
	}
}