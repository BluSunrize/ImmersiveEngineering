/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes.builder;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.data.recipes.builder.BaseHelpers.ItemInput;
import blusunrize.immersiveengineering.data.recipes.builder.BaseHelpers.ItemOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;

public class AlloyRecipeBuilder extends IERecipeBuilder<AlloyRecipeBuilder>
		implements ItemOutput<AlloyRecipeBuilder>, ItemInput<AlloyRecipeBuilder>
{
	private IngredientWithSize input0;
	private IngredientWithSize input1;
	private TagOutput output;
	private int time = 200;

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
