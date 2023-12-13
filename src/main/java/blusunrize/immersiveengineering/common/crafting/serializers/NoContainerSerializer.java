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
import blusunrize.immersiveengineering.common.network.PacketUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.crafting.IShapedRecipe;
import net.minecraft.core.registries.BuiltInRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NoContainerSerializer implements RecipeSerializer<NoContainersRecipe<?>>
{
	public static final String BASE_RECIPE = "baseRecipe";

	public static final Codec<NoContainersRecipe<?>> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Recipe.CODEC.fieldOf(BASE_RECIPE).forGetter(r -> r.baseRecipe)
	).apply(inst, inner -> {
		if(inner instanceof IShapedRecipe<?>)
			return new NoContainersShapedRecipe((CraftingRecipe)inner);
		else
			return new NoContainersRecipe((CraftingRecipe)inner);
	}));

	@Override
	public Codec<NoContainersRecipe<?>> codec()
	{
		return CODEC;
	}

	@Nullable
	@Override
	public NoContainersRecipe<?> fromNetwork(@Nonnull FriendlyByteBuf pBuffer)
	{
		RecipeSerializer<?> baseSerializer = PacketUtils.readRegistryElement(pBuffer, BuiltInRegistries.RECIPE_SERIALIZER);
		CraftingRecipe baseRecipe = (CraftingRecipe)baseSerializer.fromNetwork(pBuffer);
		if(baseRecipe instanceof IShapedRecipe<?>)
			return new NoContainersShapedRecipe(baseRecipe);
		else
			return new NoContainersRecipe(baseRecipe);
	}

	@Override
	public void toNetwork(@Nonnull FriendlyByteBuf pBuffer, @Nonnull NoContainersRecipe pRecipe)
	{
		PacketUtils.writeRegistryElement(pBuffer, BuiltInRegistries.RECIPE_SERIALIZER, pRecipe.baseRecipe.getSerializer());
		send(pRecipe.baseRecipe, pBuffer);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Recipe<?>>
	void send(T toSend, FriendlyByteBuf buffer)
	{
		((RecipeSerializer<T>)toSend.getSerializer()).toNetwork(buffer, toSend);
	}
}
