/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.common.crafting.shaped.BasicShapedRecipe;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IEShapedRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>>
		implements IRecipeSerializer<BasicShapedRecipe>
{
	@Nonnull
	@Override
	public BasicShapedRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json)
	{
		return new BasicShapedRecipe(IRecipeSerializer.CRAFTING_SHAPED.read(recipeId, json));
	}

	@Nullable
	@Override
	public BasicShapedRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer)
	{
		ShapedRecipe vanilla = IRecipeSerializer.CRAFTING_SHAPED.read(recipeId, buffer);
		if(vanilla!=null)
			return new BasicShapedRecipe(vanilla);
		else
			return null;
	}

	@Override
	public void write(@Nonnull PacketBuffer buffer, @Nonnull BasicShapedRecipe recipe)
	{
		IRecipeSerializer.CRAFTING_SHAPED.write(buffer, recipe.toVanilla());
	}
}
