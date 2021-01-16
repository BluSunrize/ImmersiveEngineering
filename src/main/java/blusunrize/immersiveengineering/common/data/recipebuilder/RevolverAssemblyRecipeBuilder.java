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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class RevolverAssemblyRecipeBuilder extends ShapedRecipeBuilder
{
	private int[] nbtCopyTargetSlot = null;

	public RevolverAssemblyRecipeBuilder(IItemProvider result, int count)
	{
		super(result, count);
	}

	public static RevolverAssemblyRecipeBuilder builder(IItemProvider result, int count)
	{
		return new RevolverAssemblyRecipeBuilder(result, count);
	}

	public static RevolverAssemblyRecipeBuilder builder(IItemProvider result)
	{
		return new RevolverAssemblyRecipeBuilder(result, 1);
	}

	public RevolverAssemblyRecipeBuilder setNBTCopyTargetRecipe(int... slot)
	{
		this.nbtCopyTargetSlot = slot;
		return this;
	}

	@Override
	public void build(Consumer<IFinishedRecipe> consumerIn, ResourceLocation id)
	{
		Consumer<IFinishedRecipe> dummyConsumer = iFinishedRecipe -> {
			RevolverResult result = new RevolverResult(iFinishedRecipe, nbtCopyTargetSlot);
			consumerIn.accept(result);
		};
		super.build(dummyConsumer, id);
	}

	public static class RevolverResult extends WrappedFinishedRecipe
	{
		final int[] nbtCopyTargetSlot;

		public RevolverResult(IFinishedRecipe base, int[] nbtCopyTargetSlot)
		{
			super(base, RecipeSerializers.REVOLVER_ASSEMBLY_SERIALIZER);
			this.nbtCopyTargetSlot = nbtCopyTargetSlot;
		}

		@Override
		public void serialize(@Nonnull JsonObject json)
		{
			super.serialize(json);

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