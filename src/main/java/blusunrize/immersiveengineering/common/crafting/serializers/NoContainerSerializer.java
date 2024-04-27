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
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.crafting.IShapedRecipe;

public class NoContainerSerializer implements RecipeSerializer<NoContainersRecipe<?>>
{
	public static final String BASE_RECIPE = "baseRecipe";

	public static final MapCodec<NoContainersRecipe<?>> CODEC = Recipe.CODEC.fieldOf(BASE_RECIPE).xmap(inner -> {
		if(inner instanceof IShapedRecipe<?>)
			return new NoContainersShapedRecipe((CraftingRecipe)inner);
		else
			return new NoContainersRecipe((CraftingRecipe)inner);
	}, r -> r.baseRecipe);

	public static final StreamCodec<RegistryFriendlyByteBuf, NoContainersRecipe<?>> STREAM_CODEC = Recipe.STREAM_CODEC.map(
			inner -> {
				if(inner instanceof IShapedRecipe<?>)
					return new NoContainersShapedRecipe((CraftingRecipe)inner);
				else
					return new NoContainersRecipe((CraftingRecipe)inner);
			},
			r -> r.baseRecipe
	);

	@Override
	public MapCodec<NoContainersRecipe<?>> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, NoContainersRecipe<?>> streamCodec()
	{
		return STREAM_CODEC;
	}
}
