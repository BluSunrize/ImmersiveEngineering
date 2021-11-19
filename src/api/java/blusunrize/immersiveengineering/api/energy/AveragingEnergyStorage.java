/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.energy;

public class AveragingEnergyStorage extends MutableEnergyStorage
{
	int averageInsertion = 0;
	int averageExtraction = 0;
	double averageDecayFactor = .5;

	public AveragingEnergyStorage(int capacity)
	{
		super(capacity);
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
}
