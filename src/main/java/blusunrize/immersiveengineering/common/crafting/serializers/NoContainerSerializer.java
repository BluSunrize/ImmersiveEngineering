/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualMapCodec;
import blusunrize.immersiveengineering.common.crafting.NoContainersRecipe;
import blusunrize.immersiveengineering.common.crafting.NoContainersShapedRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class NoContainerSerializer implements RecipeSerializer<NoContainersRecipe<?>>
{
	public static final String BASE_RECIPE = "baseRecipe";

	public static final DualMapCodec<RegistryFriendlyByteBuf, NoContainersRecipe<?>> CODECS = DualCodecs.RECIPE.fieldOf(BASE_RECIPE).map(inner -> {
		if(inner instanceof ShapedRecipe shaped)
			return new NoContainersShapedRecipe<>(shaped);
		else
			return new NoContainersRecipe<>((CraftingRecipe)inner);
	}, r -> r.baseRecipe);

	@Override
	public MapCodec<NoContainersRecipe<?>> codec()
	{
		return CODECS.mapCodec();
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, NoContainersRecipe<?>> streamCodec()
	{
		return CODECS.streamCodec();
	}
}
