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
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class FluidAwareShapedRecipeBuilder extends ShapedRecipeBuilder
{
	public FluidAwareShapedRecipeBuilder(IItemProvider result, int count)
	{
		super(result, count);
	}

	public static FluidAwareShapedRecipeBuilder builder(IItemProvider result, int count)
	{
		return new FluidAwareShapedRecipeBuilder(result, count);
	}

	public static FluidAwareShapedRecipeBuilder builder(IItemProvider result)
	{
		return new FluidAwareShapedRecipeBuilder(result, 1);
	}

	@Override
	public void build(@Nonnull Consumer<IFinishedRecipe> consumerIn, @Nonnull ResourceLocation id)
	{
		Consumer<IFinishedRecipe> dummyConsumer = iFinishedRecipe -> {
			WrappedFinishedRecipe result = new WrappedFinishedRecipe(iFinishedRecipe, RecipeSerializers.IE_SHAPED_SERIALIZER);
			consumerIn.accept(result);
		};
		super.build(dummyConsumer, id);
	}
}