/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util;

import net.minecraft.util.Direction;

import java.util.Arrays;
import java.util.Comparator;

import static net.minecraft.util.Direction.*;

public class DirectionUtils
{
	public static final Direction[] VALUES = Direction.values();
	public static final Direction[] BY_HORIZONTAL_INDEX = Arrays.stream(VALUES)
			.filter((direction) -> direction.getAxis().isHorizontal())
			.sorted(Comparator.comparingInt(Direction::getHorizontalIndex))
			.toArray(Direction[]::new);

	public static Direction rotateAround(Direction d, Direction.Axis axis)
	{
		switch(axis)
		{
			case X:
				if(d!=WEST&&d!=EAST)
					return rotateX(d);
				return d;
			case Y:
				if(d!=UP&&d!=DOWN)
					return d.rotateY();

				return d;
			case Z:
				if(d!=NORTH&&d!=SOUTH)
					return rotateZ(d);

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
