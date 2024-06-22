/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nonnull;

public record ConnectionPoint(@Nonnull BlockPos position, int index) implements Comparable<ConnectionPoint>
{
	public static final Codec<ConnectionPoint> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			BlockPos.CODEC.fieldOf("position").forGetter(ConnectionPoint::position),
			Codec.INT.fieldOf("index").forGetter(ConnectionPoint::index)
	).apply(inst, ConnectionPoint::new));
	public static final StreamCodec<ByteBuf, ConnectionPoint> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, ConnectionPoint::position,
			ByteBufCodecs.INT, ConnectionPoint::index,
			ConnectionPoint::new
	);

	public ConnectionPoint(CompoundTag nbt)
	{
		this(NbtUtils.readBlockPos(nbt, "position").orElseThrow(), nbt.getInt("index"));
	}

	public CompoundTag createTag()
	{
		CompoundTag ret = new CompoundTag();
		ret.put("position", NbtUtils.writeBlockPos(position));
		ret.putInt("index", index);
		return ret;
	}

	@Override
	public int compareTo(ConnectionPoint o)
	{
		int blockCmp = position.compareTo(o.position);
		if(blockCmp!=0)
			return blockCmp;
		return Integer.compare(index, o.index);
	}

	public int getX()
	{
		return position.getX();
	}

	public int getY()
	{
		return position.getY();
	}

	public int getZ()
	{
		return position.getZ();
	}
}
