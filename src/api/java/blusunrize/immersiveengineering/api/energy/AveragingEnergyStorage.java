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
	private int averageInsertion = 0;
	private int averageExtraction = 0;
	private static final double AVERAGE_DECAY_FACTOR = .5;

	public AveragingEnergyStorage(int capacity)
	{
		super(capacity);
	}

	@Override
	public int receiveEnergy(int energy, boolean simulate)
	{
		int received = super.receiveEnergy(energy, simulate);
		if(!simulate)
			averageInsertion = (int)Math.round(averageInsertion*AVERAGE_DECAY_FACTOR+received*(1-AVERAGE_DECAY_FACTOR));
		return received;
	}

	@Override
	public int extractEnergy(int energy, boolean simulate)
	{
		int extracted = super.extractEnergy(energy, simulate);
		if(!simulate)
			averageExtraction = (int)Math.round(averageExtraction*AVERAGE_DECAY_FACTOR+extracted*(1-AVERAGE_DECAY_FACTOR));
		return extracted;
	}

	public int getAverageInsertion()
	{
		return averageInsertion;
	}
}
