/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.util.ListUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author BluSunrize - 01.05.2015
 * <p>
 * The recipe for the crusher
 */
public class CrusherRecipe extends MultiblockRecipe
{
	public static float energyModifier = 1;
	public static float timeModifier = 1;

	public final String oreInputString;
	public final IngredientStack input;
	public final ItemStack output;
	public ItemStack[] secondaryOutput;
	public float[] secondaryChance;

	public CrusherRecipe(ItemStack output, Object input, int energy)
	{
		this.output = output;
		this.input = ApiUtils.createIngredientStack(input);
		this.oreInputString = input instanceof String?(String)input: null;
		this.totalProcessEnergy = (int)Math.floor(energy*energyModifier);
		this.totalProcessTime = (int)Math.floor(50*timeModifier);

		this.inputList = Lists.newArrayList(this.input);
		this.outputList = ListUtils.fromItem(this.output);
	}

	@Override
	public NonNullList<ItemStack> getActualItemOutputs(TileEntity tile)
	{
		NonNullList<ItemStack> list = NonNullList.create();
		list.add(output);
		if(secondaryOutput!=null&&secondaryChance!=null)
			for(int i = 0; i < secondaryOutput.length; i++)
				if(Utils.RAND.nextFloat() < secondaryChance[i])
					list.add(secondaryOutput[i]);
		return list;
	}

	/**
	 * Adds secondary outputs to the recipe. Should the recipe have secondary outputs, these will be added /in addition/<br>
	 * The array should be alternating between Item/Block/ItemStack/ArrayList and a float for the chance
	 */
	public CrusherRecipe addToSecondaryOutput(Object... outputs)
	{
		if(outputs.length%2!=0)
			return this;
		ArrayList<ItemStack> newSecondaryOutput = new ArrayList<ItemStack>();
		ArrayList<Float> newSecondaryChance = new ArrayList<Float>();
		if(secondaryOutput!=null)
			for(int i = 0; i < secondaryOutput.length; i++)
			{
				newSecondaryOutput.add(secondaryOutput[i]);
				newSecondaryChance.add(secondaryChance[i]);
			}
		for(int i = 0; i < (outputs.length/2); i++)
			if(outputs[i*2]!=null)
			{
				Object o = ApiUtils.convertToValidRecipeInput(outputs[i*2]);
				ItemStack ss = o instanceof ItemStack?(ItemStack)o: o instanceof List?IEApi.getPreferredStackbyMod((List<ItemStack>)o): ItemStack.EMPTY;
				if(!ss.isEmpty())
				{
					newSecondaryOutput.add(ss);
					newSecondaryChance.add((Float)outputs[i*2+1]);
				}
			}
		secondaryOutput = newSecondaryOutput.toArray(new ItemStack[newSecondaryOutput.size()]);
		secondaryChance = new float[newSecondaryChance.size()];
		int i = 0;
		for(Float f : newSecondaryChance)
			secondaryChance[i++] = f;

		this.outputList = ListUtils.fromItems(this.secondaryOutput);
		if(this.outputList.isEmpty())
			this.outputList.add(this.output);
		else
			this.outputList.add(0, this.output);

		return this;
	}

	public static ArrayList<CrusherRecipe> recipeList = new ArrayList<CrusherRecipe>();

	public static CrusherRecipe addRecipe(ItemStack output, Object input, int energy)
	{
		CrusherRecipe r = new CrusherRecipe(output, input, energy);
		if(r.input!=null&&!r.output.isEmpty())
			recipeList.add(r);
		return r;
	}

	public static CrusherRecipe findRecipe(ItemStack input)
	{
		for(CrusherRecipe recipe : recipeList)
			if(recipe.input.matchesItemStack(input))
				return recipe;
		return null;
	}

	public static List<CrusherRecipe> removeRecipesForOutput(ItemStack stack)
	{
		List<CrusherRecipe> list = new ArrayList();
		Iterator<CrusherRecipe> it = recipeList.iterator();
		while(it.hasNext())
		{
			CrusherRecipe ir = it.next();
			if(OreDictionary.itemMatches(ir.output, stack, true))
			{
				list.add(ir);
				it.remove();
			}
		}
		return list;
	}

	public static List<CrusherRecipe> removeRecipesForInput(ItemStack stack)
	{
		List<CrusherRecipe> list = new ArrayList();
		Iterator<CrusherRecipe> it = recipeList.iterator();
		while(it.hasNext())
		{
			CrusherRecipe ir = it.next();
			if(ir.input.matchesItemStackIgnoringSize(stack))
			{
				list.add(ir);
				it.remove();
			}
		}
		return list;
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 4;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		nbt.setTag("input", input.writeToNBT(new NBTTagCompound()));
		return nbt;
	}

	public static CrusherRecipe loadFromNBT(NBTTagCompound nbt)
	{
		IngredientStack input = IngredientStack.readFromNBT(nbt.getCompoundTag("input"));
		for(CrusherRecipe recipe : recipeList)
			if(recipe.input.equals(input))
				return recipe;
		return null;
	}
}