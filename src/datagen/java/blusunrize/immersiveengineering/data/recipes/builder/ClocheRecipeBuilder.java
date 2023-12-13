/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes.builder;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import blusunrize.immersiveengineering.api.crafting.ClocheRecipe;
import blusunrize.immersiveengineering.api.crafting.ClocheRenderFunction.ClocheRenderReference;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.crafting.TagOutputList;
import blusunrize.immersiveengineering.data.recipes.builder.BaseHelpers.ItemOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

public class ClocheRecipeBuilder extends IERecipeBuilder<ClocheRecipeBuilder>
		implements ItemOutput<ClocheRecipeBuilder>
{
	private final List<TagOutput> outputs = new ArrayList<>();
	private Ingredient seed;
	private Ingredient soil;
	private int time;
	private ClocheRenderReference renderReference;

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
		this.outputs.add(output);
		return this;
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

	public ClocheRecipeBuilder setRender(ClocheRenderReference renderReference)
	{
		this.renderReference = renderReference;
		return this;
	}

	public void build(RecipeOutput out, ResourceLocation name)
	{
		ClocheRecipe recipe = new ClocheRecipe(new TagOutputList(outputs), seed, soil, time, renderReference);
		out.accept(name, recipe, null, getConditions());
	}
}
