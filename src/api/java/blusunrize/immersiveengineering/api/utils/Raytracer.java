package blusunrize.immersiveengineering.api.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class Raytracer
{
	public static Set<BlockPos> rayTrace(Vec3 start, Vec3 end, Level world)
	{
		return rayTrace(start, end, world, (p) -> {
		});
	}

	public static Set<BlockPos> rayTrace(Vec3 start, Vec3 end, Level world, Consumer<BlockPos> out)
	{
		Set<BlockPos> ret = new HashSet<>();
		Set<BlockPos> checked = new HashSet<>();
		for(Direction.Axis axis : Direction.Axis.values())
		{
			// x
			if(start.get(axis) > end.get(axis))
			{
				Vec3 tmp = start;
				start = end;
				end = tmp;
			}
			double min = start.get(axis);
			double dif = end.get(axis)-min;
			double lengthAdd = Math.ceil(min)-start.get(axis);
			Vec3 mov = start.subtract(end);
			if(mov.get(axis)!=0)
			{
				mov = mov.scale(1/mov.get(axis));
				ray(dif, mov, start, lengthAdd, ret, world, checked, out);
			}
		}
		if(checked.isEmpty())
		{
			BlockPos pos = new BlockPos(start);
			BlockState state = world.getBlockState(pos);
			HitResult rtr = state.getCollisionShape(world, pos).clip(start, end, pos);
			if(rtr!=null&&rtr.getType()!=Type.MISS)
				ret.add(pos);
			checked.add(pos);
			out.accept(pos);
		}
		return ret;
	}

	private static void ray(double dif, Vec3 mov, Vec3 start, double lengthAdd, Set<BlockPos> ret, Level world, Set<BlockPos> checked, Consumer<BlockPos> out)
	{
		final double standardOff = .0625;
		for(int i = 0; i < dif; i++)
		{
			Vec3 pos = start.add(mov.scale(i+lengthAdd+standardOff));
			Vec3 posNext = start.add(mov.scale(i+1+lengthAdd+standardOff));
			Vec3 posPrev = start.add(mov.scale(i+lengthAdd-standardOff));
			Vec3 posVeryPrev = start.add(mov.scale(i-1+lengthAdd-standardOff));

			BlockPos blockPos = new BlockPos(pos);
			BlockState state;
			if(!checked.contains(blockPos)&&i+lengthAdd+standardOff < dif)
			{
				state = world.getBlockState(blockPos);
				HitResult rtr = state.getCollisionShape(world, blockPos).clip(pos, posNext, blockPos);
				if(rtr!=null&&rtr.getType()!=Type.MISS)
					ret.add(blockPos);
				checked.add(blockPos);
				out.accept(blockPos);
			}
			blockPos = new BlockPos(posPrev);
			if(!checked.contains(blockPos)&&i+lengthAdd-standardOff < dif)
			{
				state = world.getBlockState(blockPos);
				HitResult rtr = state.getCollisionShape(world, blockPos).clip(posVeryPrev, posPrev, blockPos);
				if(rtr!=null&&rtr.getType()!=Type.MISS)
					ret.add(blockPos);
				checked.add(blockPos);
				out.accept(blockPos);
			}
		}
	}

}
