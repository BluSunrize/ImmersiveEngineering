/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.energy.WindmillBiome;
import blusunrize.immersiveengineering.api.utils.FastEither;
import blusunrize.immersiveengineering.api.utils.IECodecs;
import blusunrize.immersiveengineering.api.utils.codec.DualMapCodec;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDevices;
import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

public class WindmillBiomeSerializer extends IERecipeSerializer<WindmillBiome>
{
	public static final MapCodec<WindmillBiome> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			Codec.FLOAT.fieldOf("modifier").forGetter(r -> r.modifier),
			TagKey.codec(Registries.BIOME).optionalFieldOf("biomeTag").forGetter(r -> r.biomes.leftOptional()),
			ResourceKey.codec(Registries.BIOME).listOf().optionalFieldOf("singleBiome").forGetter(r -> r.biomes.rightOptional())
	).apply(inst, (temperature, tag, fixedBiomes) -> {
		Preconditions.checkState(tag.isPresent()!=fixedBiomes.isPresent());
		if(tag.isPresent())
			return new WindmillBiome(tag.get(), temperature);
		else
			return new WindmillBiome(fixedBiomes.get(), temperature);
	}));
	public static final StreamCodec<RegistryFriendlyByteBuf, WindmillBiome> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.either(
					IECodecs.tagCodec(Registries.BIOME),
					ResourceKey.streamCodec(Registries.BIOME).apply(ByteBufCodecs.list())
			), r -> r.biomes.toDFU(),
			ByteBufCodecs.FLOAT, r -> r.modifier,
			(e, m) -> new WindmillBiome(FastEither.fromDFU(e), m)
	);
	public static final DualMapCodec<RegistryFriendlyByteBuf, WindmillBiome> CODECS = new DualMapCodec<>(CODEC, STREAM_CODEC);

	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, WindmillBiome> codecs()
	{
		return CODECS;
	}

	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(WoodenDevices.WINDMILL);
	}
}
