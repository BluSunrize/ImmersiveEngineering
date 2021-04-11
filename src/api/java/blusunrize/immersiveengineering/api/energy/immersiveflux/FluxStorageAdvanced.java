/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.immersiveflux;

/**
 * An advanced implementation of {@link IFluxStorage}, keeps track of the average in/output to allow transfer evaluation.
 *
 * @author BluSunrize - 02.02.2016
 */
public class FluxStorageAdvanced extends FluxStorage
{
	int averageInsertion = 0;
	int averageExtraction = 0;
	double averageDecayFactor = .5;

	public FluxStorageAdvanced(int capacity, int limitReceive, int limitExtract)
	{
		super(capacity, limitReceive, limitExtract);
	}

	public FluxStorageAdvanced(int capacity, int limitTransfer)
	{
		super(capacity, limitTransfer);
	}

	public FluxStorageAdvanced(int capacity)
	{
		super(capacity, capacity, capacity);
	}

	@Override
	public int receiveEnergy(int energy, boolean simulate)
	{
		int received = super.receiveEnergy(energy, simulate);
		if(!simulate)
			averageInsertion = (int)Math.round(averageInsertion*averageDecayFactor+received*(1-averageDecayFactor));
		return received;
	}

	@Override
	public int extractEnergy(int energy, boolean simulate)
	{
		int extracted = super.extractEnergy(energy, simulate);
		if(!simulate)
			averageExtraction = (int)Math.round(averageExtraction*averageDecayFactor+extracted*(1-averageDecayFactor));
		return extracted;
	}

	public int getAverageInsertion()
	{
		return averageInsertion;
	}

	public int getAverageExtraction()
	{
		return averageExtraction;
	}

	public FluxStorageAdvanced setDecayFactor(double factor)
	{
		this.averageDecayFactor = factor;
		return this;
	}
}
