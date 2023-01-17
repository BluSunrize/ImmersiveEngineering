/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.fluids;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;

public record ArrayFluidHandler(
		IFluidTank[] internal, boolean allowDrain, boolean allowFill, Runnable afterTransfer
) implements IFluidHandler
{
	public ArrayFluidHandler(
			IFluidTank internal, boolean allowDrain, boolean allowFill, Runnable afterTransfer
	)
	{
		this(new IFluidTank[]{internal}, allowDrain, allowFill, afterTransfer);
	}

	public ArrayFluidHandler(boolean allowDrain, boolean allowFill, Runnable afterTransfer, IFluidTank... tanks)
	{
		this(tanks, allowDrain, allowFill, afterTransfer);
	}

	public static ArrayFluidHandler drainOnly(IFluidTank internal, Runnable afterTransfer)
	{
		return new ArrayFluidHandler(internal, true, false, afterTransfer);
	}

	public static ArrayFluidHandler fillOnly(IFluidTank internal, Runnable afterTransfer)
	{
		return new ArrayFluidHandler(internal, false, true, afterTransfer);
	}

	@Override
	public int getTanks()
	{
		return internal.length;
	}

	@Nonnull
	@Override
	public FluidStack getFluidInTank(int tank)
	{
		return internal[tank].getFluid();
	}

	@Override
	public int getTankCapacity(int tank)
	{
		return internal[tank].getCapacity();
	}

	@Override
	public boolean isFluidValid(int tank, @Nonnull FluidStack stack)
	{
		return internal[tank].isFluidValid(stack);
	}

	@Override
	public int fill(FluidStack resource, FluidAction action)
	{
		if(!allowFill||resource.isEmpty())
			return 0;
		FluidStack remaining = resource.copy();
		// iterating twice is actually faster than streams
		IFluidTank existing = null;
		for(IFluidTank tank : internal)
			if(tank.getFluid().isFluidEqual(remaining))
			{
				existing = tank;
				break;
			}
		if(existing!=null)
			remaining.shrink(existing.fill(remaining, action));
		else
			for(IFluidTank tank : internal)
			{
				int filledHere = tank.fill(remaining, action);
				remaining.shrink(filledHere);
				if(filledHere > 0)
					break;
			}
		if(resource.getAmount()!=remaining.getAmount())
			afterTransfer.run();
		return resource.getAmount()-remaining.getAmount();
	}

	@Nonnull
	@Override
	public FluidStack drain(FluidStack resource, FluidAction action)
	{
		if(!allowDrain)
			return FluidStack.EMPTY;
		for(IFluidTank tank : internal)
		{
			FluidStack drainedHere = tank.drain(resource, action);
			if(!drainedHere.isEmpty())
			{
				afterTransfer.run();
				return drainedHere;
			}
		}
		return FluidStack.EMPTY;
	}

	@Nonnull
	@Override
	public FluidStack drain(int maxDrain, FluidAction action)
	{
		if(!allowDrain)
			return FluidStack.EMPTY;
		for(IFluidTank tank : internal)
		{
			FluidStack drainedHere = tank.drain(maxDrain, action);
			if(!drainedHere.isEmpty())
			{
				afterTransfer.run();
				return drainedHere;
			}
		}
		return FluidStack.EMPTY;
	}
}
