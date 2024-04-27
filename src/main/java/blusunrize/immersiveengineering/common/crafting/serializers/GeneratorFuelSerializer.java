/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.energy.GeneratorFuel;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.mojang.datafixers.util.Either;
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


public class GeneratorFuelSerializer extends IERecipeSerializer<GeneratorFuel>
{
	public static final MapCodec<GeneratorFuel> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			Codec.mapEither(
					TagKey.codec(Registries.FLUID).fieldOf("fluidTag"),
					BuiltInRegistries.FLUID.byNameCodec().listOf().fieldOf("fluidList")
			).forGetter(f -> f.getFluidsRaw().map(Either::left, Either::right)),
			Codec.INT.fieldOf("burnTime").forGetter(GeneratorFuel::getBurnTime)
	).apply(inst, GeneratorFuel::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, GeneratorFuel> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.registry(Registries.FLUID).apply(ByteBufCodecs.list()), GeneratorFuel::getFluids,
			ByteBufCodecs.INT, GeneratorFuel::getBurnTime,
			GeneratorFuel::new
	);

	@Override
	public MapCodec<GeneratorFuel> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, GeneratorFuel> streamCodec()
	{
		return STREAM_CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.DIESEL_GENERATOR.iconStack();
	}
}
