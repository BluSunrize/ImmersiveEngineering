/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.immersiveflux;

import net.minecraft.item.ItemStack;

/**
 * An interface to be implemented by Items that can store IF and allow external modification of their storage.
 *
 * @author BluSunrize - 18.01.2016
 */
public interface IFluxContainerItem
{
	/**
	 * Inserts energy into the Item. Returns the amount that was accepted.
	 *
	 * @param container The ItemStack of the container item
	 * @param energy    Maximum amount of energy to be inserted.
	 * @param simulate  If TRUE, the process is simulated and will not increase the storage.
	 * @return Amount of energy that was, or would have been, if simulated, accepted.
	 */
	int receiveEnergy(ItemStack container, int energy, boolean simulate);

	/**
	 * Extracts energy from the Item. Returns the amount that was extracted.
	 *
	 * @param container The ItemStack of the container item
	 * @param energy    Maximum amount of energy to be extracted.
	 * @param simulate  If TRUE, the process is simulated and will not decrease the storage.
	 * @return Amount of energy that was, or would have been, if simulated, extracted.
	 */
	int extractEnergy(ItemStack container, int energy, boolean simulate);

	/**
	 * @param container The ItemStack of the container item
	 * @return The amount of energy stored in the Tile.
	 */
	int getEnergyStored(ItemStack container);

	/**
	 * @param container The ItemStack of the container item
	 * @return The maximum amount of energy that can be stored in the Tile.
	 */
	int getMaxEnergyStored(ItemStack container);
}