/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.inventory;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author BluSunrize - 20.02.2017
 */
public class MultiFluidTank implements IFluidTank, IFluidHandler
{
	public List<FluidStack> fluids = new ArrayList<>();
	private final int capacity;

	public MultiFluidTank(int capacity)
	{
		this.capacity = capacity;
	}

	public MultiFluidTank readFromNBT(CompoundTag nbt)
	{
		if(nbt.contains("fluids", Tag.TAG_LIST))
		{
			fluids.clear();
			ListTag tagList = nbt.getList("fluids", Tag.TAG_COMPOUND);
			for(int i = 0; i < tagList.size(); i++)
			{
				FluidStack fs = FluidStack.loadFluidStackFromNBT(tagList.getCompound(i));
				if(!fs.isEmpty())
					this.fluids.add(fs);
			}
		}
		return this;
	}

	public CompoundTag writeToNBT(CompoundTag nbt)
	{
		ListTag tagList = new ListTag();
		for(FluidStack fs : this.fluids)
			if(!fs.isEmpty())
				tagList.add(fs.writeToNBT(new CompoundTag()));
		nbt.put("fluids", tagList);
		return nbt;
	}

	public int getFluidTypes()
	{
		return fluids.size();
	}

	@Nonnull
	@Override
	public FluidStack getFluid()
	{
		//grabbing the last fluid, for output reasons
		return fluids.size() > 0?fluids.get(fluids.size()-1): FluidStack.EMPTY;
	}

	@Override
	public int getFluidAmount()
	{
		int sum = 0;
		for(FluidStack fs : fluids)
			sum += fs.getAmount();
		return sum;
	}

	@Override
	public int getCapacity()
	{
		return this.capacity;
	}

	@Override
	public boolean isFluidValid(FluidStack stack)
	{
		return true;
	}

	@Override
	public int getTanks()
	{
		return fluids.size()+1;
	}

	@Nonnull
	@Override
	public FluidStack getFluidInTank(int tank)
	{
		if(tank < fluids.size())
			return fluids.get(tank);
		return FluidStack.EMPTY;
	}

	@Override
	public int getTankCapacity(int tank)
	{
		if(tank < fluids.size())
			return fluids.get(tank).getAmount();
		return this.capacity-getFluidAmount();
	}

	@Override
	public boolean isFluidValid(int tank, @Nonnull FluidStack stack)
	{
		return true;
	}

	@Override
	public int fill(FluidStack resource, FluidAction action)
	{
		int space = this.capacity-getFluidAmount();
		int toFill = Math.min(resource.getAmount(), space);
		if(action.simulate())
			return toFill;
		for(FluidStack fs : this.fluids)
			if(fs.isFluidEqual(resource))
			{
				fs.grow(toFill);
				return toFill;
			}
		this.fluids.add(Utils.copyFluidStackWithAmount(resource, toFill, true));
		return toFill;
	}

	public int fillRecipe(FluidStack resource, FluidAction action)
	{
		int space = this.capacity-getFluidAmount();
		int toFill = Math.min(resource.getAmount(), space);
		if(action.simulate())
			return toFill;
		for(FluidStack fs : this.fluids)
			if(fs.isFluidEqual(resource))
			{
				fs.grow(toFill);
				return toFill;
			}
		this.fluids.add(0, Utils.copyFluidStackWithAmount(resource, toFill, true));
		return toFill;

	}

	@Nonnull
	@Override
	public FluidStack drain(FluidStack resource, FluidAction action)
	{
		if(this.fluids.isEmpty())
			return FluidStack.EMPTY;
		Iterator<FluidStack> it = this.fluids.iterator();
		while(it.hasNext())
		{
			FluidStack fs = it.next();
			if(fs.isFluidEqual(resource))
			{
				int amount = Math.min(resource.getAmount(), fs.getAmount());
				if(action.execute())
				{
					fs.shrink(amount);
					if(fs.getAmount() <= 0)
						it.remove();
				}
				return Utils.copyFluidStackWithAmount(resource, amount, true);
			}
		}
		return FluidStack.EMPTY;
	}

	@Nonnull
	public FluidStack drain(FluidTagInput fluidTag, FluidAction action)
	{
		if(this.fluids.isEmpty())
			return FluidStack.EMPTY;
		Iterator<FluidStack> it = this.fluids.iterator();
		while(it.hasNext())
		{
			FluidStack fs = it.next();
			if(fluidTag.testIgnoringAmount(fs))
			{
				int amount = Math.min(fluidTag.getAmount(), fs.getAmount());
				FluidStack ret = Utils.copyFluidStackWithAmount(fs, amount, true);
				if(action.execute())
				{
					fs.shrink(amount);
					if(fs.getAmount() <= 0)
						it.remove();
				}
				return ret;
			}
		}
		return FluidStack.EMPTY;
	}

	public static FluidStack drain(int remove, FluidStack removeFrom, Iterator<FluidStack> removeIt, FluidAction action)
	{
		int amount = Math.min(remove, removeFrom.getAmount());
		if(action.execute())
		{
			removeFrom.shrink(amount);
			if(removeFrom.isEmpty())
				removeIt.remove();
		}
		return Utils.copyFluidStackWithAmount(removeFrom, amount, true);
	}

	@Nonnull
	@Override
	public FluidStack drain(int maxDrain, FluidAction doDrain)
	{
		if(this.fluids.isEmpty())
			return FluidStack.EMPTY;
		return drain(new FluidStack(getFluid(), maxDrain), doDrain);
	}
}
