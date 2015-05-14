package blusunrize.immersiveengineering.api;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.oredict.OreDictionary;

/**
 * @author BluSunrize - 23.04.2015
 *
 * The Fuel Handler for the Diesel Generator. Use this to register custom fuels
 */
public class DieselHandler
{
	static HashMap<String, Integer> dieselGenBurnTime = new HashMap<String, Integer>();
	/**
	 * @param fuel the fluid to be used as fuel
	 * @param time the total burn time gained from 1000 mB
	 */
	public static void registerFuel(Fluid fuel, int time)
	{
		if(fuel!=null)
			dieselGenBurnTime.put(fuel.getName(), time);
	}
	public static int getBurnTime(Fluid fuel)
	{
		if(fuel!=null)
			return dieselGenBurnTime.get(fuel.getName());
		return 0;
	}
	public static boolean isValidFuel(Fluid fuel)
	{
		if(fuel!=null)
			return dieselGenBurnTime.containsKey(fuel.getName());
		return false;
	}
	public static HashMap<String, Integer> getFuelValues()
	{
		return dieselGenBurnTime;
	}
	public static Map<String, Integer> getFuelValuesSorted(boolean inverse)
	{
		return ApiUtils.sortMap(dieselGenBurnTime, inverse);
	}


	static HashMap<String, Integer> plantoilOutput = new HashMap<String, Integer>();
	/**
	 * @param input either itemstack or OreDictionary name
	 * @param output the output of plant oil in mB per item
	 */
	public static void registerPlantoilSource(Object input, int output)
	{
		if(input instanceof String)
			plantoilOutput.put((String)input, output);
		else
		{
			if(input instanceof Item)
				input = new ItemStack((Item)input);
			if(input instanceof Block)
				input = new ItemStack((Block)input);
			if(input instanceof ItemStack && !ApiUtils.nameFromStack((ItemStack)input).isEmpty())
				plantoilOutput.put(ApiUtils.nameFromStack((ItemStack)input)+"::"+((ItemStack)input).getItemDamage(), output);
		}
	}
	public static int getPlantoilOutput(ItemStack stack)
	{
		for(Map.Entry<String,Integer> e : plantoilOutput.entrySet())
		{
			if(ApiUtils.compareToOreName(stack, e.getKey()))
				return e.getValue();
			else
			{
				int lIndx = e.getKey().lastIndexOf("::");
				if(lIndx>0)
				{
					String key = e.getKey().substring(0,lIndx);
					try{
						int reqMeta = Integer.parseInt(e.getKey().substring(lIndx+2));
						if(key.equals(ApiUtils.nameFromStack(stack)) && (reqMeta==OreDictionary.WILDCARD_VALUE || reqMeta==stack.getItemDamage()))
							return e.getValue();
					}catch(Exception exception){}
				}
			}
		}
		return 0;
	}
	public static HashMap<String, Integer> getPlantoilValues()
	{
		return plantoilOutput;
	}
	public static Map<String, Integer> getPlantoilValuesSorted(boolean inverse)
	{
		return ApiUtils.sortMap(plantoilOutput, inverse);
	}


	static HashMap<String, Integer> ethanolOutput = new HashMap<String, Integer>();
	/**
	 * @param input either itemstack or OreDictionary name
	 * @param output the output of ethanol in mB per item
	 */
	public static void registerEthanolSource(Object input, int output)
	{
		if(input instanceof String)
			ethanolOutput.put((String)input, output);
		else 
		{
			if(input instanceof Item)
				input = new ItemStack((Item)input);
			if(input instanceof Block)
				input = new ItemStack((Block)input);
			if(input instanceof ItemStack && !ApiUtils.nameFromStack((ItemStack)input).isEmpty())
				ethanolOutput.put(ApiUtils.nameFromStack((ItemStack)input)+"::"+((ItemStack)input).getItemDamage(), output);
		}
	}
	public static int getEthanolOutput(ItemStack stack)
	{
		for(Map.Entry<String,Integer> e : ethanolOutput.entrySet())
			if(ApiUtils.compareToOreName(stack, e.getKey()))
				return e.getValue();
			else
			{
				int lIndx = e.getKey().lastIndexOf("::");
				if(lIndx>0)
				{
					String key = e.getKey().substring(0,lIndx);
					try{
						int reqMeta = Integer.parseInt(e.getKey().substring(lIndx+2));
						if(key.equals(ApiUtils.nameFromStack(stack)) && (reqMeta==OreDictionary.WILDCARD_VALUE || reqMeta==stack.getItemDamage()))
							return e.getValue();
					}catch(Exception exception){}
				}
			}
		return 0;
	}
	public static HashMap<String, Integer> getEthanolValues()
	{
		return ethanolOutput;
	}
	public static Map<String, Integer> getEthanolValuesSorted(boolean inverse)
	{
		return ApiUtils.sortMap(ethanolOutput, inverse);
	}
}