/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.energy.ThermoelectricSource;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

public class ThermoelectricSourceSerializer extends IERecipeSerializer<ThermoelectricSource>
{
	public static final MapCodec<ThermoelectricSource> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			Codec.INT.fieldOf("tempKelvin").forGetter(r -> r.temperature),
			TagKey.codec(Registries.BLOCK).optionalFieldOf("blockTag").forGetter(r -> r.blocks.leftOptional()),
			maybeListOrSingle(BuiltInRegistries.BLOCK.byNameCodec(), "singleBlock").forGetter(r -> r.blocks.rightOptional())
	).apply(inst, (temperature, tag, fixedBlocks) -> {
		Preconditions.checkState(tag.isPresent()!=fixedBlocks.isPresent());
		if(tag.isPresent())
			return new ThermoelectricSource(tag.get(), temperature);
		else
			return new ThermoelectricSource(fixedBlocks.get(), temperature);
	}));
	public static final StreamCodec<RegistryFriendlyByteBuf, ThermoelectricSource> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.registry(Registries.BLOCK).apply(ByteBufCodecs.list()), ThermoelectricSource::getMatchingBlocks,
			ByteBufCodecs.INT, r -> r.temperature,
			ThermoelectricSource::new
	);

	@Override
	public MapCodec<ThermoelectricSource> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, ThermoelectricSource> streamCodec()
	{
		return STREAM_CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(MetalDevices.THERMOELECTRIC_GEN);
	}
}
