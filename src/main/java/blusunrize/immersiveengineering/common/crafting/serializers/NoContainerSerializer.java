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
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NoContainerSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<NoContainersRecipe>
{
	public static final String BASE_RECIPE = "baseRecipe";

	@Nonnull
	@Override
	public NoContainersRecipe fromJson(@Nonnull ResourceLocation pRecipeId, @Nonnull JsonObject pSerializedRecipe)
	{
		return new NoContainersRecipe(
				(IShapedRecipe<CraftingContainer>)RecipeManager.fromJson(pRecipeId, pSerializedRecipe.getAsJsonObject(BASE_RECIPE))
		);
	}

	@Nullable
	@Override
	public NoContainersRecipe fromNetwork(@Nonnull ResourceLocation pRecipeId, @Nonnull FriendlyByteBuf pBuffer)
	{
		RecipeSerializer<?> baseSerializer = pBuffer.readRegistryIdUnsafe(ForgeRegistries.RECIPE_SERIALIZERS);
		return new NoContainersRecipe((IShapedRecipe<CraftingContainer>)baseSerializer.fromNetwork(pRecipeId, pBuffer));
	}

	@Override
	public void toNetwork(@Nonnull FriendlyByteBuf pBuffer, @Nonnull NoContainersRecipe pRecipe)
	{
		pBuffer.writeRegistryIdUnsafe(ForgeRegistries.RECIPE_SERIALIZERS, pRecipe.baseRecipe().getSerializer());
		send(pRecipe.baseRecipe(), pBuffer);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Recipe<?>>
	void send(T toSend, FriendlyByteBuf buffer)
	{
		((RecipeSerializer<T>)toSend.getSerializer()).toNetwork(buffer, toSend);
	}
}
