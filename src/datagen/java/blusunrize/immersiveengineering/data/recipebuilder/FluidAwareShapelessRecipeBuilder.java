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
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class FluidAwareShapelessRecipeBuilder extends ShapelessRecipeBuilder
{
	public FluidAwareShapelessRecipeBuilder(ItemLike resultIn, int countIn)
	{
		super(RecipeCategory.MISC, resultIn, countIn);
	}

	public static ShapelessRecipeBuilder builder(ItemLike resultIn, int countIn)
	{
		return new FluidAwareShapelessRecipeBuilder(resultIn, countIn);
	}

	@Override
	public void save(@Nonnull Consumer<FinishedRecipe> consumerIn, @Nonnull ResourceLocation id)
	{
		Consumer<FinishedRecipe> dummyConsumer = iFinishedRecipe -> {
			WrappedFinishedRecipe result = new WrappedFinishedRecipe(iFinishedRecipe, RecipeSerializers.IE_SHAPELESS_SERIALIZER);
			consumerIn.accept(result);
		};
		super.save(dummyConsumer, id);
	}

}
