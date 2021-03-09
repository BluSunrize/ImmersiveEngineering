package blusunrize.immersiveengineering.common.util.compat.computers.generic.impl;

import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import java.util.function.Function;

public class TankCallbacks<T> extends Callback<T>
{
	private final Function<T, IFluidTank> getTank;
	private final String desc;

	public TankCallbacks(Function<T, IFluidTank> getTank, String desc)
	{
		this.getTank = getTank;
		this.desc = desc;
	}

	@Override
	public String renameMethod(String javaName)
	{
		return javaName.replace("Desc", capitalize(this.desc));
	}

	@ComputerCallable
	public FluidStack getDescFluid(CallbackEnvironment<T> env)
	{
		return getTank.apply(env.getObject()).getFluid();
	}

	@ComputerCallable
	public int getDescTankSize(CallbackEnvironment<T> env)
	{
		return getTank.apply(env.getObject()).getCapacity();
	}
}
