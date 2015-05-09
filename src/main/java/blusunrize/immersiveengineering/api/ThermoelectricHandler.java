package blusunrize.immersiveengineering.api;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 * @author BluSunrize - 08.05.2015
 *
 * The temperature registry to allow additional blocks to work with the Thermoelectric Generator<br>
 * Register either OreDictionary names or 
 */
public class ThermoelectricHandler
{
	static HashMap<String, Integer> temperatureMap = new HashMap<String, Integer>();

	public static void registerSourceInKelvin(String source, int value)
	{
		temperatureMap.put(source, value);
	}
	public static void registerSourceInCelsius(String source, int value)
	{
		temperatureMap.put(source, value+273);
	}
	/** 'murica!
	 */
	public static void registerSourceInFarenheit(String source, int value)
	{
		temperatureMap.put(source, (int)Math.round((value-32)/1.8D +273) );
	}

	public static int getTemperature(Block block, int meta)
	{
		for(int oreID : OreDictionary.getOreIDs(new ItemStack(block, 1, meta)))
			if(temperatureMap.containsKey(OreDictionary.getOreName(oreID)))
				return temperatureMap.get(OreDictionary.getOreName(oreID));
		if(temperatureMap.containsKey(ApiUtils.nameFromStack(new ItemStack(block, 1, meta))+"::"+meta))
			return temperatureMap.get(ApiUtils.nameFromStack(new ItemStack(block, 1, meta))+"::"+meta);
		return -1;
	}
}
