/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.Map;
import java.util.Map.Entry;

public class ArcRecyclingRecipe extends ArcFurnaceRecipe
{
	private Map<ItemStack, Double> outputs;

	public ArcRecyclingRecipe(Map<ItemStack, Double> outputs, Object input, int time, int energyPerTick)
	{
		super(null, input, ItemStack.EMPTY, time, energyPerTick);
		this.outputs = outputs;
		this.setSpecialRecipeType("Recycling");
		this.outputList = NonNullList.create();
		for(Entry<ItemStack, Double> e : outputs.entrySet())
		{
			double scaledOut = e.getValue();
			//Noone likes nuggets anyway >_>
			if(scaledOut >= 1)
				outputList.add(Utils.copyStackWithAmount(e.getKey(), (int)scaledOut));
			int nuggetOut = (int)((scaledOut-(int)scaledOut)*9);
			if(nuggetOut > 0)
			{
				String[] type = ApiUtils.getMetalComponentTypeAndMetal(e.getKey(), "ingot");
				if(type!=null)
				{
					ItemStack nuggets = IEApi.getPreferredOreStack("nugget"+type[1]);
					outputList.add(Utils.copyStackWithAmount(nuggets, nuggetOut));
				}
			}
		}
	}

	@Override
	public NonNullList<ItemStack> getOutputs(ItemStack input, NonNullList<ItemStack> additives)
	{
		if(outputs==null)
			return NonNullList.create();
		float mod = !input.isItemStackDamageable()?1: (input.getMaxDamage()-input.getItemDamage())/(float)input.getMaxDamage();
		NonNullList<ItemStack> outs = NonNullList.create();
		for(Entry<ItemStack, Double> e : outputs.entrySet())
		{
			double scaledOut = mod*e.getValue();
			//Noone likes nuggets anyway >_>
			if(scaledOut >= 1)
				outs.add(Utils.copyStackWithAmount(e.getKey(), (int)scaledOut));
			int nuggetOut = (int)((scaledOut-(int)scaledOut)*9);
			if(nuggetOut > 0)
			{
				String[] type = ApiUtils.getMetalComponentTypeAndMetal(e.getKey(), "ingot");
				if(type!=null)
				{
					ItemStack nuggets = IEApi.getPreferredOreStack("nugget"+type[1]);
					outs.add(Utils.copyStackWithAmount(nuggets, nuggetOut));
				}
			}
		}
		return outs;
	}

	@Override
	public boolean matches(ItemStack input, NonNullList<ItemStack> additives)
	{
		return !input.isEmpty()&&this.input.matchesItemStack(input);
	}
}
