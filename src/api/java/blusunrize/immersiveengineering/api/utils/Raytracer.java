package blusunrize.immersiveengineering.api.utils;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class Raytracer
{
	public static Set<BlockPos> rayTrace(Vector3d start, Vector3d end, World world)
	{
		return rayTrace(start, end, world, (p) -> {
		});
	}

	public static Set<BlockPos> rayTrace(Vector3d start, Vector3d end, World world, Consumer<BlockPos> out)
	{
		Set<BlockPos> ret = new HashSet<>();
		Set<BlockPos> checked = new HashSet<>();
		for(Direction.Axis axis : Direction.Axis.values())
		{
			// x
			if(start.getCoordinate(axis) > end.getCoordinate(axis))
			{
				Vector3d tmp = start;
				start = end;
				end = tmp;
			}
			double min = start.getCoordinate(axis);
			double dif = end.getCoordinate(axis)-min;
			double lengthAdd = Math.ceil(min)-start.getCoordinate(axis);
			Vector3d mov = start.subtract(end);
			if(mov.getCoordinate(axis)!=0)
			{
				mov = mov.scale(1/mov.getCoordinate(axis));
				ray(dif, mov, start, lengthAdd, ret, world, checked, out);
			}
		}
		if(checked.isEmpty())
		{
			BlockPos pos = new BlockPos(start);
			BlockState state = world.getBlockState(pos);
			RayTraceResult rtr = state.getCollisionShapeUncached(world, pos).rayTrace(start, end, pos);
			if(rtr!=null&&rtr.getType()!=Type.MISS)
				ret.add(pos);
			checked.add(pos);
			out.accept(pos);
		}
		return ret;
	}

	private static void ray(double dif, Vector3d mov, Vector3d start, double lengthAdd, Set<BlockPos> ret, World world, Set<BlockPos> checked, Consumer<BlockPos> out)
	{
		final double standardOff = .0625;
		for(int i = 0; i < dif; i++)
		{
			Vector3d pos = start.add(mov.scale(i+lengthAdd+standardOff));
			Vector3d posNext = start.add(mov.scale(i+1+lengthAdd+standardOff));
			Vector3d posPrev = start.add(mov.scale(i+lengthAdd-standardOff));
			Vector3d posVeryPrev = start.add(mov.scale(i-1+lengthAdd-standardOff));

			BlockPos blockPos = new BlockPos(pos);
			BlockState state;
			if(!checked.contains(blockPos)&&i+lengthAdd+standardOff < dif)
			{
				state = world.getBlockState(blockPos);
				RayTraceResult rtr = state.getCollisionShapeUncached(world, blockPos).rayTrace(pos, posNext, blockPos);
				if(rtr!=null&&rtr.getType()!=Type.MISS)
					ret.add(blockPos);
				checked.add(blockPos);
				out.accept(blockPos);
			}
			blockPos = new BlockPos(posPrev);
			if(!checked.contains(blockPos)&&i+lengthAdd-standardOff < dif)
			{
				state = world.getBlockState(blockPos);
				RayTraceResult rtr = state.getCollisionShapeUncached(world, blockPos).rayTrace(posVeryPrev, posPrev, blockPos);
				if(rtr!=null&&rtr.getType()!=Type.MISS)
					ret.add(blockPos);
				checked.add(blockPos);
				out.accept(blockPos);
			}
		}
	}

}
