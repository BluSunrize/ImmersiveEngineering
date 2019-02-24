/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.wires;

import blusunrize.immersiveengineering.api.ApiUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
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
	private final CatenaryData catData = new CatenaryData();

	public Connection(@Nonnull WireType type, @Nonnull ConnectionPoint endA, @Nonnull ConnectionPoint endB)
	{
		this.type = type;
		this.endA = endA;
		this.endB = endB;
		this.internal = false;
	}

	public Connection(BlockPos pos, int idA, int idB)
	{
		this.type = WireType.STEEL;//TODO
		this.endA = new ConnectionPoint(pos, idA);
		this.endB = new ConnectionPoint(pos, idB);
		this.internal = true;
	}

	public Connection(NBTTagCompound nbt)
	{
		type = WireType.getValue(nbt.getString("type"));
		endA = new ConnectionPoint(nbt.getCompoundTag("endA"));
		endB = new ConnectionPoint(nbt.getCompoundTag("endB"));
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

	public NBTTagCompound toNBT()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("endA", endA.createTag());
		nbt.setTag("endB", endB.createTag());
		nbt.setString("type", type.getUniqueName());
		nbt.setBoolean("internal", internal);
		return nbt;
	}

	public boolean isInternal()
	{
		return internal;
	}

	public void generateCatenaryData(World world)
	{
		LocalWireNetwork net = GlobalWireNetwork.getNetwork(world).getLocalNet(endA);
		Vec3d vecB = ApiUtils.getVecForIICAt(net, endB, this);
		catData.vecA = ApiUtils.getVecForIICAt(net, endA, this);
		catData.dx = vecB.x-catData.vecA.x;
		catData.dy = vecB.y-catData.vecA.y;
		catData.dz = vecB.z-catData.vecA.z;
		catData.horLength = Math.sqrt(catData.dx*catData.dx+catData.dz*catData.dz);
		if(Math.abs(catData.dx) < 0.05&&Math.abs(catData.dz) < 0.05)
		{
			catData.isVertical = true;
			catData.a = 1;
			catData.offsetX = catData.offsetY = 0;
			return;
		}
		double wireLength = Math.sqrt(catData.dx*catData.dx+catData.dy*catData.dy+catData.dz*catData.dz)*type.getSlack();
		double x = Math.sqrt(wireLength*wireLength-catData.dy*catData.dy)/catData.horLength;
		double l = 0;
		//TODO nicer numerical solver? Newton/Binary?
		int limiter = 0;
		while(limiter < 300)
		{
			limiter++;
			l += 0.01;
			if(Math.sinh(l)/l >= x)
				break;
		}
		catData.a = catData.horLength/(2*l);
		catData.offsetX = (0+catData.horLength-catData.a*Math.log((wireLength+catData.dy)/(wireLength-catData.dy)))*0.5;
		catData.offsetY = (catData.dy+0-wireLength*Math.cosh(l)/Math.sinh(l))*0.5;
	}

	public boolean hasCatenaryData()
	{
		return !Double.isNaN(catData.offsetY);
	}

	public boolean isEnd(ConnectionPoint p)
	{
		return p.equals(endA)||p.equals(endB);
	}

	//TODO proper impl, do we ever need all vertices? Or always just those on one side of the chunk border?
	public Vec3d[] getCatenaryVertices(ConnectionPoint pos)
	{
		Vec3d[] ret = new Vec3d[17];
		for(int i = 0; i <= 16; ++i)
		{
			double lambda = i/16D;
			ret[i] = getPoint(lambda, pos);
		}
		return ret;
	}

	//pos is relative to 1. 0 is the end corresponding to from, 1 is the other end.
	public Vec3d getPoint(double pos, ConnectionPoint from)
	{
		if(endB.equals(from))
		{
			pos = 1-pos;
		}
		double x = catData.dx*pos;
		double y = catData.a*Math.cosh((catData.horLength*pos-catData.offsetX)/catData.a)+catData.offsetY;
		double z = catData.dz*pos;
		if(endA.equals(from))
		{
			x += endB.getX()-endA.getX();
			y += endB.getY()-endA.getY();
			z += endB.getZ()-endA.getZ();
		}
		return new Vec3d(catData.vecA.x+x, catData.vecA.y+y, catData.vecA.z+z);
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

	private class CatenaryData
	{
		private boolean isVertical;
		//Relative to endA
		private double offsetX = Double.NaN;
		private double offsetY = Double.NaN;
		//TODO better name?
		private double a = Double.NaN;
		private double dx = Double.NaN;
		private double dy = Double.NaN;
		private double dz = Double.NaN;
		private double horLength = Double.NaN;
		private Vec3d vecA = Vec3d.ZERO;
	}
}
