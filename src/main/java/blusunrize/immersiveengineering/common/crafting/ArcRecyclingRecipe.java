package blusunrize.immersiveengineering.common.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.item.ItemStack;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.common.util.Utils;

public class ArcRecyclingRecipe extends ArcFurnaceRecipe
{
	HashMap<ItemStack,Double> outputs;
	public ArcRecyclingRecipe(HashMap<ItemStack,Double> outputs, Object input, int time, int energyPerTick)
	{
		super(null,input,null, time, energyPerTick);
		this.outputs = outputs;
		this.setSpecialRecipeType("Recycling");
	}

	public ItemStack[] getOutputs(ItemStack input, ItemStack[] additives)
	{
		if(outputs==null)
			return new ItemStack[0];
		float mod = !input.getItem().isDamageable()?1:(input.getMaxDamage()-input.getItemDamage())/(float)input.getMaxDamage();
		ArrayList<ItemStack> outs = new ArrayList<ItemStack>();
		for(Entry<ItemStack,Double> e : outputs.entrySet())
		{
			double scaledOut = mod*e.getValue();
			//Noone likes nuggets anyway >_>
			if(scaledOut>=1)
				outs.add(Utils.copyStackWithAmount(e.getKey(),(int)scaledOut));
			int nuggetOut = (int)((scaledOut-(int)scaledOut)*9);
			if(nuggetOut>0)
			{
				String[] type = ApiUtils.getMetalComponentTypeAndMetal(e.getKey(), "ingot");
				if(type!=null)
				{
					ItemStack nuggets = IEApi.getPreferredOreStack("nugget"+type[1]);
					outs.add(Utils.copyStackWithAmount(nuggets,(int)nuggetOut));
				}
			}
		}
		return outs.toArray(new ItemStack[outs.size()]);
	}
	public boolean matches(ItemStack input, ItemStack[] additives)
	{
		if (input!=null && this.input instanceof ItemStack)
		{
			boolean ignoreMeta = input.isItemStackDamageable();
			ItemStack inStack = (ItemStack) this.input;
			return input.getItem().equals(inStack.getItem()) && (ignoreMeta || inStack.getItemDamage()==input.getItemDamage());
		}
		return false;
	}
}
