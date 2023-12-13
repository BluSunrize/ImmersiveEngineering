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
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArcFurnaceRecipeBuilder extends IERecipeBuilder<ArcFurnaceRecipeBuilder>
		implements ItemInput<ArcFurnaceRecipeBuilder>, ItemOutput<ArcFurnaceRecipeBuilder>
{
	private IngredientWithSize input;
	private final List<IngredientWithSize> additives = new ArrayList<>();
	private final List<TagOutput> output = new ArrayList<>();
	private final List<StackWithChance> secondaries = new ArrayList<>();
	private TagOutput slag = TagOutput.EMPTY;
	private int energy;
	private int time;

	private ArcFurnaceRecipeBuilder()
	{
	}

	public static ArcFurnaceRecipeBuilder builder()
	{
		return new ArcFurnaceRecipeBuilder();
	}

	public ArcFurnaceRecipeBuilder input(IngredientWithSize ingredient)
	{
		this.input = ingredient;
		return this;
	}

	@Override
	public ArcFurnaceRecipeBuilder output(TagOutput output)
	{
		this.output.add(output);
		return this;
	}

	public ArcFurnaceRecipeBuilder additive(IngredientWithSize ingredient)
	{
		this.additives.add(ingredient);
		return this;
	}

	public ArcFurnaceRecipeBuilder additive(TagKey<Item> additive)
	{
		return additive(new IngredientWithSize(additive));
	}

	public ArcFurnaceRecipeBuilder slag(TagKey<Item> item, int count)
	{
		slag = new TagOutput(item, count);
		return this;
	}

	public ArcFurnaceRecipeBuilder setEnergy(int energy)
	{
		this.energy = energy;
		return this;
	}

	public ArcFurnaceRecipeBuilder setTime(int time)
	{
		this.time = time;
		return this;
	}

	public ArcFurnaceRecipeBuilder secondary(TagKey<Item> ingot, float chance, ICondition... conditions)
	{
		secondaries.add(new StackWithChance(new TagOutput(ingot), chance, conditions));
		return this;
	}

	public void build(RecipeOutput out, ResourceLocation name)
	{
		ArcFurnaceRecipe recipe = new ArcFurnaceRecipe(
				new TagOutputList(output), slag, secondaries, time, energy, input, additives
		);
		out.accept(name, recipe, null, getConditions());
	}
}
