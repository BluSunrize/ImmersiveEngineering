/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes.builder;

import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.common.crafting.LazyShapelessRecipe;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.ArrayList;
import java.util.List;

public class HammerCrushingRecipeBuilder extends IERecipeBuilder<HammerCrushingRecipeBuilder>
{
	private final TagKey<Item> input;
	private final TagKey<Item> output;

	public HammerCrushingRecipeBuilder(TagKey<Item> input, TagKey<Item> output)
	{
		this.input = input;
		this.output = output;
	}

	public static HammerCrushingRecipeBuilder builder(TagKey<Item> input, TagKey<Item> output)
	{
		return new HammerCrushingRecipeBuilder(input, output);
	}

	public void build(RecipeOutput out, ResourceLocation name)
	{
		LazyShapelessRecipe recipe = new LazyShapelessRecipe(
				"misc",
				new TagOutput(output),
				NonNullList.withSize(1, Ingredient.of(input)),
				RecipeSerializers.HAMMER_CRUSHING_SERIALIZER.get()
		);
		out.accept(name, recipe, null, getConditions());
	}
}
