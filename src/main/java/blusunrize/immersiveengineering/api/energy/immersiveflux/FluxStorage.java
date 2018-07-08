/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.immersiveflux;

import net.minecraft.nbt.NBTTagCompound;

/**
 * A simple storage object for IF and an example implementation of {@link IFluxStorage}.
 *
 * @author BluSunrize - 18.01.2016
 */
public class FluxStorage implements IFluxStorage
{
	protected int energy;
	protected int capacity;
	protected int limitReceive;
	protected int limitExtract;

	public FluxStorage(int capacity, int limitReceive, int limitExtract)
	{
		this.capacity = capacity;
		this.limitReceive = limitReceive;
		this.limitExtract = limitExtract;
	}

	public FluxStorage(int capacity, int limitTransfer)
	{
		this(capacity, limitTransfer, limitTransfer);
	}

	public FluxStorage(int capacity)
	{
		this(capacity, capacity, capacity);
	}

	public FluxStorage readFromNBT(NBTTagCompound nbt)
	{
		this.energy = nbt.getInteger("ifluxEnergy");
		if(energy > capacity)
			energy = capacity;
		return this;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		if(energy < 0)
			energy = 0;
		nbt.setInteger("ifluxEnergy", energy);
		return nbt;
	}

	public void setCapacity(int capacity)
	{
		this.capacity = capacity;
		if(energy > capacity)
			energy = capacity;
	}

	public void setLimitTransfer(int limitTransfer)
	{
		setLimitReceive(limitTransfer);
		setMaxExtract(limitTransfer);
	}

	public void setLimitReceive(int limitReceive)
	{
		this.limitReceive = limitReceive;
	}

	public void setMaxExtract(int limitExtract)
	{
		this.limitExtract = limitExtract;
	}

	public int getLimitReceive()
	{
		return limitReceive;
	}

	public int getLimitExtract()
	{
		return limitExtract;
	}

	public void setEnergy(int energy)
	{
		this.energy = energy;
		if(this.energy > capacity)
			this.energy = capacity;
		else if(this.energy < 0)
			this.energy = 0;
	}

	public void modifyEnergyStored(int energy)
	{
		this.energy += energy;
		if(this.energy > capacity)
			this.energy = capacity;
		else if(this.energy < 0)
			this.energy = 0;
	}

	@Override
	public int receiveEnergy(int energy, boolean simulate)
	{
		int received = Math.min(capacity-this.energy, Math.min(this.limitReceive, energy));
		if(!simulate)
			this.energy += received;
		return received;
	}

	@Override
	public int extractEnergy(int energy, boolean simulate)
	{
		int extracted = Math.min(this.energy, Math.min(this.limitExtract, energy));
		if(!simulate)
			this.energy -= extracted;
		return extracted;
	}

	@Override
	public int getEnergyStored()
	{
		return energy;
	}

	@Override
	public int getMaxEnergyStored()
	{
		return capacity;
	}
}
