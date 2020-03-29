/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
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

	public static class RevolverResult implements IFinishedRecipe
	{
		final IFinishedRecipe base;
		final int[] nbtCopyTargetSlot;

		public RevolverResult(IFinishedRecipe base, int[] nbtCopyTargetSlot)
		{
			this.base = base;
			this.nbtCopyTargetSlot = nbtCopyTargetSlot;
		}

		@Override
		public void serialize(JsonObject json)
		{
			base.serialize(json);

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

		@Override
		public ResourceLocation getID()
		{
			return base.getID();
		}

		@Override
		public IRecipeSerializer<?> getSerializer()
		{
			return RecipeSerializers.REVOLVER_ASSEMBLY_SERIALIZER.get();
		}

		@Nullable
		@Override
		public JsonObject getAdvancementJson()
		{
			return base.getAdvancementJson();
		}

		@Nullable
		@Override
		public ResourceLocation getAdvancementID()
		{
			return base.getAdvancementID();
		}
	}
}