/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes.builder;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.data.recipes.builder.BaseHelpers.ItemInput;
import blusunrize.immersiveengineering.data.recipes.builder.BaseHelpers.ItemOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public class FermenterRecipeBuilder extends IERecipeBuilder<FermenterRecipeBuilder>
		implements ItemInput<FermenterRecipeBuilder>, ItemOutput<FermenterRecipeBuilder>
{
	private IngredientWithSize input;
	private FluidStack fluidOutput;
	private TagOutput itemOutput = TagOutput.EMPTY;
	private int energy;

	private FermenterRecipeBuilder()
	{
	}

	public static FermenterRecipeBuilder builder()
	{
		return new FermenterRecipeBuilder();
	}

	@Override
	public FermenterRecipeBuilder input(IngredientWithSize input)
	{
		this.input = input;
		return this;
	}

	@Override
	public FermenterRecipeBuilder output(TagOutput output)
	{
		this.itemOutput = output;
		return this;
	}

	public FermenterRecipeBuilder output(Fluid output, int amount)
	{
		return output(new FluidStack(output, amount));
	}

	public FermenterRecipeBuilder setEnergy(int energy)
	{
		this.energy = energy;
		return this;
	}

	public FermenterRecipeBuilder output(FluidStack output)
	{
		this.fluidOutput = output;
		return this;
	}

	public void build(RecipeOutput out, ResourceLocation name)
	{
		FermenterRecipe recipe = new FermenterRecipe(fluidOutput, itemOutput, input, energy);
		out.accept(name, recipe, null, getConditions());
	}
}
