/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes.builder;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.data.recipes.builder.BaseHelpers.ItemOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClocheRecipeBuilder extends IERecipeBuilder<ClocheRecipeBuilder>
		implements ItemOutput<ClocheRecipeBuilder>
{
	private final List<StackWithChance> outputs = new ArrayList<>();
	private Ingredient seed;
	private Ingredient soil;
	private int time;
	private ClocheRenderFunction renderReference;

	private ClocheRecipeBuilder()
	{
	}

	public static ClocheRecipeBuilder builder()
	{
		return new ClocheRecipeBuilder();
	}

	@Override
	public ClocheRecipeBuilder output(TagOutput output)
	{
		this.outputs.add(new StackWithChance(output, 1));
		return this;
	}

	public ClocheRecipeBuilder output(TagOutput output, float chance, ICondition... conditions)
	{
		this.outputs.add(new StackWithChance(output, chance, Arrays.asList(conditions)));
		return this;
	}

	public ClocheRecipeBuilder output(IngredientWithSize ingredient, float chance, ICondition... conditions)
	{
		return output(new TagOutput(ingredient), chance, conditions);
	}

	public ClocheRecipeBuilder output(TagKey<Item> dust, float chance, ICondition... conditions)
	{
		return output(new IngredientWithSize(dust), chance, conditions);
	}

	public ClocheRecipeBuilder output(ItemLike item, float chance, ICondition... conditions)
	{
		return output(new TagOutput(item), chance, conditions);
	}

	public ClocheRecipeBuilder seed(ItemLike seed)
	{
		this.seed = Ingredient.of(seed);
		return this;
	}

	public ClocheRecipeBuilder soil(Ingredient soil)
	{
		this.soil = soil;
		return this;
	}

	public ClocheRecipeBuilder soil(ItemLike soil)
	{
		return soil(Ingredient.of(soil));
	}

	public ClocheRecipeBuilder soil(TagKey<Item> soil)
	{
		return soil(Ingredient.of(soil));
	}

	public ClocheRecipeBuilder setTime(int time)
	{
		this.time = time;
		return this;
	}

	public ClocheRecipeBuilder setRender(ClocheRenderFunction renderReference)
	{
		this.renderReference = renderReference;
		return this;
	}

	public void build(RecipeOutput out, ResourceLocation name)
	{
		ClocheRecipe recipe = new ClocheRecipe(outputs, seed, soil, time, renderReference);
		out.accept(name, recipe, null, getConditions());
	}
}
