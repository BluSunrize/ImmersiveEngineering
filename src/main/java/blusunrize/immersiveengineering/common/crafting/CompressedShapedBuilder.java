/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class CompressedShapedBuilder extends ShapedRecipeBuilder
{
	public CompressedShapedBuilder(IItemProvider resultIn, int countIn)
	{
		super(resultIn, countIn);
	}

	public static CompressedShapedBuilder shapedRecipe(IItemProvider resultIn)
	{
		return shapedRecipe(resultIn, 1);
	}

	public static CompressedShapedBuilder shapedRecipe(IItemProvider resultIn, int countIn)
	{
		return new CompressedShapedBuilder(resultIn, countIn);
	}

	@Override
	public void build(@Nonnull Consumer<IFinishedRecipe> consumerIn, @Nonnull ResourceLocation id)
	{
		super.build(f -> consumerIn.accept(new FinishedCompressedRecipe(f)), id);
	}

	private static class FinishedCompressedRecipe implements IFinishedRecipe
	{
		private final IFinishedRecipe baseRecipe;

		private FinishedCompressedRecipe(IFinishedRecipe baseRecipe)
		{
			this.baseRecipe = baseRecipe;
		}

		@Override
		public void serialize(@Nonnull JsonObject json)
		{
			baseRecipe.serialize(json);
		}

		@Nonnull
		@Override
		public ResourceLocation getID()
		{
			return baseRecipe.getID();
		}

		@Nonnull
		@Override
		public IRecipeSerializer<?> getSerializer()
		{
			return RecipeSerializers.COMPRESSED_SHAPED_SERIALIZER.get();
		}

		@Nullable
		@Override
		public JsonObject getAdvancementJson()
		{
			return baseRecipe.getAdvancementJson();
		}

		@Nullable
		@Override
		public ResourceLocation getAdvancementID()
		{
			return baseRecipe.getAdvancementID();
		}
	}
}
