package blusunrize.immersiveengineering.api.utils.shapes;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

public class ShapeUtils
{
	public static AxisAlignedBB transformAABB(AxisAlignedBB original, Direction facing)
	{
		Matrix4 mat = new Matrix4(facing);
		Vector3d minOld = new Vector3d(original.minX, original.minY, original.minZ);
		Vector3d maxOld = new Vector3d(original.maxX, original.maxY, original.maxZ);
		Vector3d firstNew = mat.apply(minOld);
		Vector3d secondNew = mat.apply(maxOld);
		return new AxisAlignedBB(firstNew, secondNew);
	}
}
