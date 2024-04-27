/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.common.crafting.RevolverAssemblyRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.List;

public class RevolverAssemblyRecipeSerializer implements RecipeSerializer<RevolverAssemblyRecipe>
{
	public static final MapCodec<RevolverAssemblyRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			RecipeSerializer.SHAPED_RECIPE.codec().forGetter(RevolverAssemblyRecipe::toVanilla),
			Codec.INT.listOf().optionalFieldOf("copyNBT", List.of()).forGetter(RevolverAssemblyRecipe::getCopyTargets)
	).apply(inst, RevolverAssemblyRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, RevolverAssemblyRecipe> STREAM_CODEC = StreamCodec.composite(
			RecipeSerializer.SHAPED_RECIPE.streamCodec(), RevolverAssemblyRecipe::toVanilla,
			ByteBufCodecs.INT.apply(ByteBufCodecs.list()), RevolverAssemblyRecipe::getCopyTargets,
			RevolverAssemblyRecipe::new
	);

	@Override
	public MapCodec<RevolverAssemblyRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, RevolverAssemblyRecipe> streamCodec()
	{
		return STREAM_CODEC;
	}
}