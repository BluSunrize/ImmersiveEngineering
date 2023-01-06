/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;

import java.util.Arrays;
import java.util.Comparator;

import static net.minecraft.core.Direction.*;

public class DirectionUtils
{
	public static final Direction[] VALUES = Direction.values();
	public static final Direction[] BY_HORIZONTAL_INDEX = Arrays.stream(VALUES)
			.filter((direction) -> direction.getAxis().isHorizontal())
			.sorted(Comparator.comparingInt(Direction::get2DDataValue))
			.toArray(Direction[]::new);

	public static Rotation getRotationBetweenFacings(Direction orig, Direction to)
	{
		if(to==orig)
			return Rotation.NONE;
		if(orig.getAxis()==Axis.Y||to.getAxis()==Axis.Y)
			return null;
		orig = orig.getClockWise();
		if(orig==to)
			return Rotation.CLOCKWISE_90;
		orig = orig.getClockWise();
		if(orig==to)
			return Rotation.CLOCKWISE_180;
		orig = orig.getClockWise();
		if(orig==to)
			return Rotation.COUNTERCLOCKWISE_90;
		return null;//This shouldn't ever happen
	}

	public static Direction rotateAround(Direction d, Direction.Axis axis)
	{
		if (axis == d.getAxis())
			return d;
		return switch(axis)
				{
					case X -> rotateX(d);
					case Y -> d.getClockWise();
					case Z -> rotateZ(d);
				};
	}

	public static Direction rotateX(Direction d)
	{
		return switch(d)
				{
					case NORTH -> DOWN;
					case SOUTH -> UP;
					case UP -> NORTH;
					case DOWN -> SOUTH;
					case EAST, WEST -> throw new IllegalStateException("Unable to get X-rotated facing of "+d);
				};
	}

	public static Direction rotateZ(Direction d)
	{
		return switch(d)
				{
					case EAST -> DOWN;
					case WEST -> UP;
					case UP -> EAST;
					case DOWN -> WEST;
					case SOUTH, NORTH -> throw new IllegalStateException("Unable to get Z-rotated facing of "+d);
				};
	}
}
