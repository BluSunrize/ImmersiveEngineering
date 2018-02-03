/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.wires;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author BluSunrize - 08.03.2015
 *
 * An interface to be implemented by TileEntities, to allow them to connect to the IE net
 *
 * "Vec3i offset" parameters give the offset between this block and the one that was clicked on, see getConnectionMaster
 */
public interface IImmersiveConnectable
{
	/**
	 * @return if wires can directly connect to this
	 */
	boolean canConnect();
	
	/**
	 * @return if the tile can in or output energy from/to the network
	 */
	boolean isEnergyOutput();
	
	/**
	 * @param amount The amount of power input, in RF
	 * @param simulate whether to actually perform the action or just simulate energy consumption
	 * @param energyType 0 for RF, 1 for EU
	 * @return the amount of power that was output
	 */
	int outputEnergy(int amount, boolean simulate, int energyType);

	/**
	 * @return a blockPos to do the connection check for.<br>For multiblocks like transformers
	 */
	BlockPos getConnectionMaster(@Nullable WireType cableType, TargetingInfo target);

	@Deprecated
	default boolean canConnectCable(WireType cableType, TargetingInfo target)
	{
		return false;
	}
	/**
	 * @return whether you can connect the given CableType to the tile
	 */
	default boolean canConnectCable(WireType cableType, TargetingInfo target, Vec3i offset)
	{
		return canConnectCable(cableType, target);
	}
	
	/**
	 * fired when a cable is attached, use to limit the cables attached to one type
	 */
	default void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other, @Nullable Vec3i offset)
	{
		connectCable(cableType, target, other);
	}
	default void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other)
	{
		connectCable(cableType, target, other, null);
	}
	
	/**
	 * get the CableType limiter of the tile
	 */
	WireType getCableLimiter(TargetingInfo target);
	
	/**
	 * return false to stop checking for available outputs from this point onward
	 * @param con: the connection through which energy enters. May be null, in that
	 * case true should be returned if and only if all connections allow energy to pass
	 */
	boolean allowEnergyToPass(Connection con);

	/**
	 * fired for every not-simulated energy packet passing through. Used for energy meter and stuff
	 */
	default void onEnergyPassthrough(int amount)
	{

	}
	/**
	 * fired for every not-simulated energy packet passing through. Used for energy meter and stuff
	 */
	default void onEnergyPassthrough(double amount)
	{
		onEnergyPassthrough((int)amount);
	}

	/**
	 * Informs the connector/relay that there is a source of energy connected to it, and gives it a way to consume it.
	 * This is valid for a single tick.
	 * This can be used to add "pulling" consumers to the net or allow non-energy-outputs to consume energy (e.g. to damage entities)
	 * @param amount The amount available from this source
	 * @param consume Call this to consume the amount of energy in the parameter
	 */
	default void addAvailableEnergy(float amount, Consumer<Float> consume)
	{}
	
	/**
	 * used to reset the CableType limiter of the tile, provided it matches the given argument
	 * acts as a wildcard, meaning if connection.CableType is null, you /always/ reset the limiter
	 */
	void removeCable(@Nullable Connection connection);


	/**
	 * Raytracing was replaced by code following the catenary, using getConnectionOffset(Connection con, TargetingInfo target)
	 */
	@Deprecated
	default Vec3d getRaytraceOffset(IImmersiveConnectable link) {
		return new Vec3d(.5, .5, .5);
	}

	/**
	 * @return Where the cable should attach
	 */
	Vec3d getConnectionOffset(Connection con);
	/**
	 * A version of getConnectionOffset that works before the connection exists.
	 * Should be identical to getConnectionOffset(Connection) once the connection is added
	 * @return Where the cable should attach
	 */
	default Vec3d getConnectionOffset(Connection con, TargetingInfo target, Vec3i offsetLink)
	{
		return getConnectionOffset(con);
	}
	/**
	 * returns a set of Blocks to be ignored when raytracing
	 */
	default Set<BlockPos> getIgnored(IImmersiveConnectable other)
	{
		return ImmutableSet.of(ApiUtils.toBlockPos(this));
	}

	/**
	 * Returns the amount of damage to be applied to an entity touching a wire connected to this TE. Do not consume energy here.
	 */
	default float getDamageAmount(Entity e, Connection c)
	{
		return 0;
	}

	/**
	 * Consume energy etc. required to hurt the entity by the specified amount. Called whenever an entity was successfully
	 * damaged after calling getDamageAmount
	 */
	default void processDamage(Entity e, float amount, Connection c)
	{}
}