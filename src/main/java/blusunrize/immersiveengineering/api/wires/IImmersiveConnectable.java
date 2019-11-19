/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.localhandlers.ILocalHandlerProvider;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

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
	 * fired for every not-simulated energy packet passing through. Used for energy meter and stuff
	 */
	@Deprecated
	default void onEnergyPassthrough(int amount)
	{

	}

	/**
	 * fired for every not-simulated energy packet passing through. Used for energy meter and stuff
	 */
	@Deprecated
	default void onEnergyPassthrough(double amount)
	{
		onEnergyPassthrough((int)amount);
	}

	/**
	 * Informs the connector/relay that there is a source of energy connected to it, and gives it a way to consume it.
	 * This is valid for a single tick.
	 * This can be used to add "pulling" consumers to the net or allow non-energy-outputs to consume energy (e.g. to damage entities)
	 *
	 * @param amount  The amount available from this source
	 * @param consume Call this to consume the amount of energy in the parameter
	 */
	@Deprecated
	default void addAvailableEnergy(float amount, Consumer<Float> consume)
	{
	}

	/**
	 * used to reset the CableType limiter of the tile, provided it matches the given argument
	 * acts as a wildcard, meaning if connection.CableType is null, you /always/ reset the limiter
	 */
	void removeCable(@Nullable Connection connection);


	/**
	 * @return Where the cable should attach
	 */
	Vec3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here);

	/**
	 * returns a set of Blocks to be ignored when raytracing
	 */
	default Set<BlockPos> getIgnored(IImmersiveConnectable other)
	{
		return ImmutableSet.of(ApiUtils.toBlockPos(this));
	}

	Collection<ConnectionPoint> getConnectionPoints();

	default Iterable<? extends Connection> getInternalConnections()
	{
		return ImmutableList.of();
	}
}