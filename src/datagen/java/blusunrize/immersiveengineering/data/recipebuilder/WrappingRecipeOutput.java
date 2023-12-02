/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipebuilder;

import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.function.Consumer;

public record WrappingRecipeOutput(RecipeOutput base, Consumer<FinishedRecipe> handleRecipe) implements RecipeOutput
{
	@Override
	public void accept(FinishedRecipe p_301033_)
	{
		handleRecipe.accept(p_301033_);
	}

	@Override
	public Builder advancement()
	{
		return base.advancement();
	}

	@Override
	public void accept(FinishedRecipe finishedRecipe, ICondition... conditions)
	{
		handleRecipe.accept(finishedRecipe);
	}
}
