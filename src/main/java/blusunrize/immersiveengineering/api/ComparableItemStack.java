package blusunrize.immersiveengineering.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ComparableItemStack
{
	public ItemStack stack;
	public boolean useNBT;

	public ComparableItemStack(ItemStack stack)
	{
		this.stack = stack;
	}

	public ComparableItemStack setUseNBT(boolean useNBT)
	{
		this.useNBT = useNBT;
		return this;
	}


	@Override
	public String toString()
	{
		return "ComparableStack: {"+this.stack.toString()+"}; checkNBT: "+useNBT;
	}
	@Override
	public int hashCode()
	{
		return this.stack.getItemDamage()&0xFFFF | Item.getIdFromItem(this.stack.getItem())<<16;
	}

	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof ComparableItemStack))
			return false;

		ItemStack otherStack = ((ComparableItemStack)object).stack;
		if(!OreDictionary.itemMatches(stack,otherStack, false))
			return false;
		if(this.useNBT)
		{
			if(this.stack.stackTagCompound==null && otherStack.stackTagCompound!=null)
				return false;
			if(this.stack.stackTagCompound!=null && otherStack.stackTagCompound==null)
				return false;
			if(!this.stack.stackTagCompound.equals(otherStack.stackTagCompound))
				return false;
		}
		return true;
	}
}