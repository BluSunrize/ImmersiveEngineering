/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.data.recipebuilder;

import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class FluidAwareShapedRecipeBuilder extends ShapedRecipeBuilder
{
	public FluidAwareShapedRecipeBuilder(ItemLike result, int count)
	{
		super(RecipeCategory.MISC, result, count);
	}

	public static FluidAwareShapedRecipeBuilder builder(ItemLike result, int count)
	{
		return new FluidAwareShapedRecipeBuilder(result, count);
	}

	public static FluidAwareShapedRecipeBuilder builder(ItemLike result)
	{
		return new FluidAwareShapedRecipeBuilder(result, 1);
	}

	@Override
	public void save(@Nonnull Consumer<FinishedRecipe> consumerIn, @Nonnull ResourceLocation id)
	{
		Consumer<FinishedRecipe> dummyConsumer = iFinishedRecipe -> {
			WrappedFinishedRecipe result = new WrappedFinishedRecipe(iFinishedRecipe, RecipeSerializers.IE_SHAPED_SERIALIZER);
			consumerIn.accept(result);
		};
		super.save(dummyConsumer, id);
	}
}