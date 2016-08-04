package blusunrize.immersiveengineering.api.energy.wires;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

/**
 * @author BluSunrize - 08.03.2015
 *
 * An interface to be implemented by TileEntities, to allow them to connect to the IE net
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
	BlockPos getConnectionMaster(WireType cableType, TargetingInfo target);
	
	/**
	 * @return whether you can connect the given CableType to the tile
	 */
	boolean canConnectCable(WireType cableType, TargetingInfo target);
	
	/**
	 * fired when a cable is attached, use to limit the cables attached to one type
	 */
	void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other);
	
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
	void onEnergyPassthrough(int amount);
	
	/**
	 * used to reset the CableType limiter of the tile, provided it matches the given argument
	 * acts as a wildcard, meaning if connection.CableType is null, you /always/ reset the limiter
	 */
	void removeCable(Connection connection);
	
	/**
	 * @return the offset used when RayTracing to or from this block. This vector is based from the blocks /origin/
	 */
	Vec3d getRaytraceOffset(IImmersiveConnectable link);
	/**
	 * Used for rendering only
	 * @return Where the cable should attach
	 */
	Vec3d getConnectionOffset(Connection con);
	/**
	 * returns a set of Blocks to be ignored when raytracing
	 */
	default Set<BlockPos> getIgnored(IImmersiveConnectable other)
	{
		return ImmutableSet.of(ApiUtils.toBlockPos(this));
	}
}