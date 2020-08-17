/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy;

import net.minecraft.fluid.Fluid;
import net.minecraft.tags.Tag;
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
	static final HashMap<Tag<Fluid>, Integer> dieselGenBurnTime = new HashMap<>();
	static final Set<Tag<Fluid>> drillFuel = new HashSet<>();

	/**
	 * @param fuel the fluidtag to be used as fuel
	 * @param time the total burn time gained from 1000 mB
	 */
	public static void registerFuel(Tag<Fluid> fuel, int time)
	{
		if(fuel!=null)
			dieselGenBurnTime.put(fuel, time);
	}

	public static int getBurnTime(Fluid fuel)
	{
		if(fuel!=null)
		{
			ResourceLocation s = fuel.getRegistryName();
			for(Map.Entry<Tag<Fluid>, Integer> entry : dieselGenBurnTime.entrySet())
				if(entry.getKey().contains(fuel))
					return entry.getValue();
		}
		return 0;
	}

	public static boolean isValidFuel(Fluid fuel)
	{
		if(fuel!=null)
			return dieselGenBurnTime.keySet().stream().anyMatch(fluidTag -> fluidTag.contains(fuel));
		return false;
	}

	public static HashMap<Tag<Fluid>, Integer> getFuelValues()
	{
		return dieselGenBurnTime;
	}

	public static void registerDrillFuel(Tag<Fluid> fuel)
	{
		if(fuel!=null)
			drillFuel.add(fuel);
	}

	public static boolean isValidDrillFuel(Fluid fuel)
	{
		return fuel!=null&&drillFuel.stream().anyMatch(fluidTag -> fluidTag.contains(fuel));
	}

}