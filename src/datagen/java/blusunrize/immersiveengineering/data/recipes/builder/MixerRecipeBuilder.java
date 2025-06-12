/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes.builder;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.data.recipes.builder.BaseHelpers.ItemInput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.List;

public class MixerRecipeBuilder extends IERecipeBuilder<MixerRecipeBuilder>
		implements ItemInput<MixerRecipeBuilder>
{
	private FluidStack fluidOutput;
	private SizedFluidIngredient fluidInput;
	private final List<IngredientWithSize> itemInputs = new ArrayList<>();
	private int energy;

	private MixerRecipeBuilder()
	{
	}

	public static MixerRecipeBuilder builder()
	{
		return new MixerRecipeBuilder();
	}

	@Override
	public MixerRecipeBuilder input(IngredientWithSize input)
	{
		itemInputs.add(input);
		return this;
	}

	public MixerRecipeBuilder setEnergy(int energy)
	{
		this.energy = energy;
		return this;
	}

	public MixerRecipeBuilder output(Fluid fluid, int amount)
	{
		this.fluidOutput = new FluidStack(fluid, amount);
		return this;
	}

	public MixerRecipeBuilder fluidInput(TagKey<Fluid> fluid, int amount)
	{
		this.fluidInput = SizedFluidIngredient.of(fluid, amount);
		return this;
	}

	public void build(RecipeOutput out, ResourceLocation name)
	{
		MixerRecipe recipe = new MixerRecipe(fluidOutput, fluidInput, itemInputs, energy);
		out.accept(name, recipe, null, getConditions());
	}
}
