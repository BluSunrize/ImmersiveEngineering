/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public class CrusherRecipeSerializer extends IERecipeSerializer<CrusherRecipe>
{
	public static final MapCodec<CrusherRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			TagOutput.CODEC.fieldOf("result").forGetter(r -> r.output),
			Ingredient.CODEC.fieldOf("input").forGetter(r -> r.input),
			Codec.INT.fieldOf("energy").forGetter(MultiblockRecipe::getBaseEnergy),
			CHANCE_LIST_CODEC.optionalFieldOf("secondaries", List.of()).forGetter(r -> r.secondaryOutputs)
	).apply(inst, CrusherRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, CrusherRecipe> STREAM_CODEC = StreamCodec.composite(
			TagOutput.STREAM_CODEC, r -> r.output,
			Ingredient.CONTENTS_STREAM_CODEC, r -> r.input,
			ByteBufCodecs.INT, MultiblockRecipe::getBaseEnergy,
			StackWithChance.STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.secondaryOutputs,
			CrusherRecipe::new
	);

	@Override
	public MapCodec<CrusherRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, CrusherRecipe> streamCodec()
	{
		return STREAM_CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.CRUSHER.iconStack();
	}
}
