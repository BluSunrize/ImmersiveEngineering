/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items.components;

import blusunrize.immersiveengineering.api.utils.codec.DualCodec;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * This is only intended as a migration aid from pre-component code to components! Do not use it in new development, and
 * move away from it where possible (ensuring save compat)
 */
public record DirectNBT(CompoundTag tag)
{
	public static final DualCodec<ByteBuf, DirectNBT> CODECS = new DualCodec<>(CompoundTag.CODEC, ByteBufCodecs.COMPOUND_TAG)
			.map(DirectNBT::new, DirectNBT::tag);

	public DirectNBT
	{
		tag = tag.copy();
	}
}
