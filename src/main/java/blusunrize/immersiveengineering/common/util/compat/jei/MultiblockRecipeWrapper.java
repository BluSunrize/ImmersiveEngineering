/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class MultiblockRecipeWrapper implements IRecipeWrapper
{
	public List<ItemStack>[] recipeInputs;
	protected List<ItemStack> inputs;
	public List<ItemStack>[] recipeOutputs;
	protected List<ItemStack> outputs;
	protected List<FluidStack> fluidInputs;
	protected List<FluidStack> fluidOutputs;

	public MultiblockRecipeWrapper(MultiblockRecipe recipe)
	{
		recipe.setupJEI();
		this.inputs = recipe.getJEITotalItemInputs();
		this.recipeInputs = recipe.jeiItemInputList;
		this.outputs = recipe.getJEITotalItemOutputs();
		this.recipeOutputs = recipe.jeiItemOutputList;
		this.fluidInputs = recipe.getJEITotalFluidInputs();
		this.fluidOutputs = recipe.getJEITotalFluidOutputs();
	}

	@Override
	public void getIngredients(IIngredients ingredients)
	{
		if(!inputs.isEmpty())
			ingredients.setInputs(ItemStack.class, inputs);
		if(!outputs.isEmpty())
			ingredients.setOutputs(ItemStack.class, outputs);
		if(!fluidInputs.isEmpty())
			ingredients.setInputs(FluidStack.class, fluidInputs);
		if(!fluidOutputs.isEmpty())
			ingredients.setOutputs(FluidStack.class, fluidOutputs);
	}

	public List<ItemStack> getItemIn()
	{
		return inputs;
	}

	public List<ItemStack> getItemOut()
	{
		return outputs;
	}

	public List<FluidStack> getFluidIn()
	{
		return fluidInputs;
	}

	public List<FluidStack> getFluidOut()
	{
		return fluidOutputs;
	}
}