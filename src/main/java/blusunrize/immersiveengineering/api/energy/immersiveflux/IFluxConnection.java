package blusunrize.immersiveengineering.api.energy.immersiveflux;

import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;

/**
 * An interface to be implemented by TileEntities that can connect to an IF network
 * 
 * @author BluSunrize - 18.01.2016
 *
 */
public interface IFluxConnection
{
	/**
	 * @param from		The direction the check is performed from, null for unknown.
	 * @return			If the TileEntity can connect.
	 */
	boolean canConnectEnergy(@Nullable EnumFacing from);
}