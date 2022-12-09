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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class RevolverAssemblyRecipeBuilder extends ShapedRecipeBuilder
{
	private int[] nbtCopyTargetSlot = null;

	public RevolverAssemblyRecipeBuilder(ItemLike result, int count)
	{
		super(RecipeCategory.MISC, result, count);
	}

	public static RevolverAssemblyRecipeBuilder builder(ItemLike result, int count)
	{
		return new RevolverAssemblyRecipeBuilder(result, count);
	}

	public static RevolverAssemblyRecipeBuilder builder(ItemLike result)
	{
		return new RevolverAssemblyRecipeBuilder(result, 1);
	}

	public RevolverAssemblyRecipeBuilder setNBTCopyTargetRecipe(int... slot)
	{
		this.nbtCopyTargetSlot = slot;
		return this;
	}

	@Override
	public void save(Consumer<FinishedRecipe> consumerIn, ResourceLocation id)
	{
		Consumer<FinishedRecipe> dummyConsumer = iFinishedRecipe -> {
			RevolverResult result = new RevolverResult(iFinishedRecipe, nbtCopyTargetSlot);
			consumerIn.accept(result);
		};
		super.save(dummyConsumer, id);
	}

	public static class RevolverResult extends WrappedFinishedRecipe
	{
		final int[] nbtCopyTargetSlot;

		public RevolverResult(FinishedRecipe base, int[] nbtCopyTargetSlot)
		{
			super(base, RecipeSerializers.REVOLVER_ASSEMBLY_SERIALIZER);
			this.nbtCopyTargetSlot = nbtCopyTargetSlot;
		}

		@Override
		public void serializeRecipeData(@Nonnull JsonObject json)
		{
			super.serializeRecipeData(json);

			if(nbtCopyTargetSlot!=null)
			{
				if(nbtCopyTargetSlot.length > 1)
				{
					JsonArray jsonarray = new JsonArray();
					for(int slot : nbtCopyTargetSlot)
						jsonarray.add(slot);
					json.add("copy_nbt", jsonarray);
				}
				else
					json.addProperty("copy_nbt", nbtCopyTargetSlot[0]);
			}
		}
	}
}