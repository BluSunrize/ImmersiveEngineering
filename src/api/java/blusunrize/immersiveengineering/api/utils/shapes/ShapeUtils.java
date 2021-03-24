package blusunrize.immersiveengineering.api.utils.shapes;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

public class ShapeUtils
{
	public static AxisAlignedBB transformAABB(AxisAlignedBB original, Direction facing)
	{
		Vector3d minOld = new Vector3d(original.minX, original.minY, original.minZ);
		Vector3d maxOld = new Vector3d(original.maxX, original.maxY, original.maxZ);
		Vector3d firstNew = rotate(minOld, facing);
		Vector3d secondNew = rotate(maxOld, facing);
		return new AxisAlignedBB(firstNew, secondNew);
	}

	public static Vector3d rotate(Vector3d in, Direction to)
	{
		switch(to)
		{
			case NORTH:
				return in;
			case SOUTH:
				return new Vector3d(-in.getX(), in.getY(), -in.getZ());
			case WEST:
				return new Vector3d(-in.getZ(), in.getY(), in.getX());
			case EAST:
				return new Vector3d(in.getZ(), in.getY(), -in.getX());
			case DOWN:
			case UP:
			default:
				throw new RuntimeException("Unexpected direction: "+to);
		}
	}
}
