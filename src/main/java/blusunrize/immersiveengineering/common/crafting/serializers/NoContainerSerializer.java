/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.common.crafting.NoContainersRecipe;
import blusunrize.immersiveengineering.common.crafting.NoContainersShapedRecipe;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.crafting.IShapedRecipe;
import net.neoforged.neoforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NoContainerSerializer implements RecipeSerializer<NoContainersRecipe<?>>
{
	public static final String BASE_RECIPE = "baseRecipe";

	@Nonnull
	@Override
	public NoContainersRecipe<?> fromJson(@Nonnull ResourceLocation pRecipeId, @Nonnull JsonObject pSerializedRecipe)
	{
		CraftingRecipe baseRecipe = (CraftingRecipe)RecipeManager.fromJson(pRecipeId, pSerializedRecipe.getAsJsonObject(BASE_RECIPE));
		if(baseRecipe instanceof IShapedRecipe<?>)
			return new NoContainersShapedRecipe(baseRecipe);
		else
			return new NoContainersRecipe(baseRecipe);
	}

	@Nullable
	@Override
	public NoContainersRecipe<?> fromNetwork(@Nonnull ResourceLocation pRecipeId, @Nonnull FriendlyByteBuf pBuffer)
	{
		RecipeSerializer<?> baseSerializer = pBuffer.readRegistryIdUnsafe(ForgeRegistries.RECIPE_SERIALIZERS);
		CraftingRecipe baseRecipe = (CraftingRecipe)baseSerializer.fromNetwork(pRecipeId, pBuffer);
		if(baseRecipe instanceof IShapedRecipe<?>)
			return new NoContainersShapedRecipe(baseRecipe);
		else
			return new NoContainersRecipe(baseRecipe);
	}

	@Override
	public void toNetwork(@Nonnull FriendlyByteBuf pBuffer, @Nonnull NoContainersRecipe pRecipe)
	{
		pBuffer.writeRegistryIdUnsafe(ForgeRegistries.RECIPE_SERIALIZERS, pRecipe.baseRecipe.getSerializer());
		send(pRecipe.baseRecipe, pBuffer);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Recipe<?>>
	void send(T toSend, FriendlyByteBuf buffer)
	{
		((RecipeSerializer<T>)toSend.getSerializer()).toNetwork(buffer, toSend);
	}
}
