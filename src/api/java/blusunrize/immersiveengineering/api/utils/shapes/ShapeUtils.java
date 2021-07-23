package blusunrize.immersiveengineering.api.utils.shapes;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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
		switch(to)
		{
			case NORTH:
				return in;
			case SOUTH:
				return new Vec3(-in.x(), in.y(), -in.z());
			case EAST:
				return new Vec3(-in.z(), in.y(), in.x());
			case WEST:
				return new Vec3(in.z(), in.y(), -in.x());
			case DOWN:
			case UP:
			default:
				throw new RuntimeException("Unexpected direction: "+to);
		}
	}
}
