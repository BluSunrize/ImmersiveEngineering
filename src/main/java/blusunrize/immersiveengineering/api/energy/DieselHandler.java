/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy;

import blusunrize.immersiveengineering.api.ApiUtils;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author BluSunrize - 23.04.2015
 * <p>
 * The Fuel Handler for the Diesel Generator. Use this to register custom fuels
 */
public class DieselHandler
{
	static final HashMap<ResourceLocation, Integer> dieselGenBurnTime = new HashMap<>();
	static final Set<Fluid> drillFuel = new HashSet<>();

	/**
	 * @param fuel the fluid to be used as fuel
	 * @param time the total burn time gained from 1000 mB
	 */
	public static void registerFuel(Fluid fuel, int time)
	{
		if(fuel!=null)
			dieselGenBurnTime.put(fuel.getRegistryName(), time);
	}

	public static int getBurnTime(Fluid fuel)
	{
		if(fuel!=null)
		{
			ResourceLocation s = fuel.getRegistryName();
			if(dieselGenBurnTime.containsKey(s))
				return dieselGenBurnTime.get(s);
		}
		return 0;
	}

	public static boolean isValidFuel(Fluid fuel)
	{
		if(fuel!=null)
			return dieselGenBurnTime.containsKey(fuel.getRegistryName());
		return false;
	}

	public static HashMap<ResourceLocation, Integer> getFuelValues()
	{
		return dieselGenBurnTime;
	}

	public static Map<ResourceLocation, Integer> getFuelValuesSorted(boolean inverse)
	{
		return ApiUtils.sortMap(dieselGenBurnTime, inverse);
	}

	public static void registerDrillFuel(Fluid fuel)
	{
		if(fuel!=null)
			drillFuel.add(fuel);
	}

	public static boolean isValidDrillFuel(Fluid fuel)
	{
		return fuel!=null&&drillFuel.contains(fuel);
	}

	public static void removeFuel(Fluid fuel)
	{
		if(fuel!=null)
			dieselGenBurnTime.remove(fuel.getRegistryName());
	}

	public static void removeDrillFuel(Fluid fuel)
	{
		drillFuel.remove(fuel);
	}
}