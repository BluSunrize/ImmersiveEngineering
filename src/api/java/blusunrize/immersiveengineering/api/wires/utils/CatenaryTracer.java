/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.utils;

import blusunrize.immersiveengineering.api.wires.Connection.CatenaryData;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.doubles.DoubleAVLTreeSet;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.doubles.DoubleSortedSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.api.ApiUtils.getDim;

public class CatenaryTracer
{
	private final CatenaryData catenaryData;
	private final BlockPos offset;
	//All coordinates on the catenary with at least one integer coordinate
	@VisibleForTesting
	public DoubleSortedSet integerIntersections = null;

	public CatenaryTracer(CatenaryData catenaryData, BlockPos offset)
	{
		this.catenaryData = catenaryData;
		this.offset = offset;
	}

	public static double acosh(double arg, CatenaryBranch branch)
	{
		double factor = branch==CatenaryBranch.NEGATIVE?-1: 1;
		return Math.log(arg+factor*Math.sqrt(arg*arg-1));
	}

	public void calculateIntegerIntersections()
	{
		integerIntersections = new DoubleAVLTreeSet();
		Vec3 across = catenaryData.getDelta();
		Vec3 start = catenaryData.getVecA();
		Vec3 end = start.add(catenaryData.getDelta());
		across = new Vec3(across.x, 0, across.z);
		double lengthHor = across.length();
		integerIntersections.add(0);
		integerIntersections.add(1);
		//Raytrace X&Z
		for(int dim = 0; dim <= 2; dim += 2)
		{
			int startCoord = (int)Math.ceil(Math.min(getDim(start, dim), getDim(end, dim)));
			int endCoord = (int)Math.floor(Math.max(getDim(start, dim), getDim(end, dim)));
			for(int i = startCoord; i <= endCoord; i++)
			{
				double factor = (i-getDim(start, dim))/getDim(across, dim);
				integerIntersections.add(factor);
			}
		}
		//Raytrace Y
		if(catenaryData.isVertical())
		{
			final double min = Math.min(start.y, end.y);
			final double max = Math.max(start.y, end.y);
			for(int y = (int)Math.ceil(min); y <= Math.floor(max); y++)
			{
				double factor = (y-min)/(max-min);
				integerIntersections.add(factor);
			}
		}
		else
		{
			//Lowest point (on an infinite catenary): x=0, so cosh(x/factor)=1
			double min;
			if(!CatenaryBranch.POSITIVE.exists(catenaryData)||!CatenaryBranch.NEGATIVE.exists(catenaryData))
				//Only one branch exists => the lower endpoint is the lowest point
				min = Math.min(start.y, end.y);
			else
				//Both branches exist => lowest point is the lowest point on the infinite catenary
				min = catenaryData.getScale()+catenaryData.getOffsetY()+start.y;
			//There are up to 2 points with the same Y coord in a catenary
			for(CatenaryBranch branch : CatenaryBranch.values())
				if(branch.exists(catenaryData))
				{
					double max = branch==CatenaryBranch.POSITIVE?end.y: start.y;
					//Iterate over all Y intersections
					for(int y = Mth.ceil(min); y <= Mth.floor(max); y++)
					{
						double yReal = y-start.y;
						double acosh = acosh((yReal-catenaryData.getOffsetY())/catenaryData.getScale(), branch);
						double posRel = (acosh*catenaryData.getScale()+catenaryData.getOffsetX())/lengthHor;
						//All calculated intersections should exist up to numerical errors
						Preconditions.checkState(posRel >= -1e-5&&posRel <= 1+1e-5);
						if(posRel >= 0&&posRel <= 1)
							integerIntersections.add(posRel);
					}
				}
		}
		/*
		Remove points closer than 1e-5, since they are usually points with >1 integer coordinate and were added twice
		with slightly different numerical errors
		 */
		double last = 0;
		DoubleIterator it = integerIntersections.iterator();
		while(it.hasNext())
		{
			double current = it.nextDouble();
			if(current > 0&&current-last < 1e-5)
				it.remove();
			else
				last = current;
		}
	}

	@VisibleForTesting
	public void forEachCloseCoordinate(Vec3 coord, double eps, Consumer<BlockPos> out)
	{
		for(int x = Mth.floor(coord.x-eps); x < Mth.ceil(coord.x+eps); ++x)
			for(int y = Mth.floor(coord.y-eps); y < Mth.ceil(coord.y+eps); ++y)
				for(int z = Mth.floor(coord.z-eps); z < Mth.ceil(coord.z+eps); ++z)
					out.accept(new BlockPos(x, y, z));
	}

	public void forEachSegment(Consumer<Segment> out)
	{
		final double epsilonIn = 1e-5;
		final double epsilonNear = 0.3;
		DoubleIterator it = integerIntersections.iterator();
		Vec3 last = catenaryData.getPoint(it.nextDouble());
		while(it.hasNext())
		{
			Vec3 next = catenaryData.getPoint(it.nextDouble());
			Set<BlockPos> in = new HashSet<>();
			Set<BlockPos> near = new HashSet<>();
			for(Vec3 pos : new Vec3[]{last, next})
			{
				forEachCloseCoordinate(pos, epsilonIn, in::add);
				forEachCloseCoordinate(pos, epsilonNear, near::add);
			}
			near.removeAll(in);
			processSegments(in, last, next, true, out);
			processSegments(near, last, next, false, out);
			last = next;
		}
	}

	private void processSegments(Set<BlockPos> positions, Vec3 start, Vec3 end, boolean in, Consumer<Segment> out)
	{
		for(BlockPos p : positions)
		{
			Vec3 posVec = new Vec3(p.getX(), p.getY(), p.getZ());
			Vec3 startRel = start.subtract(posVec);
			Vec3 endRel = end.subtract(posVec);
			BlockPos realPos = p.offset(offset);
			out.accept(new Segment(startRel, endRel, realPos, in));
		}
	}

	public static class Segment
	{
		public final Vec3 relativeSegmentStart;
		public final Vec3 relativeSegmentEnd;
		public final BlockPos mainPos;
		public final boolean inBlock;

		public Segment(Vec3 relativeSegmentStart, Vec3 relativeSegmentEnd, BlockPos mainPos, boolean inBlock)
		{
			this.relativeSegmentStart = relativeSegmentStart;
			this.relativeSegmentEnd = relativeSegmentEnd;
			this.mainPos = mainPos;
			this.inBlock = inBlock;
		}

		@Override
		public boolean equals(Object o)
		{
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			Segment segment = (Segment)o;
			return inBlock==segment.inBlock&&
					relativeSegmentStart.equals(segment.relativeSegmentStart)&&
					relativeSegmentEnd.equals(segment.relativeSegmentEnd)&&
					mainPos.equals(segment.mainPos);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(relativeSegmentStart, relativeSegmentEnd, mainPos, inBlock);
		}
	}

	private enum CatenaryBranch
	{
		POSITIVE,
		NEGATIVE;

		boolean exists(CatenaryData data)
		{
			if(this==NEGATIVE)
				return data.getOffsetX() >= 0;
			else
				return data.getOffsetX() <= data.getHorLength();
		}
	}
}
