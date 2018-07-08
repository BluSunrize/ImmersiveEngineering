/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.util.ListUtils;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author BluSunrize - 01.03.2016
 * <p>
 * The recipe for the Fermenter
 */
public class FermenterRecipe extends MultiblockRecipe
{
	public static float energyModifier = 1;
	public static float timeModifier = 1;

	public final IngredientStack input;
	public final FluidStack fluidOutput;
	@Nonnull
	public final ItemStack itemOutput;

	public FermenterRecipe(FluidStack fluidOutput, @Nonnull ItemStack itemOutput, Object input, int energy)
	{
		this.fluidOutput = fluidOutput;
		this.itemOutput = itemOutput;
		this.input = ApiUtils.createIngredientStack(input);
		this.totalProcessEnergy = (int)Math.floor(energy*energyModifier);
		this.totalProcessTime = (int)Math.floor(80*timeModifier);

		this.inputList = Lists.newArrayList(this.input);
		this.fluidOutputList = Lists.newArrayList(this.fluidOutput);
		this.outputList = ListUtils.fromItem(this.itemOutput);
	}

	public FermenterRecipe setInputSize(int size)
	{
		this.input.inputSize = size;
		return this;
	}

	public static ArrayList<FermenterRecipe> recipeList = new ArrayList();

	public static FermenterRecipe addRecipe(FluidStack fluidOutput, @Nonnull ItemStack itemOutput, Object input, int energy)
	{
		FermenterRecipe r = new FermenterRecipe(fluidOutput, itemOutput, input, energy);
		recipeList.add(r);
		return r;
	}

	public static FermenterRecipe findRecipe(ItemStack input)
	{
		if(input.isEmpty())
			return null;
		for(FermenterRecipe recipe : recipeList)
			if(recipe.input.matches(input))
				return recipe;
		return null;
	}
//	public static List<SqueezerRecipe> removeRecipes(ItemStack output)
//	{
//		List<SqueezerRecipe> list = new ArrayList();
//		for(ComparableItemStack mold : recipeList.keySet())
//		{
//			Iterator<SqueezerRecipe> it = recipeList.get(mold).iterator();
//			while(it.hasNext())
//			{
//				SqueezerRecipe ir = it.next();
//				if(OreDictionary.itemMatches(ir.output, output, true))
//				{
//					list.add(ir);
//					it.remove();
//				}
//			}
//		}
//		return list;
//	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 0;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		nbt.setTag("input", input.writeToNBT(new NBTTagCompound()));
		return nbt;
	}

	public static FermenterRecipe loadFromNBT(NBTTagCompound nbt)
	{
		IngredientStack input = IngredientStack.readFromNBT(nbt.getCompoundTag("input"));
		for(FermenterRecipe recipe : recipeList)
			if(recipe.input.equals(input))
				return recipe;
		return null;
	}

	public static Map<String, Integer> getFluidValuesSorted(Fluid f, boolean inverse)
	{
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for(FermenterRecipe recipe : recipeList)
			if(recipe.fluidOutput!=null&&recipe.fluidOutput.getFluid()==f)
			{
				ItemStack is = recipe.input.getExampleStack();
				map.put(is.getDisplayName(), recipe.fluidOutput.amount);
			}
		return ApiUtils.sortMap(map, inverse);
	}
}