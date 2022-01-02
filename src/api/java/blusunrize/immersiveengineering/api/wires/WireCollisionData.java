/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires;

import blusunrize.immersiveengineering.api.wires.utils.WireUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class WireCollisionData
{
	// Only populated on server
	private final Map<BlockPos, List<CollisionInfo>> blockToWires = new Object2ObjectOpenHashMap<>();
	// Only populated on client
	private final Map<SectionPos, List<ConnectionSegments>> sectionsToWires = new Object2ObjectOpenHashMap<>();
	private final GlobalWireNetwork net;
	private final boolean isClient;

	WireCollisionData(GlobalWireNetwork net, boolean isClient)
	{
		this.net = net;
		this.isClient = isClient;
	}

	public void addConnection(Connection conn)
	{
		if(conn.isInternal()||conn.blockDataGenerated)
			return;
		WireLogger.logger.info("Adding block data for {}", conn);
		conn.generateCatenaryData();
		if(isClient)
			forEachSection(conn, (sectionPos, segments) -> {
				synchronized(sectionsToWires)
				{
					sectionsToWires.computeIfAbsent(sectionPos, $ -> new ArrayList<>()).add(segments);
				}
			});
		else
		{
			Preconditions.checkState(net.getLocalNet(conn.getEndA())==net.getLocalNet(conn.getEndB()));
			WireUtils.raytraceAlongCatenary(
					conn,
					p -> add(p.block(), new CollisionInfo(p.entersAt(), p.leavesAt(), conn, true)),
					p -> add(p.block(), new CollisionInfo(p.entersAt(), p.leavesAt(), conn, false))
			);
		}
		conn.blockDataGenerated = true;
	}

	public void removeConnection(Connection conn)
	{
		if(!conn.blockDataGenerated)
			return;
		WireLogger.logger.info("Removing block data for {}", conn);
		if(isClient)
			forEachSection(conn, (sectionPos, segments) -> {
				synchronized(sectionsToWires)
				{
					sectionsToWires.computeIfAbsent(sectionPos, $ -> new ArrayList<>()).remove(segments);
				}
			});
		else
			WireUtils.raytraceAlongCatenary(conn, p -> remove(p.block(), conn), p -> remove(p.block(), conn));
		conn.blockDataGenerated = false;
	}

	private void forEachSection(Connection conn, BiConsumer<SectionPos, ConnectionSegments> out)
	{
		Mutable<SectionPos> currentSection = new MutableObject<>(null);
		MutableInt sectionStart = new MutableInt(0);
		WireUtils.forEachRenderPoint(conn, (id, relative, section) -> {
			if(currentSection.getValue()!=null&&
					(!section.equals(currentSection.getValue())||id==Connection.RENDER_POINTS_PER_WIRE))
			{
				out.accept(currentSection.getValue(), new ConnectionSegments(conn, sectionStart.intValue(), id));
				currentSection.setValue(null);
			}
			if(currentSection.getValue()==null)
			{
				currentSection.setValue(section);
				sectionStart.setValue(id);
			}
		});
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

	@Nullable
	public List<ConnectionSegments> getWiresIn(SectionPos section)
	{
		synchronized(sectionsToWires)
		{
			List<ConnectionSegments> containedWires = sectionsToWires.get(section);
			if(containedWires==null)
				return null;
			else
				return List.copyOf(containedWires);
		}
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

	public record ConnectionSegments(Connection connection, int firstPointToRender, int lastPointToRender)
	{
	}
}
