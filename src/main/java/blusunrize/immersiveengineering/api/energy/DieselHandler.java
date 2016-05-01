package blusunrize.immersiveengineering.api.energy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import blusunrize.immersiveengineering.api.ApiUtils;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * @author BluSunrize - 23.04.2015
 *
 * The Fuel Handler for the Diesel Generator. Use this to register custom fuels
 */
public class DieselHandler
{
	static HashMap<String, Integer> dieselGenBurnTime = new HashMap<String, Integer>();
	static Set<Fluid> drillFuel = new HashSet<Fluid>();
	
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

	public static void registerDrillFuel(Fluid fuel)
	{
		if(fuel!=null)
			drillFuel.add(fuel);
	}
	public static boolean isValidDrillFuel(Fluid fuel)
	{
		return fuel!=null && drillFuel.contains(fuel);
	}

	//	static HashMap<String, Integer> ethanolOutput = new HashMap<String, Integer>();
	//	/**
	//	 * @param input either itemstack or OreDictionary name
	//	 * @param output the output of ethanol in mB per item
	//	 */
	//	public static void registerEthanolSource(Object input, int output)
	//	{
	//		if(input instanceof String)
	//			ethanolOutput.put((String)input, output);
	//		else 
	//		{
	//			if(input instanceof Item)
	//				input = new ItemStack((Item)input);
	//			if(input instanceof Block)
	//				input = new ItemStack((Block)input);
	//			if(input instanceof ItemStack && !ApiUtils.nameFromStack((ItemStack)input).isEmpty())
	//				ethanolOutput.put(ApiUtils.nameFromStack((ItemStack)input)+"::"+((ItemStack)input).getItemDamage(), output);
	//		}
	//	}
	//	public static int getEthanolOutput(ItemStack stack)
	//	{
	//		for(Map.Entry<String,Integer> e : ethanolOutput.entrySet())
	//			if(ApiUtils.compareToOreName(stack, e.getKey()))
	//				return e.getValue();
	//			else
	//			{
	//				int lIndx = e.getKey().lastIndexOf("::");
	//				if(lIndx>0)
	//				{
	//					String key = e.getKey().substring(0,lIndx);
	//					try{
	//						int reqMeta = Integer.parseInt(e.getKey().substring(lIndx+2));
	//						if(key.equals(ApiUtils.nameFromStack(stack)) && (reqMeta==OreDictionary.WILDCARD_VALUE || reqMeta==stack.getItemDamage()))
	//							return e.getValue();
	//					}catch(Exception exception){}
	//				}
	//			}
	//		return 0;
	//	}
	//	public static HashMap<String, Integer> getEthanolValues()
	//	{
	//		return ethanolOutput;
	//	}
	//	public static Map<String, Integer> getEthanolValuesSorted(boolean inverse)
	//	{
	//		return ApiUtils.sortMap(ethanolOutput, inverse);
	//	}

	public static class RefineryRecipe
	{
		public final FluidStack input0;
		public final FluidStack input1;
		public final FluidStack output;
		public RefineryRecipe(FluidStack input0, FluidStack input1, FluidStack output)
		{
			this.input0 = input0;
			this.input1 = input1;
			this.output = output;
		}
	}
	public static ArrayList<RefineryRecipe> refineryList = new ArrayList<RefineryRecipe>();
	public static void addRefineryRecipe(FluidStack input0, FluidStack input1, FluidStack output)
	{
		refineryList.add(new RefineryRecipe(input0, input1, output));
	}
	public static RefineryRecipe findIncompleteRefineryRecipe(FluidStack input0, FluidStack input1)
	{
		if(input0==null && input1==null)
			return null;
		for(RefineryRecipe recipe : refineryList)
		{
			if(input0!=null && input1==null)
			{
				if(input0.isFluidEqual(recipe.input0) || input0.isFluidEqual(recipe.input1))
					return recipe;
			}
			else if(input0==null && input1!=null)
			{
				if(input1.isFluidEqual(recipe.input0) || input1.isFluidEqual(recipe.input1))
					return recipe;
			}
			else if( (input0.isFluidEqual(recipe.input0)&&input1.isFluidEqual(recipe.input1)) || (input0.isFluidEqual(recipe.input1)&&input1.isFluidEqual(recipe.input0)) )
				return recipe;
		}
		return null;
	}
	public static RefineryRecipe findRefineryRecipe(FluidStack input0, FluidStack input1)
	{
		if(input0==null || input1==null)
			return null;
		for(RefineryRecipe recipe : refineryList)
		{
			if( (input0.isFluidEqual(recipe.input0)&&input1.isFluidEqual(recipe.input1)) || (input0.isFluidEqual(recipe.input1)&&input1.isFluidEqual(recipe.input0)) )
				return recipe;
		}
		return null;
	}
	//	public static List<RefineryRecipe> removeFermenterRecipes(ItemStack stack)
	//	{
	//		List<RefineryRecipe> list = new ArrayList();
	//		Iterator<RefineryRecipe> it = fermenterList.iterator();
	//		while(it.hasNext())
	//		{
	//			RefineryRecipe ir = it.next();
	//			if(OreDictionary.itemMatches(ir.output, stack, true))
	//			{
	//				list.add(ir);
	//				it.remove();
	//			}
	//		}
	//		return list;
	//	}
}