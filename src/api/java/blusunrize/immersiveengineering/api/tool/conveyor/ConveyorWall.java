/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.tool.conveyor;

import net.minecraft.core.Direction;

public enum ConveyorWall
{
	LEFT, RIGHT;

	public Direction getWallSide(Direction conveyorDirection)
	{
		if(this==LEFT)
			return conveyorDirection.getCounterClockWise();
		else
			return conveyorDirection.getClockWise();
	}
}
