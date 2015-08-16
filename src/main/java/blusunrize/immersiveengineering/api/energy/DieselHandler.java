package blusunrize.immersiveengineering.api.energy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.IEContent;

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

	public static class SqueezerRecipe
	{
		public final Object input;
		public final ItemStack output;
		public final int time;
		public final FluidStack fluid;
		public SqueezerRecipe(Object input, int time, FluidStack fluid, ItemStack itemOutput)
		{
			this.input = ApiUtils.convertToValidRecipeInput(input);
			this.output=itemOutput;
			this.time=time;
			this.fluid=fluid;
		}
	}
	public static ArrayList<SqueezerRecipe> squeezerList = new ArrayList<SqueezerRecipe>();
	public static void addSqueezerRecipe(Object input, int time, FluidStack fluid, ItemStack itemOutput)
	{
		if(input instanceof String && !OreDictionary.doesOreNameExist((String)input))
			return;
		squeezerList.add(new SqueezerRecipe(input, time, fluid, itemOutput));
	}
	public static SqueezerRecipe findSqueezerRecipe(ItemStack input)
	{
		for(SqueezerRecipe recipe : squeezerList)
			if(ApiUtils.stackMatchesObject(input, recipe.input))
				return recipe;
		return null;
	}
	public static List<SqueezerRecipe> removeSqueezerRecipes(ItemStack stack)
	{
		List<SqueezerRecipe> list = new ArrayList();
		Iterator<SqueezerRecipe> it = squeezerList.iterator();
		while(it.hasNext())
		{
			SqueezerRecipe ir = it.next();
			if(OreDictionary.itemMatches(ir.output, stack, true))
			{
				list.add(ir);
				it.remove();
			}
		}
		return list;
	}
	public static Map<String, Integer> getPlantoilValuesSorted(boolean inverse)
	{
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for(SqueezerRecipe recipe : squeezerList)
			if(recipe.fluid!=null && recipe.fluid.getFluid()==IEContent.fluidPlantoil)
			{
				ItemStack is = (ItemStack)(recipe.input instanceof ArrayList?((ArrayList)recipe.input).get(0): recipe.input instanceof ItemStack?recipe.input: null);
				map.put(is.getDisplayName(), recipe.fluid.amount);
			}
		return ApiUtils.sortMap(map, inverse);
	}

	public static class FermenterRecipe
	{
		public final Object input;
		public final ItemStack output;
		public final int time;
		public final FluidStack fluid;
		public FermenterRecipe(Object input, int time, FluidStack fluid, ItemStack itemOutput)
		{
			this.input = ApiUtils.convertToValidRecipeInput(input);
			this.output=itemOutput;
			this.time=time;
			this.fluid=fluid;
		}
	}
	public static ArrayList<FermenterRecipe> fermenterList = new ArrayList<FermenterRecipe>();
	public static void addFermenterRecipe(Object input, int time, FluidStack fluid, ItemStack itemOutput)
	{
		if(input instanceof String && !OreDictionary.doesOreNameExist((String)input))
			return;
		fermenterList.add(new FermenterRecipe(input, time, fluid, itemOutput));
	}
	public static FermenterRecipe findFermenterRecipe(ItemStack input)
	{
		for(FermenterRecipe recipe : fermenterList)
			if(ApiUtils.stackMatchesObject(input, recipe.input))
				return recipe;
		return null;
	}
	public static List<FermenterRecipe> removeFermenterRecipes(ItemStack stack)
	{
		List<FermenterRecipe> list = new ArrayList();
		Iterator<FermenterRecipe> it = fermenterList.iterator();
		while(it.hasNext())
		{
			FermenterRecipe ir = it.next();
			if(OreDictionary.itemMatches(ir.output, stack, true))
			{
				list.add(ir);
				it.remove();
			}
		}
		return list;
	}
	public static Map<String, Integer> getEthanolValuesSorted(boolean inverse)
	{
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for(FermenterRecipe recipe : fermenterList)
			if(recipe.fluid!=null && recipe.fluid.getFluid()==IEContent.fluidEthanol)
			{
				ItemStack is = (ItemStack)(recipe.input instanceof ArrayList?((ArrayList)recipe.input).get(0): recipe.input instanceof ItemStack?recipe.input: null);
				map.put(is.getDisplayName(), recipe.fluid.amount);
			}
		return ApiUtils.sortMap(map, inverse);
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