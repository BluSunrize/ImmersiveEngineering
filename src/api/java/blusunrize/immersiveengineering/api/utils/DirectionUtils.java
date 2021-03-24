package blusunrize.immersiveengineering.api.utils;

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Rotation;

public class DirectionUtils
{
	public static final Direction[] VALUES = Direction.values();

	public static Rotation getRotationBetweenFacings(Direction orig, Direction to)
	{
		if(to==orig)
			return Rotation.NONE;
		if(orig.getAxis()==Axis.Y||to.getAxis()==Axis.Y)
			return null;
		orig = orig.rotateY();
		if(orig==to)
			return Rotation.CLOCKWISE_90;
		orig = orig.rotateY();
		if(orig==to)
			return Rotation.CLOCKWISE_180;
		orig = orig.rotateY();
		if(orig==to)
			return Rotation.COUNTERCLOCKWISE_90;
		return null;//This shouldn't ever happen
	}
}
