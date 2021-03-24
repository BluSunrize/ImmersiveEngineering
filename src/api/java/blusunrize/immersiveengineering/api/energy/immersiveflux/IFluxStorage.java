/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.immersiveflux;

/**
 * An interface to implement for an object that stores ImmersiveFlux<br>
 * See {@link FluxStorage} for an example implementation
 *
 * @author BluSunrize - 18.01.2016
 */
public interface IFluxStorage
{
	/**
	 * Inserts energy into the storage. Returns the amount that was accepted.
	 *
	 * @param energy   Maximum amount of energy to be inserted.
	 * @param simulate If TRUE, the process is simulated and will not increase the storage.
	 * @return Amount of energy that was, or would have been, if simulated, accepted.
	 */
	int receiveEnergy(int energy, boolean simulate);

	/**
	 * Extracts energy from the storage. Returns the amount that was extracted.
	 *
	 * @param energy   Maximum amount of energy to be extracted.
	 * @param simulate If TRUE, the process is simulated and will not decrease the storage.
	 * @return Amount of energy that was, or would have been, if simulated, extracted.
	 */
	int extractEnergy(int energy, boolean simulate);

	/**
	 * @return The amount of energy stored.
	 */
	int getEnergyStored();

	/**
	 * @return The maximum amount of energy that can be stored.
	 */
	int getMaxEnergyStored();
}
