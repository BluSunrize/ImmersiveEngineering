/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.immersiveflux;

import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;

/**
 * An interface to be implemented by TileEntities that can connect to an IF network
 *
 * @author BluSunrize - 18.01.2016
 */
public interface IFluxConnection
{
	/**
	 * @param from The direction the check is performed from, null for unknown.
	 * @return If the TileEntity can connect.
	 */
	boolean canConnectEnergy(@Nullable EnumFacing from);
}