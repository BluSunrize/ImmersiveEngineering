/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes.builder;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.data.recipes.builder.BaseHelpers.ItemInput;
import blusunrize.immersiveengineering.data.recipes.builder.BaseHelpers.ItemOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class BlastFurnaceRecipeBuilder extends IERecipeBuilder<BlastFurnaceRecipeBuilder>
		implements ItemOutput<BlastFurnaceRecipeBuilder>, ItemInput<BlastFurnaceRecipeBuilder>
{
	private IngredientWithSize input;
	private TagOutput output;
	private TagOutput slag;
	private int time;

	private BlastFurnaceRecipeBuilder()
	{
	}

	public static BlastFurnaceRecipeBuilder builder()
	{
		return new BlastFurnaceRecipeBuilder();
	}

	@Override
	public BlastFurnaceRecipeBuilder output(TagOutput output)
	{
		this.output = output;
		return this;
	}

	public BlastFurnaceRecipeBuilder slag(TagKey<Item> tag, int count)
	{
		this.slag = new TagOutput(tag, count);
		return this;
	}

	public BlastFurnaceRecipeBuilder input(IngredientWithSize input)
	{
		this.input = input;
		return this;
	}

	public BlastFurnaceRecipeBuilder setTime(int time)
	{
		this.time = time;
		return this;
	}

	public void build(RecipeOutput out, ResourceLocation name)
	{
		BlastFurnaceRecipe recipe = new BlastFurnaceRecipe(output, input, time, slag);
		out.accept(name, recipe, null, getConditions());
	}
}
