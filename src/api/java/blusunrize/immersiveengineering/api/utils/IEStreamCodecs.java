/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class IEStreamCodecs
{
	public static final StreamCodec<FriendlyByteBuf, Float> FLOAT = StreamCodec.of(
			FriendlyByteBuf::writeFloat, FriendlyByteBuf::readFloat
	);
}
