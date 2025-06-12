/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.inventory;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

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

	public MultiFluidTank readFromNBT(CompoundTag nbt, Provider provider)
	{
		if(nbt.contains("fluids", Tag.TAG_LIST))
		{
			fluids.clear();
			ListTag tagList = nbt.getList("fluids", Tag.TAG_COMPOUND);
			for(int i = 0; i < tagList.size(); i++)
			{
				FluidStack fs = FluidStack.parseOptional(provider, tagList.getCompound(i));
				if(!fs.isEmpty())
					this.fluids.add(fs);
			}
		}
		return this;
	}

	public CompoundTag writeToNBT(CompoundTag nbt, Provider provider)
	{
		ListTag tagList = new ListTag();
		for(FluidStack fs : this.fluids)
			if(!fs.isEmpty())
				tagList.add(fs.save(provider));
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
			if(FluidStack.isSameFluidSameComponents(fs, resource))
			{
				fs.grow(toFill);
				return toFill;
			}
		this.fluids.add(0, resource.copyWithAmount(toFill));
		return toFill;
	}

	public int fillRecipe(FluidStack resource, FluidAction action)
	{
		int space = this.capacity-getFluidAmount();
		int toFill = Math.min(resource.getAmount(), space);
		if(action.simulate())
			return toFill;
		for(FluidStack fs : this.fluids)
			if(FluidStack.isSameFluidSameComponents(fs, resource))
			{
				fs.grow(toFill);
				return toFill;
			}
		this.fluids.add(resource.copyWithAmount(toFill));
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
			if(FluidStack.isSameFluidSameComponents(fs, resource))
			{
				int amount = Math.min(resource.getAmount(), fs.getAmount());
				if(action.execute())
				{
					fs.shrink(amount);
					if(fs.getAmount() <= 0)
						it.remove();
				}
				return resource.copyWithAmount(amount);
			}
		}
		return FluidStack.EMPTY;
	}

	@Nonnull
	public FluidStack drain(FluidIngredient fluidIngredient, int toDrain, FluidAction action)
	{
		if(this.fluids.isEmpty())
			return FluidStack.EMPTY;
		Iterator<FluidStack> it = this.fluids.iterator();
		while(it.hasNext())
		{
			FluidStack fs = it.next();
			if(fluidIngredient.test(fs))
			{
				int amount = Math.min(toDrain, fs.getAmount());
				FluidStack ret = fs.copyWithAmount(amount);
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
		return removeFrom.copyWithAmount(amount);
	}

	@Nonnull
	@Override
	public FluidStack drain(int maxDrain, FluidAction doDrain)
	{
		if(this.fluids.isEmpty())
			return FluidStack.EMPTY;
		return drain(getFluid().copyWithAmount(maxDrain), doDrain);
	}
}
