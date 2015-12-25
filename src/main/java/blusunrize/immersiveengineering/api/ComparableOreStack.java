package blusunrize.immersiveengineering.api;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ComparableOreStack extends ComparableItemStack
{
	int oreID;
	public ComparableOreStack(String oreName)
	{
		super(IEApi.getPreferredOreStack(oreName));
		this.oreID = OreDictionary.getOreID(oreName);
	}


	@Override
	public String toString()
	{
		return "ComparableOreStack: {OreID:"+this.oreID+", Stack: "+this.stack.toString()+"}";
	}
	@Override
	public int hashCode()
	{
		return oreID!=-1?oreID:super.hashCode();
	}

	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof ComparableItemStack))
			return false;

		if(object instanceof ComparableOreStack)
			return ((ComparableOreStack)object).oreID == this.oreID;
		
		ItemStack otherStack = ((ComparableItemStack)object).stack;
		int[] ids = OreDictionary.getOreIDs(otherStack);
		for(int id : ids)
			if(id==this.oreID)
				return true;
		return false;
	}
}