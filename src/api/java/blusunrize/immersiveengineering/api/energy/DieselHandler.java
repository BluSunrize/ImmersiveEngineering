/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy;

import net.minecraft.tags.Tag;
import net.minecraft.world.level.material.Fluid;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author BluSunrize - 23.04.2015
 * <p>
 * The Fuel Handler for the Diesel Generator. Use this to register custom fuels
 */
public class DieselHandler
{
	static final List<Pair<Tag<Fluid>, Integer>> dieselGenBurnTime = new ArrayList<>();

	/**
	 * @param fuel the fluidtag to be used as fuel
	 * @param time the total burn time gained from one bucket
	 */
	public static void registerFuel(Tag<Fluid> fuel, int time)
	{
		if(fuel!=null)
			dieselGenBurnTime.add(Pair.of(fuel, time));
	}

	public static int getBurnTime(Fluid fuel)
	{
		if(fuel!=null)
			for(Map.Entry<Tag<Fluid>, Integer> entry : dieselGenBurnTime)
				if(entry.getKey().contains(fuel))
					return entry.getValue();
		return 0;
	}

	public static boolean isValidFuel(Fluid fuel)
	{
		if(fuel!=null)
			return dieselGenBurnTime.stream().anyMatch(pair -> pair.getLeft().contains(fuel));
		return false;
	}

	public static List<Pair<Tag<Fluid>, Integer>> getFuelValues()
	{
		return dieselGenBurnTime;
	}
}