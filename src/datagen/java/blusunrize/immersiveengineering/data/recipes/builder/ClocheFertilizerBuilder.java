/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes.builder;

import blusunrize.immersiveengineering.api.crafting.ClocheFertilizer;
import blusunrize.immersiveengineering.data.recipes.builder.BaseHelpers.UnsizedItemInput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

public class ClocheFertilizerBuilder extends IERecipeBuilder<ClocheFertilizerBuilder>
		implements UnsizedItemInput<ClocheFertilizerBuilder>
{
	private Ingredient input;
	private final float modifier;

	private ClocheFertilizerBuilder(float modifier)
	{
		this.modifier = modifier;
	}

	public static ClocheFertilizerBuilder builder(float modifier)
	{
		return new ClocheFertilizerBuilder(modifier);
	}

	@Override
	public ClocheFertilizerBuilder input(Ingredient input)
	{
		this.input = input;
		return this;
	}

	public void build(RecipeOutput out, ResourceLocation name)
	{
		out.accept(name, new ClocheFertilizer(input, modifier), null, getConditions());
	}
}
