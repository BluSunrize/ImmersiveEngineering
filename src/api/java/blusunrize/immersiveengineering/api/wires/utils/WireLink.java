/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.utils;

import blusunrize.immersiveengineering.api.IEApiDataComponents;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record WireLink(
		ConnectionPoint cp, ResourceKey<Level> dimension, BlockPos offset, TargetingInfo target
)
{
	public static final Codec<TargetingInfo> TARGETING_INFO_CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Direction.CODEC.fieldOf("side").forGetter(t -> t.side),
			Codec.FLOAT.fieldOf("hitX").forGetter(t -> t.hitX),
			Codec.FLOAT.fieldOf("hitY").forGetter(t -> t.hitY),
			Codec.FLOAT.fieldOf("hitZ").forGetter(t -> t.hitZ)
	).apply(inst, TargetingInfo::new));
	public static final StreamCodec<ByteBuf, TargetingInfo> TARGETING_INFO_STREAM_CODEC = StreamCodec.composite(
			Direction.STREAM_CODEC, t -> t.side,
			ByteBufCodecs.FLOAT, t -> t.hitX,
			ByteBufCodecs.FLOAT, t -> t.hitY,
			ByteBufCodecs.FLOAT, t -> t.hitZ,
			TargetingInfo::new
	);

	public static final Codec<WireLink> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			ConnectionPoint.CODEC.fieldOf("cp").forGetter(WireLink::cp),
			ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(WireLink::dimension),
			BlockPos.CODEC.fieldOf("offset").forGetter(WireLink::offset),
			TARGETING_INFO_CODEC.fieldOf("target").forGetter(WireLink::target)
	).apply(inst, WireLink::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, WireLink> STREAM_CODEC = StreamCodec.composite(
			ConnectionPoint.STREAM_CODEC, WireLink::cp,
			ResourceKey.streamCodec(Registries.DIMENSION), WireLink::dimension,
			BlockPos.STREAM_CODEC, WireLink::offset,
			TARGETING_INFO_STREAM_CODEC, WireLink::target,
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

	@Deprecated(forRemoval = true)
	public void writeToItem(ItemStack stack)
	{
		stack.set(IEApiDataComponents.WIRE_LINK, this);
	}

	@Deprecated(forRemoval = true)
	public static WireLink readFromItem(ItemStack stack)
	{
		return stack.get(IEApiDataComponents.WIRE_LINK);
	}
}
