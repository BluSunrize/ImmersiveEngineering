package blusunrize.immersiveengineering.common.util;

import net.minecraft.util.Direction;

import static net.minecraft.util.Direction.*;

public class DirectionUtils
{
	public static Direction rotateAround(Direction d, Direction.Axis axis)
	{
		switch(axis)
		{
			case X:
				if(d!=WEST&&d!=EAST)
				{
					return rotateX(d);
				}
				return d;
			case Y:
				if(d!=UP&&d!=DOWN)
				{
					return d.rotateY();
				}

				return d;
			case Z:
				if(d!=NORTH&&d!=SOUTH)
				{
					return rotateZ(d);
				}

				return d;
			default:
				throw new IllegalStateException("Unable to get CW facing for axis "+axis);
		}
	}

	public static Direction rotateX(Direction d)
	{
		switch(d)
		{
			case NORTH:
				return DOWN;
			case EAST:
			case WEST:
			default:
				throw new IllegalStateException("Unable to get X-rotated facing of "+d);
			case SOUTH:
				return UP;
			case UP:
				return NORTH;
			case DOWN:
				return SOUTH;
		}
	}

	public static Direction rotateZ(Direction d)
	{
		switch(d)
		{
			case EAST:
				return DOWN;
			case SOUTH:
			default:
				throw new IllegalStateException("Unable to get Z-rotated facing of "+d);
			case WEST:
				return UP;
			case UP:
				return EAST;
			case DOWN:
				return WEST;
		}
	}
}
