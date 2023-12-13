/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes.builder;

import blusunrize.immersiveengineering.api.crafting.SawmillRecipe;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.crafting.TagOutputList;
import blusunrize.immersiveengineering.data.recipes.builder.BaseHelpers.ItemOutput;
import blusunrize.immersiveengineering.data.recipes.builder.BaseHelpers.UnsizedItemInput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

public class SawmillRecipeBuilder extends IERecipeBuilder<SawmillRecipeBuilder>
		implements UnsizedItemInput<SawmillRecipeBuilder>, ItemOutput<SawmillRecipeBuilder>
{
	private Ingredient input;
	private TagOutput stripped = TagOutput.EMPTY;
	private final List<TagOutput> secondaryStripping = new ArrayList<>();
	private TagOutput output;
	private final List<TagOutput> secondaryOutputs = new ArrayList<>();
	private int energy;

	private SawmillRecipeBuilder()
	{
	}

	public static SawmillRecipeBuilder builder()
	{
		return new SawmillRecipeBuilder();
	}

	@Override
	public SawmillRecipeBuilder output(TagOutput output)
	{
		this.output = output;
		return this;
	}

	public SawmillRecipeBuilder input(Ingredient input)
	{
		this.input = input;
		return this;
	}

	public SawmillRecipeBuilder setEnergy(int energy)
	{
		this.energy = energy;
		return this;
	}

	public SawmillRecipeBuilder addStripped(ItemLike stripped)
	{
		this.stripped = new TagOutput(stripped);
		return this;
	}

	public SawmillRecipeBuilder addStripSecondary(ItemLike secondary, int count)
	{
		this.secondaryStripping.add(new TagOutput(secondary, count));
		return this;
	}

	public SawmillRecipeBuilder addStripSecondary(TagKey<Item> stripped)
	{
		this.secondaryStripping.add(new TagOutput(stripped));
		return this;
	}

	public SawmillRecipeBuilder addSawSecondary(TagKey<Item> stripped)
	{
		this.secondaryOutputs.add(new TagOutput(stripped));
		return this;
	}

	public void build(RecipeOutput out, ResourceLocation name)
	{
		SawmillRecipe recipe = new SawmillRecipe(
				output, stripped, input, energy,
				new TagOutputList(secondaryStripping), new TagOutputList(secondaryOutputs)
		);
		out.accept(name, recipe, null, getConditions());
	}
}
