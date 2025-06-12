/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes.builder;

import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class RefineryRecipeBuilder extends IERecipeBuilder<RefineryRecipeBuilder>
{
	private FluidStack output;
	private SizedFluidIngredient input0;
	private SizedFluidIngredient input1;
	private Ingredient catalyst = Ingredient.EMPTY;
	private int energy;

	private RefineryRecipeBuilder()
	{
	}

	public static RefineryRecipeBuilder builder()
	{
		return new RefineryRecipeBuilder();
	}

	public RefineryRecipeBuilder output(Fluid output, int amount)
	{
		return output(new FluidStack(output, amount));
	}

	public RefineryRecipeBuilder setEnergy(int energy)
	{
		this.energy = energy;
		return this;
	}

	public RefineryRecipeBuilder output(FluidStack output)
	{
		this.output = output;
		return this;
	}

	public RefineryRecipeBuilder catalyst(TagKey<Item> catalyst)
	{
		this.catalyst = Ingredient.of(catalyst);
		return this;
	}

	public RefineryRecipeBuilder input(TagKey<Fluid> fluid, int amount)
	{
		return this.input(SizedFluidIngredient.of(fluid, amount));
	}

	public RefineryRecipeBuilder input(SizedFluidIngredient input)
	{
		if(input0==null)
			input0 = input;
		else
			input1 = input;
		return this;
	}

	public void build(RecipeOutput out, ResourceLocation name)
	{
		RefineryRecipe recipe = new RefineryRecipe(output, input0, input1, catalyst, energy);
		out.accept(name, recipe, null, getConditions());
	}
}
