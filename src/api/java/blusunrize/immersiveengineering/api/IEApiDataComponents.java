/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import blusunrize.immersiveengineering.api.wires.utils.WireLink;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public class IEApiDataComponents
{
	static {
		if (true)
			throw new IllegalStateException("Need to be registered!");
	}

	public static DataComponentType<WireLink> WIRE_LINK;
	public static DataComponentType<String> BLUEPRINT_TYPE;
	public static DataComponentType<ResourceLocation> SHADER_TYPE;
	public static DataComponentType<Unit> FLUID_PRESSURIZED;

	public record CodecPair<T>(
			Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec, T defaultValue
	)
	{
		public static final CodecPair<Unit> UNIT = new CodecPair<>(
				Codec.unit(Unit.INSTANCE), StreamCodec.unit(Unit.INSTANCE), Unit.INSTANCE
		);

		public DataComponentType<T> makeDataComponentType()
		{
			return DataComponentType.<T>builder()
					.networkSynchronized(streamCodec())
					.persistent(codec())
					.build();
		}
	}
}
