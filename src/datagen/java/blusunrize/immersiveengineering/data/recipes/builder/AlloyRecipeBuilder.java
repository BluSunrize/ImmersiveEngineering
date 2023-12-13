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
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.ArrayList;
import java.util.List;

public class AlloyRecipeBuilder extends IERecipeBuilder<AlloyRecipeBuilder>
		implements ItemOutput<AlloyRecipeBuilder>, ItemInput<AlloyRecipeBuilder>
{
	private IngredientWithSize input0;
	private IngredientWithSize input1;
	private TagOutput output;
	private int time;

	private AlloyRecipeBuilder()
	{
	}

	public static AlloyRecipeBuilder builder()
	{
		return new AlloyRecipeBuilder();
	}

	@Override
	public AlloyRecipeBuilder output(TagOutput output)
	{
		this.output = output;
		return this;
	}

	public AlloyRecipeBuilder input(IngredientWithSize input)
	{
		if(this.input0==null)
			this.input0 = input;
		else
			this.input1 = input;
		return this;
	}

	public AlloyRecipeBuilder setTime(int time)
	{
		this.time = time;
		return this;
	}

	public void build(RecipeOutput out, ResourceLocation name)
	{
		AlloyRecipe recipe = new AlloyRecipe(output, input0, input1, time);
		out.accept(name, recipe, null, getConditions());
	}
}
