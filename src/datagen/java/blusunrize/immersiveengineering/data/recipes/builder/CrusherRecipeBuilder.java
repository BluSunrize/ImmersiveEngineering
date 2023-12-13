/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes.builder;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.data.recipes.builder.BaseHelpers.ItemInput;
import blusunrize.immersiveengineering.data.recipes.builder.BaseHelpers.ItemOutput;
import blusunrize.immersiveengineering.data.recipes.builder.BaseHelpers.UnsizedItemInput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CrusherRecipeBuilder extends IERecipeBuilder<CrusherRecipeBuilder>
		implements UnsizedItemInput<CrusherRecipeBuilder>, ItemOutput<CrusherRecipeBuilder>
{
	private Ingredient input;
	private TagOutput output;
	private final List<StackWithChance> secondaries = new ArrayList<>();
	private int energy;

	private CrusherRecipeBuilder()
	{
	}

	public static CrusherRecipeBuilder builder()
	{
		return new CrusherRecipeBuilder();
	}

	@Override
	public CrusherRecipeBuilder input(Ingredient input)
	{
		this.input = input;
		return this;
	}

	@Override
	public CrusherRecipeBuilder output(TagOutput output)
	{
		this.output = output;
		return this;
	}

	public CrusherRecipeBuilder addSecondary(IngredientWithSize item, float chance, ICondition... conditions)
	{
		secondaries.add(new StackWithChance(new TagOutput(item), chance, Arrays.asList(conditions)));
		return this;
	}

	public CrusherRecipeBuilder addSecondary(TagKey<Item> dust, float chance, ICondition... conditions)
	{
		return addSecondary(new IngredientWithSize(dust), chance, conditions);
	}

	public CrusherRecipeBuilder addSecondary(Item item, float chance, ICondition... conditions)
	{
		return addSecondary(new IngredientWithSize(Ingredient.of(item)), chance, conditions);
	}

	public CrusherRecipeBuilder setEnergy(int energy)
	{
		this.energy = energy;
		return this;
	}

	public void build(RecipeOutput out, ResourceLocation name)
	{
		CrusherRecipe recipe = new CrusherRecipe(output, input, energy, secondaries);
		out.accept(name, recipe, null, getConditions());
	}
}
