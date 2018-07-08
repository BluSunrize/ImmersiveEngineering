/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.inventory;

import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author BluSunrize - 20.02.2017
 */
public class MultiFluidTank implements IFluidTank, IFluidHandler
{
	public ArrayList<FluidStack> fluids = new ArrayList<>();
	private final int capacity;

	public MultiFluidTank(int capacity)
	{
		this.capacity = capacity;
	}

	public MultiFluidTank readFromNBT(NBTTagCompound nbt)
	{
		if(nbt.hasKey("fluids"))
		{
			fluids.clear();
			NBTTagList tagList = nbt.getTagList("fluids", 10);
			for(int i = 0; i < tagList.tagCount(); i++)
			{
				FluidStack fs = FluidStack.loadFluidStackFromNBT(tagList.getCompoundTagAt(i));
				if(fs!=null)
					this.fluids.add(fs);
			}
		}
		return this;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		NBTTagList tagList = new NBTTagList();
		for(FluidStack fs : this.fluids)
			if(fs!=null)
				tagList.appendTag(fs.writeToNBT(new NBTTagCompound()));
		nbt.setTag("fluids", tagList);
		return nbt;
	}

	public int getFluidTypes()
	{
		return fluids.size();
	}

	@Nullable
	@Override
	public FluidStack getFluid()
	{
		//grabbing the last fluid, for output reasons
		return fluids.size() > 0?fluids.get(fluids.size()-1): null;
	}

	@Override
	public int getFluidAmount()
	{
		int sum = 0;
		for(FluidStack fs : fluids)
			sum += fs.amount;
		return sum;
	}

	@Override
	public int getCapacity()
	{
		return this.capacity;
	}

	@Override
	public FluidTankInfo getInfo()
	{
		FluidStack fs = getFluid();
		int capacity = this.capacity-getFluidAmount();
		if(fs!=null)
			capacity += fs.amount;
		return new FluidTankInfo(fs, capacity);
	}

	@Override
	public IFluidTankProperties[] getTankProperties()
	{
		return new IFluidTankProperties[0];
	}

	@Override
	public int fill(FluidStack resource, boolean doFill)
	{
		int space = this.capacity-getFluidAmount();
		int toFill = Math.min(resource.amount, space);
		if(!doFill)
			return toFill;
		for(FluidStack fs : this.fluids)
			if(fs.isFluidEqual(resource))
			{
				fs.amount += toFill;
				return toFill;
			}
		this.fluids.add(Utils.copyFluidStackWithAmount(resource, toFill, true));
		return toFill;

	}

	@Nullable
	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain)
	{
		if(this.fluids.isEmpty())
			return null;
		Iterator<FluidStack> it = this.fluids.iterator();
		while(it.hasNext())
		{
			FluidStack fs = it.next();
			if(fs.isFluidEqual(resource))
			{
				int amount = Math.min(resource.amount, fs.amount);
				if(doDrain)
				{
					fs.amount -= amount;
					if(fs.amount <= 0)
						it.remove();
				}
				return Utils.copyFluidStackWithAmount(resource, amount, true);
			}
		}
		return null;
	}

	public static FluidStack drain(int remove, FluidStack removeFrom, Iterator<FluidStack> removeIt, boolean doDrain)
	{
		int amount = Math.min(remove, removeFrom.amount);
		if(doDrain)
		{
			removeFrom.amount -= amount;
			if(removeFrom.amount <= 0)
				removeIt.remove();
		}
		return Utils.copyFluidStackWithAmount(removeFrom, amount, true);
	}

	@Nullable
	@Override
	public FluidStack drain(int maxDrain, boolean doDrain)
	{
		if(this.fluids.isEmpty())
			return null;
		return drain(new FluidStack(getFluid(), maxDrain), doDrain);
	}
}
