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

public final class ConnectionPoint implements Comparable<ConnectionPoint>
{
	@Nonnull
	private final BlockPos pos;
	private final int index;

	public ConnectionPoint(@Nonnull BlockPos pos, int index)
	{
		this.pos = pos;
		this.index = index;
	}

	public ConnectionPoint(CompoundTag nbt)
	{
		pos = NbtUtils.readBlockPos(nbt);
		index = nbt.getInt("index");
	}

	public CompoundTag createTag()
	{
		CompoundTag ret = NbtUtils.writeBlockPos(pos);
		ret.putInt("index", index);
		return ret;
	}

	@Nonnull
	public BlockPos getPosition()
	{
		return pos;
	}

	public int getIndex()
	{
		return index;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;

		ConnectionPoint that = (ConnectionPoint)o;

		if(index!=that.index) return false;
		return pos.equals(that.pos);
	}

	@Override
	public int hashCode()
	{
		int result = pos.hashCode();
		result = 31*result+index;
		return result;
	}

	@Override
	public int compareTo(ConnectionPoint o)
	{
		int blockCmp = pos.compareTo(o.pos);
		if(blockCmp!=0)
			return blockCmp;
		return Integer.compare(index, o.index);
	}

	@Override
	public String toString()
	{
		return "[x="+pos.getX()+", y="+pos.getY()+", z="+pos.getZ()+", index="+index+"]";
	}

	public int getX()
	{
		return pos.getX();
	}

	public int getY()
	{
		return pos.getY();
	}

	public int getZ()
	{
		return pos.getZ();
	}
}
