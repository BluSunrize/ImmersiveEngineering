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
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class SqueezerRecipeBuilder extends IERecipeBuilder<SqueezerRecipeBuilder>
		implements ItemInput<SqueezerRecipeBuilder>, ItemOutput<SqueezerRecipeBuilder>
{
	private IngredientWithSize input;
	private FluidStack fluidOutput = FluidStack.EMPTY;
	private TagOutput itemOutput = TagOutput.EMPTY;
	private int energy;

	private SqueezerRecipeBuilder()
	{
	}

	public static SqueezerRecipeBuilder builder()
	{
		return new SqueezerRecipeBuilder();
	}

	@Override
	public SqueezerRecipeBuilder input(IngredientWithSize input)
	{
		this.input = input;
		return this;
	}

	@Override
	public SqueezerRecipeBuilder output(TagOutput output)
	{
		this.itemOutput = output;
		return this;
	}

	public SqueezerRecipeBuilder output(Fluid output, int amount)
	{
		return output(new FluidStack(output, amount));
	}

	public SqueezerRecipeBuilder setEnergy(int energy)
	{
		this.energy = energy;
		return this;
	}

	public SqueezerRecipeBuilder output(FluidStack output)
	{
		this.fluidOutput = output;
		return this;
	}

	public void build(RecipeOutput out, ResourceLocation name)
	{
		SqueezerRecipe recipe = new SqueezerRecipe(fluidOutput, itemOutput, input, energy);
		out.accept(name, recipe, null, getConditions());
	}
}
