/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeMapCodecs;
import malte0811.dualcodecs.DualMapCodec;
import blusunrize.immersiveengineering.common.crafting.RevolverAssemblyRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.List;

public class RevolverAssemblyRecipeSerializer implements RecipeSerializer<RevolverAssemblyRecipe>
{
	public static final DualMapCodec<RegistryFriendlyByteBuf, RevolverAssemblyRecipe> CODECS = DualCompositeMapCodecs.composite(
			new DualMapCodec<>(
					RecipeSerializer.SHAPED_RECIPE.codec(), RecipeSerializer.SHAPED_RECIPE.streamCodec()
			), RevolverAssemblyRecipe::toVanilla,
			DualCodecs.INT.listOf().optionalFieldOf("copyNBT", List.of()), RevolverAssemblyRecipe::getCopyTargets,
			RevolverAssemblyRecipe::new
	);

	@Override
	public MapCodec<RevolverAssemblyRecipe> codec()
	{
		return CODECS.mapCodec();
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, RevolverAssemblyRecipe> streamCodec()
	{
		return CODECS.streamCodec();
	}
}