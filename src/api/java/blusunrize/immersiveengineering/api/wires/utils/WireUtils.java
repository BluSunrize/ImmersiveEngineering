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
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
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
	public static Connection getConnectionMovedThrough(World world, LivingEntity e)
	{
		Vector3d start = e.getEyePosition(0);
		Vector3d end = e.getEyePosition(1);
		return raytraceWires(world, start, end, null);
	}

	public static Connection raytraceWires(World world, Vector3d start, Vector3d end, @Nullable Connection ignored)
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
					Vector3d startRelative = start.add(-pos.getX(), -pos.getY(), -pos.getZ());
					Vector3d across = wireInfo.intersectB.subtract(wireInfo.intersectA);
					double t = getCoeffForMinDistance(startRelative, wireInfo.intersectA, across);
					t = MathHelper.clamp(t, 0, 1);
					Vector3d closest = wireInfo.intersectA.add(t*across.x, t*across.y, t*across.z);
					double distSq = closest.squareDistanceTo(startRelative);
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

	public static boolean preventsConnection(World worldIn, BlockPos pos, BlockState state, Vector3d a, Vector3d b)
	{
		VoxelShape shape = state.getCollisionShapeUncached(worldIn, pos);
		shape = VoxelShapes.combine(shape, VoxelShapes.fullCube(), IBooleanFunction.AND);
		for(AxisAlignedBB aabb : shape.toBoundingBoxList())
		{
			aabb = aabb.grow(1e-5);
			if(aabb.contains(a)||aabb.contains(b)||aabb.rayTrace(a, b).isPresent())
				return true;
		}
		return false;
	}

	public static Set<BlockPos> findObstructingBlocks(World world, Connection conn, Set<BlockPos> ignore)
	{
		TileEntity teA = world.getTileEntity(conn.getEndA().getPosition());
		TileEntity teB = world.getTileEntity(conn.getEndB().getPosition());
		Set<BlockPos> obstructions = new HashSet<>();
		if(teA instanceof IImmersiveConnectable&&teB instanceof IImmersiveConnectable)
		{
			Vector3d start = ((IImmersiveConnectable)teA).getConnectionOffset(conn, conn.getEndA());
			Vector3d end = ((IImmersiveConnectable)teB).getConnectionOffset(conn, conn.getEndB());
			Vector3i offsetEndInt = conn.getEndB().getPosition().subtract(conn.getEndA().getPosition());
			Vector3d offsetEnd = new Vector3d(offsetEndInt.getX(), offsetEndInt.getY(), offsetEndInt.getZ());
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

	public static WireType getWireTypeFromNBT(CompoundNBT tag, String key)
	{
		return WireType.getValue(tag.getString(key));
	}

	public static void raytraceAlongCatenary(Connection conn, LocalWireNetwork net, Consumer<Triple<BlockPos, Vector3d, Vector3d>> in,
											 Consumer<Triple<BlockPos, Vector3d, Vector3d>> close)
	{
		Vector3d vStart = getVecForIICAt(net, conn.getEndA(), conn, false);
		Vector3d vEnd = getVecForIICAt(net, conn.getEndB(), conn, true);
		raytraceAlongCatenaryRelative(conn, in, close, vStart, vEnd);
	}

	public static void raytraceAlongCatenaryRelative(Connection conn, Consumer<Triple<BlockPos, Vector3d, Vector3d>> in,
													 Consumer<Triple<BlockPos, Vector3d, Vector3d>> close, Vector3d vStart,
													 Vector3d vEnd)
	{
		conn.generateCatenaryData(vStart, vEnd);
		final BlockPos offset = conn.getEndA().getPosition();
		raytraceAlongCatenary(conn.getCatenaryData(), offset, in, close);
	}

	public static void raytraceAlongCatenary(CatenaryData data, BlockPos offset, Consumer<Triple<BlockPos, Vector3d, Vector3d>> in,
											 Consumer<Triple<BlockPos, Vector3d, Vector3d>> close)
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

	public static Vector3d[] getConnectionCatenary(Vector3d start, Vector3d end, double slack)
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
		Vector3d[] vex = new Vector3d[vertices+1];

		vex[0] = new Vector3d(start.x, start.y, start.z);
		for(int i = 1; i < vertices; i++)
		{
			float posRelative = i/(float)vertices;
			double x = 0+dx*posRelative;
			double z = 0+dz*posRelative;
			double y = a*Math.cosh((dw*posRelative-offsetX)/a)+offsetY;
			vex[i] = new Vector3d(start.x+x, start.y+y, start.z+z);
		}
		vex[vertices] = new Vector3d(end.x, end.y, end.z);

		return vex;
	}

	public static Vector3d getVecForIICAt(LocalWireNetwork net, ConnectionPoint pos, Connection conn, boolean fromOtherEnd)
	{
		//Force loading
		IImmersiveConnectable iicPos = net.getConnector(pos.getPosition());
		Preconditions.checkArgument(
				iicPos!=null&&!iicPos.isProxy(),
				"Expected non-proxy at %s while querying offset for connection %s, but got %s",
				pos, conn, iicPos
		);
		Vector3d offset = iicPos.getConnectionOffset(conn, pos);
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
		if(object instanceof TileEntity)
			return ((TileEntity)object).getPos();
		if(object instanceof IImmersiveConnectable)
			return ((IImmersiveConnectable)object).getPosition();
		return null;
	}

	public static Connection getTargetConnection(World world, PlayerEntity player, Connection ignored, double maxDistance)
	{
		Vector3d look = player.getLookVec();
		Vector3d start = player.getEyePosition(1);
		Vector3d end = start.add(look.scale(maxDistance));
		return raytraceWires(world, start, end, ignored);
	}

	public static void moveConnectionEnd(Connection conn, ConnectionPoint currEnd, ConnectionPoint newEnd, World world)
	{
		ConnectionPoint fixedPos = conn.getOtherEnd(currEnd);
		GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(world);
		globalNet.removeConnection(conn);
		globalNet.addConnection(new Connection(conn.type, fixedPos, newEnd));
	}

	public static double getCoeffForMinDistance(Vector3d point, Vector3d line, Vector3d across)
	{
		if(across.x==0&&across.z==0)
			return (point.y-line.y)/across.y;
		else
		{
			Vector3d delta = point.subtract(line);
			return delta.dotProduct(across)/across.lengthSquared();
		}
	}
}
