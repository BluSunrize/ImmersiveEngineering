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

public class SawmillRecipeSerializer extends IERecipeSerializer<SawmillRecipe>
{
	public static final MapCodec<SawmillRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			TagOutput.CODEC.fieldOf("result").forGetter(r -> r.output),
			optionalItemOutput("stripped").forGetter(r -> r.stripped),
			Ingredient.CODEC.fieldOf("input").forGetter(r -> r.input),
			Codec.INT.fieldOf("energy").forGetter(MultiblockRecipe::getBaseEnergy),
			TagOutputList.CODEC.optionalFieldOf("strippingSecondaries", TagOutputList.EMPTY).forGetter(r -> r.secondaryStripping),
			TagOutputList.CODEC.optionalFieldOf("secondaryOutputs", TagOutputList.EMPTY).forGetter(r -> r.secondaryOutputs)
	).apply(inst, SawmillRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, SawmillRecipe> STREAM_CODEC = StreamCodec.composite(
			TagOutput.STREAM_CODEC, r -> r.output,
			TagOutput.STREAM_CODEC, r -> r.stripped,
			Ingredient.CONTENTS_STREAM_CODEC, r -> r.input,
			ByteBufCodecs.INT, MultiblockRecipe::getBaseEnergy,
			TagOutputList.STREAM_CODEC, r -> r.secondaryStripping,
			TagOutputList.STREAM_CODEC, r -> r.secondaryOutputs,
			SawmillRecipe::new
	);

	@Override
	public MapCodec<SawmillRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, SawmillRecipe> streamCodec()
	{
		return STREAM_CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.SAWMILL.iconStack();
	}
}
