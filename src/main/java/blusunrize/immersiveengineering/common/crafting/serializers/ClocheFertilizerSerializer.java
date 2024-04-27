/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.ClocheFertilizer;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public class ClocheFertilizerSerializer extends IERecipeSerializer<ClocheFertilizer>
{
	public static final MapCodec<ClocheFertilizer> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
					Ingredient.CODEC.fieldOf("input").forGetter(r -> r.input),
					Codec.FLOAT.fieldOf("growthModifier").forGetter(r -> r.growthModifier)
			).apply(inst, ClocheFertilizer::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, ClocheFertilizer> STREAM_CODEC = StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC, r -> r.input,
			ByteBufCodecs.FLOAT, r -> r.growthModifier,
			ClocheFertilizer::new
	);

	@Override
	public MapCodec<ClocheFertilizer> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, ClocheFertilizer> streamCodec()
	{
		return STREAM_CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Items.BONE_MEAL);
	}
}
