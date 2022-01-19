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
import blusunrize.immersiveengineering.api.wires.Connection.CatenaryData;
import blusunrize.immersiveengineering.api.wires.WireCollisionData.CollisionInfo;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

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
		BlockEntity teA = world.getBlockEntity(conn.getEndA().getPosition());
		BlockEntity teB = world.getBlockEntity(conn.getEndB().getPosition());
		Set<BlockPos> obstructions = new HashSet<>();
		if(teA instanceof IImmersiveConnectable&&teB instanceof IImmersiveConnectable)
		{
			Vec3 start = ((IImmersiveConnectable)teA).getConnectionOffset(conn, conn.getEndA());
			Vec3 end = ((IImmersiveConnectable)teB).getConnectionOffset(conn, conn.getEndB());
			Vec3i offsetEndInt = conn.getEndB().getPosition().subtract(conn.getEndA().getPosition());
			Vec3 offsetEnd = new Vec3(offsetEndInt.getX(), offsetEndInt.getY(), offsetEndInt.getZ());
			WireUtils.raytraceAlongCatenaryRelative(conn, (p) -> {
				if(!ignore.contains(p.getLeft()))
				{
					BlockState state = world.getBlockState(p.getLeft());
					if(WireUtils.preventsConnection(world, p.getLeft(), state, p.getMiddle(), p.getRight()))
						obstructions.add(p.getLeft());
				}
			}, (p) -> {
			}, start, end.add(offsetEnd));
		}
		return obstructions;
	}

	public static WireType getWireTypeFromNBT(CompoundTag tag, String key)
	{
		return WireType.getValue(tag.getString(key));
	}

	public static void raytraceAlongCatenary(Connection conn, LocalWireNetwork net, Consumer<Triple<BlockPos, Vec3, Vec3>> in,
											 Consumer<Triple<BlockPos, Vec3, Vec3>> close)
	{
		Vec3 vStart = getVecForIICAt(net, conn.getEndA(), conn, false);
		Vec3 vEnd = getVecForIICAt(net, conn.getEndB(), conn, true);
		raytraceAlongCatenaryRelative(conn, in, close, vStart, vEnd);
	}

	public static void raytraceAlongCatenaryRelative(Connection conn, Consumer<Triple<BlockPos, Vec3, Vec3>> in,
													 Consumer<Triple<BlockPos, Vec3, Vec3>> close, Vec3 vStart,
													 Vec3 vEnd)
	{
		conn.generateCatenaryData(vStart, vEnd);
		final BlockPos offset = conn.getEndA().getPosition();
		raytraceAlongCatenary(conn.getCatenaryData(), offset, in, close);
	}

	public static void raytraceAlongCatenary(CatenaryData data, BlockPos offset, Consumer<Triple<BlockPos, Vec3, Vec3>> in,
											 Consumer<Triple<BlockPos, Vec3, Vec3>> close)
	{
		CatenaryTracer ct = new CatenaryTracer(data, offset);
		ct.calculateIntegerIntersections();
		ct.forEachSegment(segment -> {
			if(segment.inBlock)
				in.accept(new ImmutableTriple<>(segment.mainPos, segment.relativeSegmentStart, segment.relativeSegmentEnd));
			else
				close.accept(new ImmutableTriple<>(segment.mainPos, segment.relativeSegmentStart, segment.relativeSegmentEnd));
		});
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

	public static Vec3 getVecForIICAt(LocalWireNetwork net, ConnectionPoint pos, Connection conn, boolean fromOtherEnd)
	{
		//Force loading
		IImmersiveConnectable iicPos = net.getConnector(pos.getPosition());
		Preconditions.checkArgument(
				iicPos!=null&&!iicPos.isProxy(),
				"Expected non-proxy at %s while querying offset for connection %s, but got %s",
				pos, conn, iicPos
		);
		Vec3 offset = iicPos.getConnectionOffset(conn, pos);
		if(fromOtherEnd)
		{
			BlockPos posA = pos.getPosition();
			BlockPos posB = conn.getOtherEnd(pos).getPosition();
			offset = offset.add(posA.getX()-posB.getX(), posA.getY()-posB.getY(), posA.getZ()-posB.getZ());
		}
		return offset;
	}

	public static BlockPos toBlockPos(Object object)
	{
		if(object instanceof BlockPos)
			return (BlockPos)object;
		if(object instanceof BlockEntity)
			return ((BlockEntity)object).getBlockPos();
		if(object instanceof IImmersiveConnectable)
			return ((IImmersiveConnectable)object).getPosition();
		return null;
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
		globalNet.addConnection(new Connection(conn.type, fixedPos, newEnd));
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
		{
			LocalWireNetwork local = global.getNullableLocalNet(cp);
			if(local!=null&&!local.getConnections(cp).stream().allMatch(Connection::isInternal))
				return true;
		}
		return false;
	}
}
