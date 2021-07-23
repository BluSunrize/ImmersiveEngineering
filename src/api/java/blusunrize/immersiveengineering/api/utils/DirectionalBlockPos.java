package blusunrize.immersiveengineering.api.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Objects;
import java.util.StringJoiner;

public final class DirectionalBlockPos
{
	private final BlockPos position;
	private final Direction side;

	public DirectionalBlockPos(BlockPos position, Direction side)
	{
		this.position = position;
		this.side = side;
	}

	public BlockPos getPosition()
	{
		return position;
	}

	public Direction getSide()
	{
		return side;
	}

	@Override
	public String toString()
	{
		return new StringJoiner(", ", DirectionalBlockPos.class.getSimpleName()+"[", "]")
				.add("position="+position)
				.add("side="+side)
				.toString();
	}

	@Override
	public boolean equals(Object o)
	{
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		DirectionalBlockPos that = (DirectionalBlockPos)o;
		return position.equals(that.position)&&side==that.side;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(position, side);
	}
}
