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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.List;

public class BottlingMachineRecipeBuilder extends IERecipeBuilder<BottlingMachineRecipeBuilder>
		implements ItemInput<BottlingMachineRecipeBuilder>, ItemOutput<BottlingMachineRecipeBuilder>
{
	private final List<IngredientWithSize> inputs = new ArrayList<>();
	private FluidTagInput fluidInput;
	private final List<TagOutput> output = new ArrayList<>();

	private BottlingMachineRecipeBuilder()
	{
	}

	public static BottlingMachineRecipeBuilder builder()
	{
		return new BottlingMachineRecipeBuilder();
	}

	@Override
	public BottlingMachineRecipeBuilder input(IngredientWithSize input)
	{
		inputs.add(input);
		return this;
	}

	public BottlingMachineRecipeBuilder fluidInput(TagKey<Fluid> input, int amount)
	{
		this.fluidInput = new FluidTagInput(input, amount);
		return this;
	}

	@Override
	public BottlingMachineRecipeBuilder output(TagOutput output)
	{
		this.output.add(output);
		return this;
	}

	public void build(RecipeOutput out, ResourceLocation name)
	{
		BottlingMachineRecipe recipe = new BottlingMachineRecipe(new TagOutputList(output), inputs, fluidInput);
		out.accept(name, recipe, null, getConditions());
	}
}
