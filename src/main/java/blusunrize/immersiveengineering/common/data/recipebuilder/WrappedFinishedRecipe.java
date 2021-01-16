/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.data.recipebuilder;

import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WrappedFinishedRecipe implements IFinishedRecipe
{
	private final IFinishedRecipe base;
	private final IRecipeSerializer<?> serializer;

	public WrappedFinishedRecipe(
			IFinishedRecipe base, RegistryObject<? extends IRecipeSerializer<?>> serializer
	)
	{
		this.base = base;
		this.serializer = serializer.get();
	}

	@Override
	public void serialize(@Nonnull JsonObject json)
	{
		base.serialize(json);
	}

	@Nonnull
	@Override
	public ResourceLocation getID()
	{
		return base.getID();
	}

	@Nonnull
	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return serializer;
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
