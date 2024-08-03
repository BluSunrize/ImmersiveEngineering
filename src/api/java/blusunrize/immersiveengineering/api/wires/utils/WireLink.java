/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.utils;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.utils.codec.DualCodec;
import blusunrize.immersiveengineering.api.utils.codec.DualCodecs;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record WireLink(
		ConnectionPoint cp, ResourceKey<Level> dimension, BlockPos offset, TargetingInfo target
)
{
	public static final DualCodec<ByteBuf, TargetingInfo> TARGETING_INFO_CODECS = DualCodecs.composite(
			DualCodecs.DIRECTION.fieldOf("side"), t -> t.side,
			DualCodecs.FLOAT.fieldOf("hitX"), t -> t.hitX,
			DualCodecs.FLOAT.fieldOf("hitY"), t -> t.hitY,
			DualCodecs.FLOAT.fieldOf("hitZ"), t -> t.hitZ,
			TargetingInfo::new
	);

	public static final DualCodec<RegistryFriendlyByteBuf, WireLink> CODECS = DualCodecs.composite(
			ConnectionPoint.CODECS.fieldOf("cp"), WireLink::cp,
			DualCodecs.resourceKey(Registries.DIMENSION).fieldOf("dimension"), WireLink::dimension,
			DualCodecs.BLOCK_POS.fieldOf("offset"), WireLink::offset,
			TARGETING_INFO_CODECS.fieldOf("target"), WireLink::target,
			WireLink::new
	);

	public WireLink
	{
		offset = offset.immutable();
	}

	public static WireLink create(ConnectionPoint cp, Level world, BlockPos offset, TargetingInfo info)
	{
		return new WireLink(cp, world.dimension(), offset, info);
	}
}
