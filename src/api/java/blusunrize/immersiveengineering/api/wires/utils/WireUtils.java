/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.utils;

import blusunrize.immersiveengineering.api.utils.Raytracer;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.WireCollisionData.CollisionInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableDouble;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class WireUtils
{
	public static Connection getConnectionMovedThrough(Level world, LivingEntity e)
	{
		Vec3 start = e.getEyePosition(0);
		Vec3 end = e.getEyePosition(1);
		return raytraceWires(world, start, end, null);
	}

	public static Connection raytraceWires(Level world, Vec3 start, Vec3 end, @Nullable Connection ignored)
	{
		GlobalWireNetwork global = GlobalWireNetwork.getNetwork(world);
		WireCollisionData collisionData = global.getCollisionData();
		AtomicReference<Connection> ret = new AtomicReference<>();
		MutableDouble minDistSq = new MutableDouble(Double.POSITIVE_INFINITY);
		Raytracer.rayTrace(start, end, world, (pos) ->
		{
			Collection<CollisionInfo> infoAtPos = collisionData.getCollisionInfo(pos);
			for(CollisionInfo wireInfo : infoAtPos)
			{
				Connection c = wireInfo.conn;
				if(!c.equals(ignored))
				{
					Vec3 startRelative = start.add(-pos.getX(), -pos.getY(), -pos.getZ());
					Vec3 across = wireInfo.intersectB.subtract(wireInfo.intersectA);
					double t = getCoeffForMinDistance(startRelative, wireInfo.intersectA, across);
					t = Mth.clamp(t, 0, 1);
					Vec3 closest = wireInfo.intersectA.add(t*across.x, t*across.y, t*across.z);
					double distSq = closest.distanceToSqr(startRelative);
					if(distSq < minDistSq.doubleValue())
					{
						ret.set(c);
						minDistSq.setValue(distSq);
					}
				}
			}
		});
		return ret.get();
	}

	public static boolean preventsConnection(Level worldIn, BlockPos pos, BlockState state, Vec3 a, Vec3 b)
	{
		VoxelShape shape = state.getCollisionShape(worldIn, pos);
		shape = Shapes.joinUnoptimized(shape, Shapes.block(), BooleanOp.AND);
		for(AABB aabb : shape.toAabbs())
		{
			aabb = aabb.inflate(1e-5);
			if(aabb.contains(a)||aabb.contains(b)||aabb.clip(a, b).isPresent())
				return true;
		}
		return false;
	}

	public static Set<BlockPos> findObstructingBlocks(Level world, Connection conn, Set<BlockPos> ignore)
	{
		Set<BlockPos> obstructions = new HashSet<>();
		WireUtils.raytraceAlongCatenary(conn, (p) -> {
			if(!ignore.contains(p.block()))
			{
				BlockState state = world.getBlockState(p.block());
				if(WireUtils.preventsConnection(world, p.block(), state, p.entersAt(), p.leavesAt()))
					obstructions.add(p.block());
			}
		}, (p) -> {
		});
		return obstructions;
	}

	public static WireType getWireTypeFromNBT(CompoundTag tag, String key)
	{
		return WireType.getValue(tag.getString(key));
	}

	public static void raytraceAlongCatenary(Connection conn, Consumer<BlockIntersection> in,
											 Consumer<BlockIntersection> close)
	{
		final BlockPos offset = conn.getEndA().getPosition();
		conn.generateCatenaryData();
		CatenaryTracer ct = new CatenaryTracer(conn.getCatenaryData(), offset);
		ct.calculateIntegerIntersections();
		ct.forEachSegment(segment -> (segment.inBlock?in: close).accept(new BlockIntersection(
				segment.mainPos, segment.relativeSegmentStart, segment.relativeSegmentEnd
		)));
	}

	public static Vec3[] getConnectionCatenary(Vec3 start, Vec3 end, double slack)
	{
		final int vertices = 17;
		double dx = (end.x)-(start.x);
		double dy = (end.y)-(start.y);
		double dz = (end.z)-(start.z);
		double dw = Math.sqrt(dx*dx+dz*dz);
		double k = Math.sqrt(dx*dx+dy*dy+dz*dz)*slack;
		double l = 0;
		int limiter = 0;
		while(limiter < 300)
		{
			limiter++;
			l += 0.01;
			if(Math.sinh(l)/l >= Math.sqrt(k*k-dy*dy)/dw)
				break;
		}
		double a = dw/2/l;
		double offsetX = (0+dw-a*Math.log((k+dy)/(k-dy)))*0.5;
		double offsetY = (dy+0-k*Math.cosh(l)/Math.sinh(l))*0.5;
		Vec3[] vex = new Vec3[vertices+1];

		vex[0] = new Vec3(start.x, start.y, start.z);
		for(int i = 1; i < vertices; i++)
		{
			float posRelative = i/(float)vertices;
			double x = 0+dx*posRelative;
			double z = 0+dz*posRelative;
			double y = a*Math.cosh((dw*posRelative-offsetX)/a)+offsetY;
			vex[i] = new Vec3(start.x+x, start.y+y, start.z+z);
		}
		vex[vertices] = new Vec3(end.x, end.y, end.z);

		return vex;
	}

	public static Connection getTargetConnection(Level world, Player player, Connection ignored, double maxDistance)
	{
		Vec3 look = player.getLookAngle();
		Vec3 start = player.getEyePosition(1);
		Vec3 end = start.add(look.scale(maxDistance));
		return raytraceWires(world, start, end, ignored);
	}

	public static void moveConnectionEnd(Connection conn, ConnectionPoint currEnd, ConnectionPoint newEnd, Level world)
	{
		ConnectionPoint fixedPos = conn.getOtherEnd(currEnd);
		GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(world);
		globalNet.removeConnection(conn);
		globalNet.addConnection(new Connection(conn.type, fixedPos, newEnd, globalNet));
	}

	public static double getCoeffForMinDistance(Vec3 point, Vec3 line, Vec3 across)
	{
		if(across.x==0&&across.z==0)
			return (point.y-line.y)/across.y;
		else
		{
			Vec3 delta = point.subtract(line);
			return delta.dot(across)/across.lengthSqr();
		}
	}

	public static boolean hasAnyConnections(GlobalWireNetwork global, IImmersiveConnectable iic)
	{
		for(ConnectionPoint cp : iic.getConnectionPoints())
			if(!global.getLocalNet(cp).getConnections(cp).stream().allMatch(Connection::isInternal))
				return true;
		return false;
	}

	public static Vec3 loadVec3(Tag loadFrom)
	{
		if(!(loadFrom instanceof ListTag list))
			return Vec3.ZERO;
		return new Vec3(list.getDouble(0), list.getDouble(1), list.getDouble(2));
	}

	public static Tag storeVec3(Vec3 vec)
	{
		ListTag list = new ListTag();
		list.add(DoubleTag.valueOf(vec.x));
		list.add(DoubleTag.valueOf(vec.y));
		list.add(DoubleTag.valueOf(vec.z));
		return list;
	}

	public static Vec3 getConnectionOffset(
			GlobalWireNetwork globalNet, ConnectionPoint here, ConnectionPoint other, WireType type
	)
	{
		return globalNet.getLocalNet(here).getConnector(here).getConnectionOffset(here, other, type);
	}

	public static record BlockIntersection(BlockPos block, Vec3 entersAt, Vec3 leavesAt)
	{
	}
}
