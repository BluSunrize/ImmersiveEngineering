/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils.shapes;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class ShapeUtils
{
	public static AABB transformAABB(AABB original, Direction facing)
	{
		Vec3 minOld = new Vec3(original.minX-0.5, original.minY-0.5, original.minZ-0.5);
		Vec3 maxOld = new Vec3(original.maxX-0.5, original.maxY-0.5, original.maxZ-0.5);
		Vec3 firstNew = rotate(minOld, facing);
		Vec3 secondNew = rotate(maxOld, facing);
		return new AABB(
				firstNew.x+0.5, firstNew.y+0.5, firstNew.z+0.5,
				secondNew.x+0.5, secondNew.y+0.5, secondNew.z+0.5
		);
	}

	public static Vec3 rotate(Vec3 in, Direction to)
	{
		return switch(to)
				{
					case NORTH -> in;
					case SOUTH -> new Vec3(-in.x(), in.y(), -in.z());
					case EAST -> new Vec3(-in.z(), in.y(), in.x());
					case WEST -> new Vec3(in.z(), in.y(), -in.x());
					case DOWN, UP -> throw new RuntimeException("Unexpected direction: "+to);
				};
	}

	public static VoxelShape join(List<AABB> boxes)
	{
		VoxelShape ret = Shapes.empty();
		for(AABB aabb : boxes)
			ret = Shapes.joinUnoptimized(ret, Shapes.create(aabb), BooleanOp.OR);
		return ret.optimize();
	}
}
