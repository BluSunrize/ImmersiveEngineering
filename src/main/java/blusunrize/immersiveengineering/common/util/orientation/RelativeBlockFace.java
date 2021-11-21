/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.orientation;

import com.google.common.base.Preconditions;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public enum RelativeBlockFace
{
	FRONT,
	LEFT,
	BACK,
	RIGHT,
	UP,
	DOWN;

	public Direction forFront(Direction front, boolean mirror)
	{
		Preconditions.checkArgument(front.getAxis().isHorizontal());
		return switch(this)
				{
					case FRONT -> front;
					case LEFT -> mirror?front.getCounterClockWise(): front.getClockWise();
					case BACK -> front.getOpposite();
					case RIGHT -> mirror?front.getClockWise(): front.getCounterClockWise();
					case UP -> Direction.UP;
					case DOWN -> Direction.DOWN;
				};
	}

	public static RelativeBlockFace from(Direction front, boolean mirror, Direction absoluteFace)
	{
		Preconditions.checkArgument(front.getAxis().isHorizontal());
		if(absoluteFace==Direction.UP)
			return UP;
		else if(absoluteFace==Direction.DOWN)
			return DOWN;
		int numRotations = Mth.positiveModulo(front.get2DDataValue()-absoluteFace.get2DDataValue(), 4);
		return switch(numRotations)
				{
					case 0 -> FRONT;
					case 1 -> mirror?LEFT: RIGHT;
					case 2 -> BACK;
					case 3 -> mirror?RIGHT: LEFT;
					default -> throw new IllegalStateException("Unexpected value: "+numRotations);
				};
	}
}
