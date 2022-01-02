/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

import javax.annotation.Nonnull;

public record ConnectionPoint(@Nonnull BlockPos position, int index) implements Comparable<ConnectionPoint>
{
	public ConnectionPoint(CompoundTag nbt)
	{
		this(NbtUtils.readBlockPos(nbt), nbt.getInt("index"));
	}

	public CompoundTag createTag()
	{
		CompoundTag ret = NbtUtils.writeBlockPos(position);
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
