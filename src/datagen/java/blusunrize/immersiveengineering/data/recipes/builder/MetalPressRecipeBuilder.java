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
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MetalPressRecipeBuilder extends IERecipeBuilder<MetalPressRecipeBuilder>
		implements ItemInput<MetalPressRecipeBuilder>, ItemOutput<MetalPressRecipeBuilder>
{
	private IngredientWithSize input;
	private TagOutput output;
	private int energy;
	private ItemLike mold;

	private MetalPressRecipeBuilder()
	{
	}

	public static MetalPressRecipeBuilder builder()
	{
		return new MetalPressRecipeBuilder();
	}

	@Override
	public MetalPressRecipeBuilder input(IngredientWithSize input)
	{
		this.input = input;
		return this;
	}

	@Override
	public MetalPressRecipeBuilder output(TagOutput output)
	{
		this.output = output;
		return this;
	}

	public MetalPressRecipeBuilder mold(ItemLike mold)
	{
		this.mold = mold;
		return this;
	}

	public MetalPressRecipeBuilder setEnergy(int energy)
	{
		this.energy = energy;
		return this;
	}

	public void build(RecipeOutput out, ResourceLocation name)
	{
		MetalPressRecipe recipe = new MetalPressRecipe(output, input, mold.asItem(), energy);
		out.accept(name, recipe, null, getConditions());
	}
}
