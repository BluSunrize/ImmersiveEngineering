package blusunrize.immersiveengineering.api;

import java.util.HashMap;

import net.minecraftforge.fluids.Fluid;

/**
 * @author BluSunrize - 23.04.2015
 *
 * The Fuel Handler for the Diesel Generator. Use this to register custom fuels
 */
public class DieselGeneratorHandler
{
	static HashMap<String, Integer> burnTime = new HashMap<String, Integer>();

	/**
	 * @param fuel the fluid to be used as fuel
	 * @param time the total burn time gained from 1000 mB
	 */
	public static void registerFuel(Fluid fuel, int time)
	{
		if(fuel!=null)
			burnTime.put(fuel.getName(), time);
	}
	public static int getBurnTime(Fluid fuel)
	{
		if(fuel!=null)
			return burnTime.get(fuel.getName());
		return 0;
	}
	public static boolean isValidFuel(Fluid fuel)
	{
		if(fuel!=null)
			return burnTime.containsKey(fuel.getName());
		return false;
	}
}
