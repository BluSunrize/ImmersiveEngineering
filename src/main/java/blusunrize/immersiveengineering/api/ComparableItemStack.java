package blusunrize.immersiveengineering.api;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ComparableItemStack
{
	public ItemStack stack;
	public boolean useNBT;
	public int oreID=-1;

	public ComparableItemStack(ItemStack stack)
	{
		if(stack==null)
			throw new RuntimeException("You cannot instantiate a ComparableItemStack with null for an Item!");
		this.stack = stack;
		int[] oids = OreDictionary.getOreIDs(stack);
		if(oids!=null && oids.length>0)
			this.oreID = oids[0];
	}
	public ComparableItemStack(String oreName)
	{
		this(IEApi.getPreferredOreStack(oreName));
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
		//return this.stack.getItemDamage()&0xFFFF | Item.getIdFromItem(this.stack.getItem())<<16;
		return (stack.getItemDamage()&0xffff)*31 + stack.getItem().hashCode()*31;
	}

	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof ComparableItemStack))
			return false;

		if(this.oreID!=-1 && ((ComparableItemStack)object).oreID!=-1)
			return this.oreID == ((ComparableItemStack)object).oreID;

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