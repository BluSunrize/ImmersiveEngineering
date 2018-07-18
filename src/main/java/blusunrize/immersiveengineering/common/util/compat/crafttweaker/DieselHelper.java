/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.energy.DieselHandler;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.liquid.ILiquidStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.immersiveengineering.DieselHandler")
public class DieselHelper
{
	@ZenMethod
	public static void addFuel(ILiquidStack fuel, int time)
	{

		CraftTweakerAPI.apply(new AddFuel(fuel, time));
	}

	private static class AddFuel implements IAction
	{
		private final ILiquidStack fuel;
		private final int time;

		public AddFuel(ILiquidStack fuel, int time)
		{
			this.fuel = fuel;
			this.time = time;
		}

		@Override
		public void apply()
		{
			Fluid fuelFluid = FluidRegistry.getFluid(fuel.getName());
			DieselHandler.registerFuel(fuelFluid, time);
		}

		@Override
		public String describe()
		{
			return "Registering Diesel Generator Fuel "+fuel.getDisplayName();
		}
	}

	@ZenMethod
	public static void addDrillFuel(ILiquidStack fuel)
	{
		CraftTweakerAPI.apply(new AddDrillFuel(fuel));
	}

	private static class AddDrillFuel implements IAction
	{
		private final ILiquidStack fuel;

		public AddDrillFuel(ILiquidStack fuel)
		{
			this.fuel = fuel;
		}

		@Override
		public void apply()
		{
			Fluid fuelFluid = FluidRegistry.getFluid(fuel.getName());
			DieselHandler.registerDrillFuel(fuelFluid);
		}

		@Override
		public String describe()
		{
			return "Registering Drill Fuel "+fuel.getDisplayName();
		}
	}

	@ZenMethod
	public static void removeFuel(ILiquidStack fuel)
	{
		CraftTweakerAPI.apply(new RemoveFuel(fuel));
	}

	private static class RemoveFuel implements IAction
	{
		private final ILiquidStack fuel;

		public RemoveFuel(ILiquidStack fuel)
		{
			this.fuel = fuel;
		}

		@Override
		public void apply()
		{
			Fluid fuelFluid = FluidRegistry.getFluid(fuel.getName());
			DieselHandler.removeFuel(fuelFluid);
		}

		@Override
		public String describe()
		{
			return "Removing Fuel "+fuel.getDisplayName();
		}
	}

	@ZenMethod
	public static void removeDrillFuel(ILiquidStack fuel)
	{
		CraftTweakerAPI.apply(new RemoveDrillFuel(fuel));
	}

	private static class RemoveDrillFuel implements IAction
	{
		private final ILiquidStack fuel;

		public RemoveDrillFuel(ILiquidStack fuel)
		{
			this.fuel = fuel;
		}

		@Override
		public void apply()
		{
			Fluid fuelFluid = FluidRegistry.getFluid(fuel.getName());
			DieselHandler.removeDrillFuel(fuelFluid);
		}

		@Override
		public String describe()
		{
			return "Removing Drill Fuel "+fuel.getDisplayName();
		}
	}


}
