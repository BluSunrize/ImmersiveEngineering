/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.Collection;
import java.util.HashSet;

public class ComparableItemStack
{
	public ItemStack stack;
	public boolean useNBT;
	public Collection<ResourceLocation> tags;

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
			tags = ImmutableList.copyOf(ApiUtils.getMatchingTagNames(stack));
	}

	public ComparableItemStack(Collection<ResourceLocation> tags)
	{
		this.tags = tags;
	}

	public void copy()
	{
		stack = stack.copy();
	}

	public ComparableItemStack(ResourceLocation tag)
	{
		this(IEApi.getPreferredTagStack(tag), true, false);
		tags = ImmutableList.of(tag);
	}

	public ComparableItemStack setUseNBT(boolean useNBT)
	{
		this.useNBT = useNBT;
		return this;
	}

	@Override
	public String toString()
	{
		return "ComparableStack: {"+this.stack.toString()+"}; tags: "+this.tags+"; checkNBT: "+this.useNBT;
	}

	@Override
	public int hashCode()
	{
		if(hasTags())
			return this.tags.hashCode();
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

		if(this.hasTags()&&((ComparableItemStack)object).hasTags())
			return this.tags.equals(((ComparableItemStack)object).tags);

		ItemStack otherStack = ((ComparableItemStack)object).stack;
		if(!ItemStack.areItemsEqual(stack, otherStack))
			return false;
		if(this.useNBT)
			return ItemStack.areItemStackTagsEqual(stack, otherStack);
		return true;
	}

	private boolean hasTags()
	{
		return tags!=null&&!tags.isEmpty();
	}

	public CompoundNBT writeToNBT(CompoundNBT nbt)
	{
		if(hasTags())
		{
			ListNBT tagList = new ListNBT();
			for(ResourceLocation rl : tags)
				tagList.add(StringNBT.valueOf(rl.toString()));
			nbt.put("tags", tagList);
		}
		else
		{
			nbt.put("stack", stack.write(new CompoundNBT()));
			nbt.putBoolean("useNBT", useNBT);
		}
		return nbt;
	}

	public static ComparableItemStack readFromNBT(CompoundNBT nbt)
	{
		if(nbt.contains("tags", NBT.TAG_LIST))
		{
			Collection<ResourceLocation> tags = new HashSet<>();
			ListNBT list = nbt.getList("tags", NBT.TAG_STRING);
			for(int i = 0; i < list.size(); ++i)
				tags.add(new ResourceLocation(list.getString(i)));
			return new ComparableItemStack(tags);
		}
		else if(nbt.contains("stack", NBT.TAG_COMPOUND))
		{
			ComparableItemStack comp = new ComparableItemStack(ItemStack.read(nbt.getCompound("stack")), true, false);
			comp.useNBT = nbt.getBoolean("useNBT");
			return comp;
		}
		return null;
	}
}