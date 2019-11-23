/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires;

import blusunrize.immersiveengineering.api.ApiUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class Connection
{
	@Nonnull
	public final WireType type;
	@Nonnull
	private final ConnectionPoint endA;
	@Nonnull
	private final ConnectionPoint endB;
	private final boolean internal;
	public final CatenaryData catData = new CatenaryData();
	boolean blockDataGenerated = false;

	public Connection(@Nonnull WireType type, @Nonnull ConnectionPoint endA, @Nonnull ConnectionPoint endB)
	{
		this.type = type;
		this.endA = endA;
		this.endB = endB;
		this.internal = false;
	}

	public Connection(BlockPos pos, int idA, int idB)
	{
		this.type = WireType.INTERNAL_CONNECTION;
		this.endA = new ConnectionPoint(pos, idA);
		this.endB = new ConnectionPoint(pos, idB);
		this.internal = true;
	}

	public Connection(CompoundNBT nbt)
	{
		type = WireType.getValue(nbt.getString("type"));
		endA = new ConnectionPoint(nbt.getCompound("endA"));
		endB = new ConnectionPoint(nbt.getCompound("endB"));
		internal = nbt.getBoolean("internal");
	}

	public ConnectionPoint getOtherEnd(ConnectionPoint known)
	{
		if(known.equals(endA))
			return endB;
		else
			return endA;
	}

	@Nonnull
	public ConnectionPoint getEndA()
	{
		return endA;
	}

	@Nonnull
	public ConnectionPoint getEndB()
	{
		return endB;
	}

	public boolean isPositiveEnd(ConnectionPoint p)
	{
		return p.equals(endA);
	}

	public CompoundNBT toNBT()
	{
		CompoundNBT nbt = new CompoundNBT();
		nbt.put("endA", endA.createTag());
		nbt.put("endB", endB.createTag());
		nbt.putString("type", type.getUniqueName());
		nbt.putBoolean("internal", internal);
		return nbt;
	}

	public boolean isInternal()
	{
		return internal;
	}

	public void generateCatenaryData(World world)
	{
		LocalWireNetwork net = GlobalWireNetwork.getNetwork(world).getLocalNet(endA);
		Vec3d vecA = ApiUtils.getVecForIICAt(net, endA, this, false);
		Vec3d vecB = ApiUtils.getVecForIICAt(net, endB, this, true);
		generateCatenaryData(vecA, vecB);
	}

	public void generateCatenaryData(Vec3d vecA, Vec3d vecB)
	{
		catData.vecA = vecA;
		catData.deltaX = vecB.x-vecA.x;
		catData.deltaY = vecB.y-vecA.y;
		catData.deltaZ = vecB.z-vecA.z;
		catData.horLength = Math.sqrt(catData.deltaX*catData.deltaX+catData.deltaZ*catData.deltaZ);
		if(Math.abs(catData.deltaX) < 0.05&&Math.abs(catData.deltaZ) < 0.05)
		{
			catData.isVertical = true;
			catData.a = 1;
			catData.offsetX = catData.offsetY = 0;
			return;
		}
		double wireLength = Math.sqrt(catData.deltaX*catData.deltaX+catData.deltaY*catData.deltaY+catData.deltaZ*catData.deltaZ)*type.getSlack();
		double l;
		{
			double goal = Math.sqrt(wireLength*wireLength-catData.deltaY*catData.deltaY)/catData.horLength;
			double lower = 0;
			double upper = 1;
			while(Math.sinh(upper)/upper < goal)
			{
				lower = upper;
				upper *= 2;
			}
			final int iterations = 20;
			for(int i = 0; i < iterations; ++i)
			{
				double middleL = (lower+upper)/2;
				double middleVal = Math.sinh(middleL)/middleL;
				if(middleVal < goal)
				{
					lower = middleL;
				}
				else if(middleVal > goal)
				{
					upper = middleL;
				}
				else
				{
					upper = lower = middleL;
					break;
				}
			}
			l = (lower+upper)/2;
		}
		catData.a = catData.horLength/(2*l);
		catData.offsetX = (0+catData.horLength-catData.a*Math.log((wireLength+catData.deltaY)/(wireLength-catData.deltaY)))*0.5;
		catData.offsetY = (catData.deltaY+0-wireLength*Math.cosh(l)/Math.sinh(l))*0.5;
	}

	public boolean hasCatenaryData()
	{
		return !Double.isNaN(catData.offsetY);
	}

	public boolean isEnd(ConnectionPoint p)
	{
		return p.equals(endA)||p.equals(endB);
	}

	//pos is relative to 1. 0 is the end corresponding to from, 1 is the other end.
	public Vec3d getPoint(double pos, ConnectionPoint from)
	{
		if(endB.equals(from))
			pos = 1-pos;
		Vec3d basic = catData.getPoint(pos);
		Vec3d add = Vec3d.ZERO;
		if(endB.equals(from))
			add = new Vec3d(endA.getPosition().subtract(endB.getPosition()));
		return basic.add(add);
	}

	public double getSlope(double pos, ConnectionPoint from)
	{
		if(endB.equals(from))
			pos = 1-pos;
		double slope = catData.getSlope(pos);
		if(endB.equals(from))
			slope *= -1;
		return slope;
	}

	public ConnectionPoint getEndFor(BlockPos pos)
	{
		return endA.getPosition().equals(pos)?endA: endB;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;

		Connection that = (Connection)o;

		if(internal!=that.internal) return false;
		if(!type.equals(that.type)) return false;
		if(!endA.equals(that.endA)) return false;
		return endB.equals(that.endB);
	}

	@Override
	public int hashCode()
	{
		int result = type.hashCode();
		result = 31*result+endA.hashCode();
		result = 31*result+endB.hashCode();
		result = 31*result+(internal?1: 0);
		return result;
	}

	public boolean hasSameConnectors(Connection other)
	{
		return (endA.equals(other.endA)&&endB.equals(other.endB))
				||endA.equals(other.endB)&&endB.equals(other.endA);
	}

	public static class RenderData
	{
		public static final int POINTS_PER_WIRE = 16;
		public final CatenaryData data;
		public final WireType type;
		public final int pointsToRender;
		public final int color;

		public RenderData(Connection conn, boolean startAtB, int count)
		{
			type = conn.type;
			assert (conn.hasCatenaryData());
			pointsToRender = count;
			color = type.getColour(conn);
			data = conn.catData.copy();
			if(startAtB)
			{
				data.vecA = conn.getPoint(0, conn.getEndB());
				data.deltaX *= -1;
				data.deltaZ *= -1;
				data.offsetX = conn.catData.horLength-conn.catData.offsetX;//TODO is this correct?
				data.offsetY = -data.a*Math.cosh(-data.offsetX/data.a);
			}
		}

		@Override
		public boolean equals(Object o)
		{
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;

			RenderData that = (RenderData)o;

			if(pointsToRender!=that.pointsToRender) return false;
			if(!data.equals(that.data)) return false;
			return type.equals(that.type);
		}

		@Override
		public int hashCode()
		{
			int result = data.hashCode();
			result = 31*result+type.hashCode();
			result = 31*result+pointsToRender;
			return result;
		}

		public Vec3d getPoint(int index)
		{
			return data.getPoint(index/(double)POINTS_PER_WIRE);
		}
	}

	public static class CatenaryData
	{
		private boolean isVertical;
		//Relative to endA
		private double offsetX = Double.NaN;
		private double offsetY = Double.NaN;
		//TODO better name?
		private double a = Double.NaN;
		private double deltaX = Double.NaN;
		private double deltaY = Double.NaN;
		private double deltaZ = Double.NaN;
		private double horLength = Double.NaN;
		private Vec3d vecA = Vec3d.ZERO;

		public CatenaryData copy()
		{
			CatenaryData ret = new CatenaryData();
			ret.isVertical = isVertical;
			ret.offsetX = offsetX;
			ret.offsetY = offsetY;
			ret.a = a;
			ret.deltaX = deltaX;
			ret.deltaY = deltaY;
			ret.deltaZ = deltaZ;
			ret.horLength = horLength;
			ret.vecA = vecA;
			return ret;
		}

		public double getSlope(double pos)
		{
			pos = MathHelper.clamp(pos, 0, 1);
			if(isVertical)
				return Double.POSITIVE_INFINITY;
			else
				return Math.sinh((pos*horLength-offsetX)/a);
		}

		public Vec3d getPoint(double pos)
		{
			if(pos==1)
				return vecA.add(deltaX, deltaY, deltaZ);
			double x = deltaX*pos;
			double y;
			if(isVertical)
				y = deltaY*pos;
			else
				y = a*Math.cosh((horLength*pos-offsetX)/a)+offsetY;
			double z = deltaZ*pos;
			return vecA.add(x, y, z);
		}

		public boolean isVertical()
		{
			return isVertical;
		}

		public double getOffsetX()
		{
			return offsetX;
		}

		public double getOffsetY()
		{
			return offsetY;
		}

		public double getA()
		{
			return a;
		}

		public double getDeltaX()
		{
			return deltaX;
		}

		public double getDeltaY()
		{
			return deltaY;
		}

		public double getDeltaZ()
		{
			return deltaZ;
		}

		public double getHorLength()
		{
			return horLength;
		}

		public Vec3d getVecA()
		{
			return vecA;
		}

		@Override
		public String toString()
		{
			return "Vertical: "+isVertical+", offset: x "+offsetX+" y "+offsetY+", Factor A: "+a+", Vector at end A: "+
					vecA+", horizontal length: "+horLength+", delta: "+deltaX+", "+deltaY+", "+deltaZ;
		}

		@Override
		public boolean equals(Object o)
		{
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;

			CatenaryData that = (CatenaryData)o;

			if(isVertical!=that.isVertical) return false;
			if(Double.compare(that.offsetX, offsetX)!=0) return false;
			if(Double.compare(that.offsetY, offsetY)!=0) return false;
			if(Double.compare(that.a, a)!=0) return false;
			if(Double.compare(that.deltaX, deltaX)!=0) return false;
			if(Double.compare(that.deltaY, deltaY)!=0) return false;
			if(Double.compare(that.deltaZ, deltaZ)!=0) return false;
			if(Double.compare(that.horLength, horLength)!=0) return false;
			return vecA.equals(that.vecA);
		}

		@Override
		public int hashCode()
		{
			int result;
			long temp;
			result = (isVertical?1: 0);
			temp = Double.doubleToLongBits(offsetX);
			result = 31*result+(int)(temp^(temp >>> 32));
			temp = Double.doubleToLongBits(offsetY);
			result = 31*result+(int)(temp^(temp >>> 32));
			temp = Double.doubleToLongBits(a);
			result = 31*result+(int)(temp^(temp >>> 32));
			temp = Double.doubleToLongBits(deltaX);
			result = 31*result+(int)(temp^(temp >>> 32));
			temp = Double.doubleToLongBits(deltaY);
			result = 31*result+(int)(temp^(temp >>> 32));
			temp = Double.doubleToLongBits(deltaZ);
			result = 31*result+(int)(temp^(temp >>> 32));
			temp = Double.doubleToLongBits(horLength);
			result = 31*result+(int)(temp^(temp >>> 32));
			result = 31*result+vecA.hashCode();
			return result;
		}

		public Vec3d getDelta()
		{
			return new Vec3d(deltaX, deltaY, deltaZ);
		}
	}
}
