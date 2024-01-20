/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.Vec3;

public enum PlacementLimitation
{
	SIDE_CLICKED((side, placer, hitPos) -> side),
	PISTON_LIKE((side, placer, hitPos) -> Direction.orderedByNearest(placer)[0]),
	PISTON_INVERTED((side, placer, hitPos) -> Direction.orderedByNearest(placer)[0].getOpposite()),
	HORIZONTAL((side, placer, hitPos) -> Direction.fromYRot(placer.getYRot())),
	VERTICAL((side, placer, hitPos) -> (side!=Direction.DOWN&&(side==Direction.UP||hitPos.y <= .5))?Direction.UP: Direction.DOWN),
	HORIZONTAL_AXIS((side, placer, hitPos) -> {
		Direction f = Direction.fromYRot(placer.getYRot());
		if(f==Direction.SOUTH||f==Direction.WEST)
			return f.getOpposite();
		else
			return f;
	}
	),
	HORIZONTAL_QUADRANT((side, placer, hitPos) -> {
		if(side.getAxis()!=Axis.Y)
			return side.getOpposite();
		else
		{
			double xFromMid = hitPos.x-.5;
			double zFromMid = hitPos.z-.5;
			double max = Math.max(Math.abs(xFromMid), Math.abs(zFromMid));
			if(max==Math.abs(xFromMid))
				return xFromMid < 0?Direction.WEST: Direction.EAST;
			else
				return zFromMid < 0?Direction.NORTH: Direction.SOUTH;
		}
	}),
	HORIZONTAL_PREFER_SIDE((side, placer, hitPos) -> side.getAxis()!=Axis.Y?side.getOpposite(): placer.getDirection()),
	FIXED_DOWN((side, placer, hitPos) -> Direction.DOWN);

	private final DirectionGetter dirGetter;

	PlacementLimitation(DirectionGetter dirGetter)
	{
		this.dirGetter = dirGetter;
	}

	public Direction getDirectionForPlacement(Direction side, LivingEntity placer, Vec3 clickLocation)
	{
		return this.dirGetter.getDirectionForPlacement(side, placer, clickLocation);
	}

	public Direction getDirectionForPlacement(BlockPlaceContext context)
	{
		Vec3 clickLocation = context.getClickLocation();
		BlockPos pos = context.getClickedPos();
		clickLocation = clickLocation.subtract(pos.getX(), pos.getY(), pos.getZ());
		return getDirectionForPlacement(context.getClickedFace(), context.getPlayer(), clickLocation);
	}

	private interface DirectionGetter
	{
		Direction getDirectionForPlacement(Direction side, LivingEntity placer, Vec3 clickPos);
	}
}
