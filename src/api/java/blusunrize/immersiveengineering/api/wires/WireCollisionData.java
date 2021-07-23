/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires;

import blusunrize.immersiveengineering.api.wires.utils.WireUtils;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class WireCollisionData
{
	private final Map<BlockPos, List<CollisionInfo>> blockToWires = new Object2ObjectOpenHashMap<>();
	private final GlobalWireNetwork net;
	private final boolean isRemote;

	WireCollisionData(GlobalWireNetwork net, boolean isRemote)
	{
		this.net = net;
		this.isRemote = isRemote;
	}

	public void addConnection(Connection conn)
	{
		if(!isRemote&&!conn.isInternal())
		{
			WireLogger.logger.info("Adding block data for {}", conn);
			if(!conn.blockDataGenerated)
			{
				WireLogger.logger.info("Raytracing for addition of {}", conn);
				if((net.getLocalNet(conn.getEndA())!=net.getLocalNet(conn.getEndB()))) throw new AssertionError();
				WireUtils.raytraceAlongCatenary(conn, net.getLocalNet(conn.getEndA()), (p) ->
								add(p.getLeft(), new CollisionInfo(p.getMiddle(), p.getRight(), conn, true))
						, (p) ->
								add(p.getLeft(), new CollisionInfo(p.getMiddle(), p.getRight(), conn, false))
				);
				conn.blockDataGenerated = true;
			}
		}
	}

	public void removeConnection(Connection conn)
	{
		WireLogger.logger.info("Removing block data for {}", conn);
		if(!isRemote&&conn.blockDataGenerated)
		{
			WireLogger.logger.info("Raytracing for removal of {}", conn);
			WireUtils.raytraceAlongCatenary(conn.getCatenaryData(), conn.getEndA().getPosition(),
					p -> remove(p.getLeft(), conn), p -> remove(p.getLeft(), conn)
			);
			conn.blockDataGenerated = false;
		}
	}

	private void remove(BlockPos pos, Connection toRemove)
	{
		List<CollisionInfo> existing = blockToWires.computeIfAbsent(pos, $ -> new ArrayList<>());
		existing.removeIf(i -> i.conn==toRemove);
		if(existing.isEmpty())
			blockToWires.remove(pos);
	}

	private void add(BlockPos pos, CollisionInfo info)
	{
		List<CollisionInfo> existing = blockToWires.computeIfAbsent(pos, $ -> new ArrayList<>());
		if(!existing.contains(info))
			existing.add(info);
	}

	@Nonnull
	public Collection<CollisionInfo> getCollisionInfo(BlockPos pos)
	{
		List<CollisionInfo> ret = blockToWires.get(pos);
		if(ret==null)
			ret = ImmutableList.of();
		return ret;
	}

	public class CollisionInfo
	{
		@Nonnull
		public final Vec3 intersectA;
		@Nonnull
		public final Vec3 intersectB;
		@Nonnull
		public final Connection conn;
		public final boolean isInBlock;

		public CollisionInfo(@Nonnull Vec3 intersectA, @Nonnull Vec3 intersectB, @Nonnull Connection conn,
							 boolean isInBlock)
		{
			this.intersectA = intersectA;
			this.intersectB = intersectB;
			this.conn = conn;
			this.isInBlock = isInBlock;
		}

		public LocalWireNetwork getLocalNet()
		{
			return conn.getContainingNet(net);
		}

		@Override
		public boolean equals(Object o)
		{
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;

			CollisionInfo that = (CollisionInfo)o;

			if(isInBlock!=that.isInBlock) return false;
			if(!intersectA.equals(that.intersectA)) return false;
			if(!intersectB.equals(that.intersectB)) return false;
			return conn.equals(that.conn);
		}

		@Override
		public int hashCode()
		{
			int result = intersectA.hashCode();
			result = 31*result+intersectB.hashCode();
			result = 31*result+conn.hashCode();
			result = 31*result+(isInBlock?1: 0);
			return result;
		}
	}
}
