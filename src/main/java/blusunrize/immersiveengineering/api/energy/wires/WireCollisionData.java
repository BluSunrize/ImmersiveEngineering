/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.wires;

import blusunrize.immersiveengineering.api.ApiUtils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import java.util.Collection;

public class WireCollisionData
{
	private final Multimap<BlockPos, CollisionInfo> blockToWires = HashMultimap.create();
	private final GlobalWireNetwork net;

	public WireCollisionData(GlobalWireNetwork net)
	{
		this.net = net;
	}

	public void addConnection(Connection conn)
	{
		if(!conn.blockDataGenerated)
		{
			ApiUtils.raytraceAlongCatenary(conn, net.getLocalNet(conn.getEndA()), (p) ->
			{
				blockToWires.put(p.getLeft(), new CollisionInfo(p.getMiddle(), p.getRight(), conn, true));
				return false;
			}, (p) ->
					blockToWires.put(p.getLeft(), new CollisionInfo(p.getMiddle(), p.getRight(), conn, false)));
			conn.blockDataGenerated = true;
		}
	}

	public void removeConnection(Connection conn)
	{
		if(conn.blockDataGenerated)
		{
			ApiUtils.raytraceAlongCatenary(conn, net.getLocalNet(conn.getEndA()), (p) ->
			{
				blockToWires.remove(p.getLeft(), new CollisionInfo(p.getMiddle(), p.getRight(), conn, true));
				return false;
			}, (p) ->
					blockToWires.remove(p.getLeft(), new CollisionInfo(p.getMiddle(), p.getRight(), conn, false)));
			conn.blockDataGenerated = false;
		}
	}

	public Collection<CollisionInfo> getCollisionInfo(BlockPos pos)
	{
		return blockToWires.get(pos);
	}

	public class CollisionInfo
	{
		@Nonnull
		public final Vec3d intersectA;
		@Nonnull
		public final Vec3d intersectB;
		@Nonnull
		public final Connection conn;
		public final boolean isInBlock;

		public CollisionInfo(@Nonnull Vec3d intersectA, @Nonnull Vec3d intersectB, @Nonnull Connection conn,
							 boolean isInBlock)
		{
			this.intersectA = intersectA;
			this.intersectB = intersectB;
			this.conn = conn;
			this.isInBlock = isInBlock;
		}

		public LocalWireNetwork getLocalNet()
		{
			return net.getLocalNet(conn.getEndA());
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
