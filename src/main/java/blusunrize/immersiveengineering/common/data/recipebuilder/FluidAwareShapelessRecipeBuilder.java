/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.data.recipebuilder;

import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class FluidAwareShapelessRecipeBuilder extends ShapelessRecipeBuilder
{
	public FluidAwareShapelessRecipeBuilder(IItemProvider resultIn, int countIn)
	{
		super(resultIn, countIn);
	}

	public static ShapelessRecipeBuilder builder(IItemProvider resultIn, int countIn)
	{
		return new FluidAwareShapelessRecipeBuilder(resultIn, countIn);
	}

	@Override
	public void build(@Nonnull Consumer<IFinishedRecipe> consumerIn, @Nonnull ResourceLocation id)
	{
		Consumer<IFinishedRecipe> dummyConsumer = iFinishedRecipe -> {
			WrappedFinishedRecipe result = new WrappedFinishedRecipe(iFinishedRecipe, RecipeSerializers.IE_SHAPELESS_SERIALIZER);
			consumerIn.accept(result);
		};
		super.build(dummyConsumer, id);
	}

}
