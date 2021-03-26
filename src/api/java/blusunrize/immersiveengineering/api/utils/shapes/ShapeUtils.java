package blusunrize.immersiveengineering.api.utils.shapes;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

public class ShapeUtils
{
	public static AxisAlignedBB transformAABB(AxisAlignedBB original, Direction facing)
	{
		Vector3d minOld = new Vector3d(original.minX-0.5, original.minY-0.5, original.minZ-0.5);
		Vector3d maxOld = new Vector3d(original.maxX-0.5, original.maxY-0.5, original.maxZ-0.5);
		Vector3d firstNew = rotate(minOld, facing);
		Vector3d secondNew = rotate(maxOld, facing);
		return new AxisAlignedBB(
				firstNew.x+0.5, firstNew.y+0.5, firstNew.z+0.5,
				secondNew.x+0.5, secondNew.y+0.5, secondNew.z+0.5
		);
	}

	public static Vector3d rotate(Vector3d in, Direction to)
	{
		switch(to)
		{
			case NORTH:
				return in;
			case SOUTH:
				return new Vector3d(-in.getX(), in.getY(), -in.getZ());
			case EAST:
				return new Vector3d(-in.getZ(), in.getY(), in.getX());
			case WEST:
				return new Vector3d(in.getZ(), in.getY(), -in.getX());
			case DOWN:
			case UP:
			default:
				throw new RuntimeException("Unexpected direction: "+to);
		}
	}
}
