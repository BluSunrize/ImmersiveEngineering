/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.multiblocks.blocks.util;

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nullable;

public enum RelativeBlockFace
{
	FRONT,
	LEFT,
	BACK,
	RIGHT,
	UP,
	DOWN;

	public static final RelativeBlockFace[] HORIZONTAL = {FRONT, LEFT, BACK, RIGHT};

	public Direction forFront(MultiblockOrientation orientation)
	{
		final Direction front = orientation.front();
		final boolean mirror = orientation.mirrored();
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

	@Contract("_, null -> null; _, !null -> !null")
	public static RelativeBlockFace from(MultiblockOrientation orientation, @Nullable Direction absoluteFace)
	{
		if(absoluteFace==null)
			return null;
		final Direction front = orientation.front();
		final boolean mirror = orientation.mirrored();
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

	public BlockPos offsetRelative(BlockPos startPos, int amount)
	{
		return switch(this)
				{
					case FRONT -> startPos.relative(Direction.NORTH, amount);
					case LEFT -> startPos.relative(Direction.EAST, amount);
					case BACK -> startPos.relative(Direction.SOUTH, amount);
					case RIGHT -> startPos.relative(Direction.WEST, amount);
					case UP -> startPos.above(amount);
					case DOWN -> startPos.below(amount);
				};
	}

	public RelativeBlockFace getOpposite()
	{
		return switch(this)
				{
					case FRONT -> BACK;
					case LEFT -> RIGHT;
					case BACK -> FRONT;
					case RIGHT -> LEFT;
					case UP -> DOWN;
					case DOWN -> UP;
				};
	}
}
