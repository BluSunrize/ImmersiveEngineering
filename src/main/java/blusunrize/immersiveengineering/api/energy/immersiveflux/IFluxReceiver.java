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
 * An interface to be implemented by TileEntities that can receive IF.
 *
 * @author BluSunrize - 18.01.2016
 */
public interface IFluxReceiver extends IFluxConnection
{
	/**
	 * Inserts energy into the TileEntity. Returns the amount that was accepted.
	 *
	 * @param from     The direction the energy is inserted from, null for unknown. Unknown directions should ALWAYS work.
	 * @param energy   Maximum amount of energy to be inserted.
	 * @param simulate If TRUE, the process is simulated and will not increase the storage.
	 * @return Amount of energy that was, or would have been, if simulated, accepted.
	 */
	int receiveEnergy(@Nullable EnumFacing from, int energy, boolean simulate);

	/**
	 * @param from The direction the check is performed from, null for unknown.
	 * @return The amount of energy stored in the Tile.
	 */
	int getEnergyStored(@Nullable EnumFacing from);

	/**
	 * @param from The direction the check is performed from, null for unknown.
	 * @return The maximum amount of energy that can be stored in the Tile.
	 */
	int getMaxEnergyStored(@Nullable EnumFacing from);
}