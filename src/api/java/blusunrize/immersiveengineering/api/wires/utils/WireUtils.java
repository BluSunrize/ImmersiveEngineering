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
import net.minecraft.core.SectionPos;
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
				Connection c = wireInfo.connection();
				if(!c.equals(ignored))
				{
					Vec3 startRelative = start.add(-pos.getX(), -pos.getY(), -pos.getZ());
					Vec3 across = wireInfo.intersectB().subtract(wireInfo.intersectA());
					double t = getCoeffForMinDistance(startRelative, wireInfo.intersectA(), across);
					t = Mth.clamp(t, 0, 1);
					Vec3 closest = wireInfo.intersectA().add(t*across.x, t*across.y, t*across.z);
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
		final BlockPos offset = conn.getEndA().position();
		CatenaryTracer ct = new CatenaryTracer(conn.getCatenaryData(), offset);
		ct.calculateIntegerIntersections();
		ct.forEachSegment(segment -> (segment.inBlock?in: close).accept(new BlockIntersection(
				segment.mainPos, segment.relativeSegmentStart, segment.relativeSegmentEnd
		)));
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
		{
			LocalWireNetwork local = global.getNullableLocalNet(cp);
			if(local!=null&&!local.getConnections(cp).stream().allMatch(Connection::isInternal))
				return true;
		}
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

	public static void forEachRenderPoint(Connection conn, RenderPointConsumer out)
	{
		BlockPos origin = conn.getEndA().position();
		for(int i = 0; i <= Connection.RENDER_POINTS_PER_WIRE; ++i)
		{
			Vec3 relativePos = conn.getCatenaryData().getRenderPoint(i);
			BlockPos containingBlock = origin.offset(BlockPos.containing(relativePos));
			SectionPos section = SectionPos.of(containingBlock);
			out.accept(i, relativePos, section);
		}
	}

	public record BlockIntersection(BlockPos block, Vec3 entersAt, Vec3 leavesAt)
	{
	}

	public interface RenderPointConsumer
	{
		void accept(int id, Vec3 relative, SectionPos section);
	}
}
