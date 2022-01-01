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
	private final boolean isClient;

	WireCollisionData(GlobalWireNetwork net, boolean isClient)
	{
		this.net = net;
		this.isClient = isClient;
	}

	public void addConnection(Connection conn)
	{
		if(!isClient&&!conn.isInternal())
		{
			WireLogger.logger.info("Adding block data for {}", conn);
			if(!conn.blockDataGenerated)
			{
				WireLogger.logger.info("Raytracing for addition of {}", conn);
				if((net.getLocalNet(conn.getEndA())!=net.getLocalNet(conn.getEndB()))) throw new AssertionError();
				WireUtils.raytraceAlongCatenary(
						conn,
						p -> add(p.block(), new CollisionInfo(p.entersAt(), p.leavesAt(), conn, true)),
						p -> add(p.block(), new CollisionInfo(p.entersAt(), p.leavesAt(), conn, false))
				);
				conn.blockDataGenerated = true;
			}
		}
	}

	public void removeConnection(Connection conn)
	{
		WireLogger.logger.info("Removing block data for {}", conn);
		if(!isClient&&conn.blockDataGenerated)
		{
			WireLogger.logger.info("Raytracing for removal of {}", conn);
			WireUtils.raytraceAlongCatenary(conn, p -> remove(p.block(), conn), p -> remove(p.block(), conn));
			conn.blockDataGenerated = false;
		}
	}

	private void remove(BlockPos pos, Connection toRemove)
	{
		List<CollisionInfo> existing = blockToWires.computeIfAbsent(pos, $ -> new ArrayList<>());
		existing.removeIf(i -> i.connection==toRemove);
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

	public record CollisionInfo(
			@Nonnull Vec3 intersectA, @Nonnull Vec3 intersectB, @Nonnull Connection connection, boolean isInBlock
	)
	{
		public LocalWireNetwork getLocalNet(GlobalWireNetwork net)
		{
			return connection.getContainingNet(net);
		}
	}
}
