/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.tool.conveyor;

import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.ConveyorDirection;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;

public record BasicConveyorCacheData(
		Block cover,
		Direction facing,
		ConveyorDirection direction,
		boolean active,
		boolean leftWall,
		boolean rightWall,
		DyeColor color
)
{
}
