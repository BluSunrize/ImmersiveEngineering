package blusunrize.immersiveengineering.api;

import net.minecraft.util.Vec3;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler.Connection;

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
	public boolean canConnect();
	
	/**
	 * @return if the tile can in or output energy from/to the network
	 */
	public boolean isRFInOutput();
	
	/**
	 * @return the amount of power that was output
	 */
	public int outputRedstoneFlux(int amount, boolean simulate);
	
	/**
	 * @return wether you can connect the given CableType to the tile
	 */
	public boolean canConnectCable(WireType cableType, TargetingInfo target);
	
	/**
	 * fired when a cable is attached, use to limit the cables attached to one type
	 */
	public void connectCable(WireType cableType, TargetingInfo target);
	
	/**
	 * get the CableType limiter of the tile
	 */
	public WireType getCableLimiter(TargetingInfo target);
	
	/**
	 * used to reset the CableType limiter of the tile, provided it matches the given argument
	 * null acts as a wildcard, meaning if null is parsed, you /always/ reset the limiter
	 */
	public void removeCable(WireType cableType);
	
	/**
	 * @return the offset used when RayTracing to or from this block. This vector is based from the blocks /origin/
	 */
	public Vec3 getRaytraceOffset();
	/**
	 * Used for rendering only
	 * @return Where the cable should attach
	 */
	public Vec3 getConnectionOffset(Connection con);
}