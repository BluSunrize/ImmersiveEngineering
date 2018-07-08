/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

public class ComparableItemStack
{
	public ItemStack stack;
	public boolean useNBT;
	public int oreID = -1;

	public ComparableItemStack(ItemStack stack)
	{
		this(stack, true);
	}

	public ComparableItemStack(ItemStack stack, boolean matchOre)
	{
		this(stack, matchOre, true);
	}

	public ComparableItemStack(ItemStack stack, boolean matchOre, boolean copy)
	{
		if(stack==null)
			throw new RuntimeException("You cannot instantiate a ComparableItemStack with null for an Item!");
		this.stack = stack;
		if(copy)
			copy();
		if(matchOre)
		{
			int[] oids = OreDictionary.getOreIDs(stack);
			if(oids!=null&&oids.length > 0)
				this.oreID = oids[0];
		}
	}

	public void copy()
	{
		stack = stack.copy();
	}

	public ComparableItemStack(String oreName)
	{
		this(IEApi.getPreferredOreStack(oreName), true, false);
		this.oreID = OreDictionary.getOreID(oreName);
	}

	public ComparableItemStack setUseNBT(boolean useNBT)
	{
		this.useNBT = useNBT;
		return this;
	}

	public ComparableItemStack setOreID(int oid)
	{
		this.oreID = oid;
		return this;
	}

	@Override
	public String toString()
	{
		return "ComparableStack: {"+this.stack.toString()+"}; oreID: "+this.oreID+"; checkNBT: "+this.useNBT;
	}

	@Override
	public int hashCode()
	{
		if(this.oreID!=-1)
			return this.oreID;
		int hash = (stack.getItemDamage()&0xffff)*31+stack.getItem().hashCode()*31;
		if(this.useNBT&&stack.hasTagCompound())
			hash += stack.getTagCompound().hashCode()*31;
		return hash;
	}

	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof ComparableItemStack))
			return false;

		if(this.oreID!=-1&&((ComparableItemStack)object).oreID!=-1)
			return this.oreID==((ComparableItemStack)object).oreID;

		ItemStack otherStack = ((ComparableItemStack)object).stack;
		if(!OreDictionary.itemMatches(stack, otherStack, false))
			return false;
		if(this.useNBT)
			return ItemStack.areItemStackTagsEqual(stack, otherStack);
		return true;
	}


	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		if(this.oreID!=-1)
			nbt.setString("oreID", OreDictionary.getOreName(oreID));
		else
		{
			nbt.setTag("stack", stack.writeToNBT(new NBTTagCompound()));
			nbt.setBoolean("useNBT", useNBT);
		}
		return nbt;
	}

	public static ComparableItemStack readFromNBT(NBTTagCompound nbt)
	{
		if(nbt.hasKey("oreID"))
			return new ComparableItemStack(nbt.getString("oreID"));
		else if(nbt.hasKey("stack"))
		{
			ComparableItemStack comp = new ComparableItemStack(new ItemStack(nbt.getCompoundTag("stack")), true, false);
			comp.useNBT = nbt.getBoolean("useNBT");
			return comp;
		}
		return null;
	}
}