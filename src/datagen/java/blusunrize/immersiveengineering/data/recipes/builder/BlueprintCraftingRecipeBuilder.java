/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes.builder;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.data.recipes.builder.BaseHelpers.ItemInput;
import blusunrize.immersiveengineering.data.recipes.builder.BaseHelpers.ItemOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class BlueprintCraftingRecipeBuilder extends IERecipeBuilder<BlueprintCraftingRecipeBuilder>
		implements ItemOutput<BlueprintCraftingRecipeBuilder>, ItemInput<BlueprintCraftingRecipeBuilder>
{
	private String blueprintCategory;
	private TagOutput output;
	private final List<IngredientWithSize> inputs = new ArrayList<>();

	private BlueprintCraftingRecipeBuilder()
	{
	}

	public static BlueprintCraftingRecipeBuilder builder()
	{
		return new BlueprintCraftingRecipeBuilder();
	}

	@Override
	public BlueprintCraftingRecipeBuilder output(TagOutput output)
	{
		this.output = output;
		return this;
	}

	@Override
	public BlueprintCraftingRecipeBuilder input(IngredientWithSize input)
	{
		this.inputs.add(input);
		return this;
	}

	public BlueprintCraftingRecipeBuilder category(String blueprintCategory)
	{
		this.blueprintCategory = blueprintCategory;
		return this;
	}

	public void build(RecipeOutput out, ResourceLocation name)
	{
		BlueprintCraftingRecipe recipe = new BlueprintCraftingRecipe(blueprintCategory, output, inputs);
		out.accept(name, recipe, null, getConditions());
	}
}
