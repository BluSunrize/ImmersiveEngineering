/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes.builder;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.data.recipes.builder.BaseHelpers.ItemInput;
import blusunrize.immersiveengineering.data.recipes.builder.BaseHelpers.ItemOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class CokeOvenRecipeBuilder extends IERecipeBuilder<CokeOvenRecipeBuilder>
		implements ItemOutput<CokeOvenRecipeBuilder>, ItemInput<CokeOvenRecipeBuilder>
{
	private IngredientWithSize input;
	private TagOutput output;
	private int time;
	private int creosote;

	private CokeOvenRecipeBuilder()
	{
	}

	public static CokeOvenRecipeBuilder builder()
	{
		return new CokeOvenRecipeBuilder();
	}

	@Override
	public CokeOvenRecipeBuilder output(TagOutput output)
	{
		this.output = output;
		return this;
	}

	public CokeOvenRecipeBuilder input(IngredientWithSize input)
	{
		this.input = input;
		return this;
	}

	public CokeOvenRecipeBuilder setTime(int time)
	{
		this.time = time;
		return this;
	}

	public CokeOvenRecipeBuilder creosoteAmount(int amount)
	{
		this.creosote = amount;
		return this;
	}

	public void build(RecipeOutput out, ResourceLocation name)
	{
		CokeOvenRecipe recipe = new CokeOvenRecipe(output, input, time, creosote);
		out.accept(name, recipe, null, getConditions());
	}
}
