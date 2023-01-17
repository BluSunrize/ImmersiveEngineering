/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.energy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class AveragingEnergyStorage extends MutableEnergyStorage
{
	private int averageInsertion = 0;
	private int averageExtraction = 0;
	private static final double AVERAGE_DECAY_FACTOR = .5;

	public AveragingEnergyStorage(int capacity)
	{
		super(capacity);
	}

	private int tickInsert = 0;
	private int tickExtract = 0;

	/**
	 * This must be called once per tick to keep the average working nicely
	 */
	public void updateAverage()
	{
		// calculate averages
		averageInsertion = (int)Math.round(averageInsertion*AVERAGE_DECAY_FACTOR+tickInsert*(1-AVERAGE_DECAY_FACTOR));
		averageExtraction = (int)Math.round(averageExtraction*AVERAGE_DECAY_FACTOR+tickExtract*(1-AVERAGE_DECAY_FACTOR));
		// reset per-tick amount
		tickInsert = tickExtract = 0;
	}

	@Override
	public int receiveEnergy(int energy, boolean simulate)
	{
		int received = super.receiveEnergy(energy, simulate);
		if(!simulate)
			tickInsert += energy;
		return received;
	}

	@Override
	public int extractEnergy(int energy, boolean simulate)
	{
		int extracted = super.extractEnergy(energy, simulate);
		if(!simulate)
			tickExtract += energy;
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

	@Override
	public Tag serializeNBT()
	{
		final CompoundTag compound = new CompoundTag();
		compound.putInt("energy", energy);
		compound.putInt("averageInsertion", averageInsertion);
		compound.putInt("averageExtraction", averageExtraction);
		return compound;
	}

	@Override
	public void deserializeNBT(Tag nbt)
	{
		if(!(nbt instanceof CompoundTag compound))
			return;
		this.energy = compound.getInt("energy");
		this.averageInsertion = compound.getInt("averageInsertion");
		this.averageExtraction = compound.getInt("averageExtraction");
	}
}
