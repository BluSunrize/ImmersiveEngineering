/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.energy;

import net.minecraftforge.energy.IEnergyStorage;

public record WrappingEnergyStorage(
		IEnergyStorage base, boolean allowInsert, boolean allowExtract, Runnable afterTransfer
) implements IEnergyStorage
{
	public WrappingEnergyStorage(IEnergyStorage base, boolean allowInsert, boolean allowExtract)
	{
		this(base, allowInsert, allowExtract, () -> {
		});
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate)
	{
		if(allowInsert)
			return postTransfer(base.receiveEnergy(maxReceive, simulate), simulate);
		else
			return 0;
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate)
	{
		if(allowExtract)
			return postTransfer(base.extractEnergy(maxExtract, simulate), simulate);
		else
			return 0;
	}

	@Override
	public int getEnergyStored()
	{
		return base.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored()
	{
		return base.getMaxEnergyStored();
	}

	@Override
	public boolean canExtract()
	{
		return allowExtract;
	}

	@Override
	public boolean canReceive()
	{
		return allowInsert;
	}

	private int postTransfer(int transferred, boolean simulate)
	{
		if(!simulate&&transferred > 0)
			afterTransfer.run();
		return transferred;
	}
}
