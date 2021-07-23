/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.localhandlers.ILocalHandlerProvider;
import blusunrize.immersiveengineering.api.wires.utils.WireUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

/**
 * @author BluSunrize - 08.03.2015
 * <p>
 * An interface to be implemented by TileEntities, to allow them to connect to the IE net
 * <p>
 * "Vec3i offset" parameters give the offset between this block and the one that was clicked on, see getConnectionMaster
 */
public interface IImmersiveConnectable extends ILocalHandlerProvider
{
	/**
	 * @return if wires can directly connect to this
	 */
	boolean canConnect();

	/**
	 * @return a blockPos to do the connection check for.<br>For multiblocks like transformers
	 */
	BlockPos getConnectionMaster(@Nullable WireType cableType, TargetingInfo target);

	/**
	 * @return whether you can connect the given CableType to the tile
	 */
	boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset);

	/**
	 * fired when a cable is attached, use to limit the cables attached to one type
	 */
	void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget);

	@Nullable
	ConnectionPoint getTargetedPoint(TargetingInfo info, Vec3i offset);

	/**
	 * used to reset the CableType limiter of the tile, provided it matches the given argument
	 * acts as a wildcard, meaning if connection.CableType is null, you /always/ reset the limiter
	 */
	void removeCable(@Nullable Connection connection, ConnectionPoint attachedPoint);


	/**
	 * @return Where the cable should attach
	 */
	Vec3 getConnectionOffset(@Nonnull Connection con, ConnectionPoint here);

	/**
	 * returns a set of Blocks to be ignored when raytracing
	 */
	default Set<BlockPos> getIgnored(IImmersiveConnectable other)
	{
		return ImmutableSet.of(WireUtils.toBlockPos(this));
	}

	Collection<ConnectionPoint> getConnectionPoints();

	default Iterable<? extends Connection> getInternalConnections()
	{
		return ImmutableList.of();
	}

	default boolean isProxy()
	{
		return false;
	}

	default BlockPos getPosition()
	{
		return WireUtils.toBlockPos(this);
	}
}