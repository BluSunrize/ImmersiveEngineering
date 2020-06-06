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
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.Map;
import java.util.Map.Entry;

public class ArcRecyclingRecipe extends ArcFurnaceRecipe
{
	private Map<ItemStack, Double> outputs;

	public ArcRecyclingRecipe(ResourceLocation id, Map<ItemStack, Double> outputs, IngredientWithSize input, int time, int energyPerTick)
	{
		super(id, outputs.keySet().stream().collect(NonNullList::create, AbstractList::add, AbstractCollection::addAll),
				input, ItemStack.EMPTY, time, energyPerTick);
		this.outputs = outputs;
		this.setSpecialRecipeType("Recycling");
		this.outputList = NonNullList.create();
		for(Entry<ItemStack, Double> e : outputs.entrySet())
		{
			double scaledOut = e.getValue();
			addOutputToList(scaledOut, outputList, e);
		}
	}

	@Override
	public NonNullList<ItemStack> getOutputs(ItemStack input, NonNullList<ItemStack> additives)
	{
		if(outputs==null)
			return NonNullList.create();
		float mod;
		if(!input.isDamageable())
			mod = 1;
		else
			mod = (input.getMaxDamage()-input.getDamage())/(float)input.getMaxDamage();
		NonNullList<ItemStack> outs = NonNullList.create();
		for(Entry<ItemStack, Double> e : outputs.entrySet())
		{
			double scaledOut = mod*e.getValue();
			addOutputToList(scaledOut, outs, e);
		}
		return outs;
	}

	private void addOutputToList(double scaledOut, NonNullList<ItemStack> outs, Entry<ItemStack, Double> e)
	{
		//Noone likes nuggets anyway >_>
		if(scaledOut >= 1)
			outs.add(Utils.copyStackWithAmount(e.getKey(), (int)scaledOut));
		int nuggetOut = (int)((scaledOut-(int)scaledOut)*9);
		if(nuggetOut > 0)
		{
			String[] type = ApiUtils.getMetalComponentTypeAndMetal(e.getKey(), "ingots");
			if(type!=null)
			{
				ItemStack nuggets = IEApi.getPreferredTagStack(IETags.getNugget(type[1]));
				outs.add(Utils.copyStackWithAmount(nuggets, nuggetOut));
			}
		}
	}

	@Override
	public boolean matches(ItemStack input, NonNullList<ItemStack> additives)
	{
		return !input.isEmpty()&&this.input.test(input);
	}
}
