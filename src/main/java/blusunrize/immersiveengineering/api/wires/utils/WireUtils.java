/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.utils;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.Connection.CatenaryData;
import blusunrize.immersiveengineering.api.wires.WireCollisionData.CollisionInfo;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AtomicDouble;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
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
		Vec3d start = e.getEyePosition(0);
		Vec3d end = e.getEyePosition(1);
		return raytraceWires(world, start, end, null);
	}

	public static Connection raytraceWires(World world, Vec3d start, Vec3d end, @Nullable Connection ignored)
	{
		GlobalWireNetwork global = GlobalWireNetwork.getNetwork(world);
		WireCollisionData collisionData = global.getCollisionData();
		AtomicReference<Connection> ret = new AtomicReference<>();
		AtomicDouble minDistSq = new AtomicDouble(Double.POSITIVE_INFINITY);
		Utils.rayTrace(start, end, world, (pos) ->
		{
			Collection<CollisionInfo> infoAtPos = collisionData.getCollisionInfo(pos);
			for(CollisionInfo wireInfo : infoAtPos)
			{
				Connection c = wireInfo.conn;
				if(!c.equals(ignored))
				{
					Vec3d startRelative = start.add(-pos.getX(), -pos.getY(), -pos.getZ());
					Vec3d across = wireInfo.intersectB.subtract(wireInfo.intersectA);
					double t = Utils.getCoeffForMinDistance(startRelative, wireInfo.intersectA, across);
					t = MathHelper.clamp(t, 0, 1);
					Vec3d closest = wireInfo.intersectA.add(t*across.x, t*across.y, t*across.z);
					double distSq = closest.squareDistanceTo(startRelative);
					if(distSq < minDistSq.get())
					{
						ret.set(c);
						minDistSq.set(distSq);
					}
				}
			}
		});
		return ret.get();
	}

	public static boolean preventsConnection(World worldIn, BlockPos pos, BlockState state, Vec3d a, Vec3d b)
	{
		for(AxisAlignedBB aabb : state.getCollisionShape(worldIn, pos).toBoundingBoxList())
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
			Vec3d start = ((IImmersiveConnectable)teA).getConnectionOffset(conn, conn.getEndA());
			Vec3d end = ((IImmersiveConnectable)teB).getConnectionOffset(conn, conn.getEndB());
			ApiUtils.raytraceAlongCatenaryRelative(conn, (p) -> {
				if(!ignore.contains(p.getLeft()))
				{
					BlockState state = world.getBlockState(p.getLeft());
					if(ApiUtils.preventsConnection(world, p.getLeft(), state, p.getMiddle(), p.getRight()))
						obstructions.add(p.getLeft());
				}
			}, (p) -> {
			}, start, end.add(new Vec3d(conn.getEndB().getPosition().subtract(conn.getEndA().getPosition()))));
		}
		return obstructions;
	}

	public static WireType getWireTypeFromNBT(CompoundNBT tag, String key)
	{
		return WireType.getValue(tag.getString(key));
	}

	public static void raytraceAlongCatenary(Connection conn, LocalWireNetwork net, Consumer<Triple<BlockPos, Vec3d, Vec3d>> in,
											 Consumer<Triple<BlockPos, Vec3d, Vec3d>> close)
	{
		Vec3d vStart = getVecForIICAt(net, conn.getEndA(), conn, false);
		Vec3d vEnd = getVecForIICAt(net, conn.getEndB(), conn, true);
		raytraceAlongCatenaryRelative(conn, in, close, vStart, vEnd);
	}

	public static void raytraceAlongCatenaryRelative(Connection conn, Consumer<Triple<BlockPos, Vec3d, Vec3d>> in,
													 Consumer<Triple<BlockPos, Vec3d, Vec3d>> close, Vec3d vStart,
													 Vec3d vEnd)
	{
		conn.generateCatenaryData(vStart, vEnd);
		final BlockPos offset = conn.getEndA().getPosition();
		raytraceAlongCatenary(conn.getCatenaryData(), offset, in, close);
	}

	public static void raytraceAlongCatenary(CatenaryData data, BlockPos offset, Consumer<Triple<BlockPos, Vec3d, Vec3d>> in,
											 Consumer<Triple<BlockPos, Vec3d, Vec3d>> close)
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

	public static Vec3d[] getConnectionCatenary(Vec3d start, Vec3d end, double slack)
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
		Vec3d[] vex = new Vec3d[vertices+1];

		vex[0] = new Vec3d(start.x, start.y, start.z);
		for(int i = 1; i < vertices; i++)
		{
			float posRelative = i/(float)vertices;
			double x = 0+dx*posRelative;
			double z = 0+dz*posRelative;
			double y = a*Math.cosh((dw*posRelative-offsetX)/a)+offsetY;
			vex[i] = new Vec3d(start.x+x, start.y+y, start.z+z);
		}
		vex[vertices] = new Vec3d(end.x, end.y, end.z);

		return vex;
	}

	@Deprecated
	public static IImmersiveConnectable toIIC(Object object, World world)
	{
		return toIIC(object, world, true);
	}

	@Deprecated
	public static IImmersiveConnectable toIIC(Object object, World world, boolean allowProxies)
	{
		if(object instanceof IImmersiveConnectable)
			return (IImmersiveConnectable)object;
		else if(object instanceof BlockPos)
		{
			BlockPos pos = (BlockPos)object;
			if(world!=null&&(allowProxies||world.isBlockLoaded(pos)))
				return GlobalWireNetwork.getNetwork(world).getLocalNet(pos).getConnector(pos);
		}
		return null;
	}

	public static Vec3d getVecForIICAt(LocalWireNetwork net, ConnectionPoint pos, Connection conn, boolean fromOtherEnd)
	{
		//Force loading
		IImmersiveConnectable iicPos = net.getConnector(pos.getPosition());
		Preconditions.checkArgument(iicPos!=null&&!iicPos.isProxy());
		Vec3d offset = iicPos.getConnectionOffset(conn, pos);
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
		Vec3d look = player.getLookVec();
		Vec3d start = player.getEyePosition(1);
		Vec3d end = start.add(look.scale(maxDistance));
		return raytraceWires(world, start, end, ignored);
	}

	public static void moveConnectionEnd(Connection conn, ConnectionPoint currEnd, ConnectionPoint newEnd, World world)
	{
		ConnectionPoint fixedPos = conn.getOtherEnd(currEnd);
		GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(world);
		globalNet.removeConnection(conn);
		globalNet.addConnection(new Connection(conn.type, fixedPos, newEnd));
	}
}
