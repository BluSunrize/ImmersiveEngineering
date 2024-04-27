/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceFuel;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.common.register.IEItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class BlastFurnaceFuelSerializer extends IERecipeSerializer<BlastFurnaceFuel>
{
	public static final MapCodec<BlastFurnaceFuel> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			Ingredient.CODEC.fieldOf("input").forGetter(f -> f.input),
			Codec.INT.fieldOf("time").forGetter(f -> f.burnTime)
	).apply(inst, BlastFurnaceFuel::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, BlastFurnaceFuel> STREAM_CODEC = StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC, f -> f.input,
			ByteBufCodecs.INT, f -> f.burnTime,
			BlastFurnaceFuel::new
	);

	@Override
	public MapCodec<BlastFurnaceFuel> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, BlastFurnaceFuel> streamCodec()
	{
		return STREAM_CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(IEItems.Ingredients.COAL_COKE);
	}
}
