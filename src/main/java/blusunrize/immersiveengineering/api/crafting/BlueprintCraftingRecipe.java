package blusunrize.immersiveengineering.api.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.util.Utils;

import com.google.common.collect.ArrayListMultimap;

/**
 * @author BluSunrize - 21.07.2015
 * <br>
 * These recipes are accessible in the Engineers Workbench, with a Engineers Blueprint item.<br>
 * For every "category" registered, a blueprint item will be added automatically.
 */
public class BlueprintCraftingRecipe
{
	public static ArrayList<String> blueprintCategories = new ArrayList<String>();
	public static ArrayListMultimap<String, BlueprintCraftingRecipe> recipeList = ArrayListMultimap.create();
	public static HashMap<String, ItemStack> villagerPrices = new HashMap<String, ItemStack>();

	public ItemStack output;
	public Object[] inputs;

	public BlueprintCraftingRecipe(ItemStack output, Object[] inputs)
	{
		this.output = output;

		this.inputs = new Object[inputs.length];
		for(int io=0; io<inputs.length; io++)
			this.inputs[io] = ApiUtils.convertToValidRecipeInput(inputs[io]);
	}

	public boolean matchesRecipe(ItemStack[] query)
	{
		//		ArrayList<Object> inputList = new ArrayList();
		//		for(Object i : inputs)
		//			if(i!=null)
		//				inputList.add(i instanceof ItemStack? ((ItemStack)i).copy(): i);
		//		ArrayList<ItemStack> queryList = new ArrayList();
		//		for(ItemStack q : query)
		//			if(q!=null)
		//				queryList.add(q.copy());
		//
		//		Iterator inputIt = inputList.iterator();
		//		while(inputIt.hasNext())
		//		{
		//			boolean match = false;
		//			Object o = inputIt.next();
		//			Iterator<ItemStack> queryIt = queryList.iterator();
		//			while(queryIt.hasNext())
		//			{
		//				ItemStack stack = queryIt.next();
		//				if(ApiUtils.stackMatchesObject(stack, o))
		//				{
		//					if(o instanceof ItemStack)
		//					{
		//						int taken = Math.min(stack.stackSize, ((ItemStack)o).stackSize);
		//						stack.stackSize-=taken;
		//						if(stack.stackSize<=0)
		//						{
		//							queryIt.remove();
		//						}
		//
		//						((ItemStack)o).stackSize-=taken;
		//						if(((ItemStack)o).stackSize<=0)
		//						{
		//							match = true;
		//							inputIt.remove();
		//							break;
		//						}
		//					}
		//					else
		//					{
		//						stack.stackSize--;
		//						if(stack.stackSize<=0)
		//							queryIt.remove();
		//
		//						match = true;
		//						inputIt.remove();
		//						break;
		//					}
		//
		//				}
		//			}
		//			if(!match)
		//			{
		//				return false;
		//			}
		//		}
		//		if(inputList.isEmpty())
		//			return true;
		return getMaxCrafted(query)>0;
	}
	public int getMaxCrafted(ItemStack[] query)
	{
		HashMap<ItemStack, Integer> queryAmount = new HashMap<ItemStack, Integer>();
		for(ItemStack q : query)
			if(q!=null)
			{
				boolean inc = false;
				for(ItemStack key : queryAmount.keySet())
					if(OreDictionary.itemMatches(q, key, true))
					{
						queryAmount.put(key, queryAmount.get(key)+q.stackSize);
						inc = true;
					}
				if(!inc)
					queryAmount.put(q, q.stackSize);
			}

		int maxCrafted = 0;
		ArrayList<Object> formattedInputList = getFormattedInputs();
		Iterator formInputIt = formattedInputList.iterator();
		while(formInputIt.hasNext())
		{
			Object o = formInputIt.next();
			int supplied = 0;
			int req = o instanceof ItemStack?((ItemStack)o).stackSize: o instanceof ArrayList?((ItemStack)((ArrayList)o).get(0)).stackSize: 0;
			Iterator<Entry<ItemStack, Integer>> queryIt = queryAmount.entrySet().iterator();
			while(queryIt.hasNext())
			{
				Entry<ItemStack, Integer> e = queryIt.next();
				ItemStack compStack = e.getKey();
				if(ApiUtils.stackMatchesObject(compStack, o))
				{
					int taken = e.getValue()/req;
					if(taken>0)
					{
						e.setValue(e.getValue()-taken*req);
						if(e.getValue()<=0)
							queryIt.remove();
						supplied += taken;
					}
				}
			}
			if(supplied<=0)
				return 0;
			else
				maxCrafted = maxCrafted==0?supplied:Math.min(maxCrafted, supplied);
		}
		return maxCrafted;
	}

	public void consumeInputs(ItemStack[] query, int crafted)
	{
		ArrayList<Object> inputList = new ArrayList();
		for(Object i : inputs)
			if(i!=null)
				inputList.add(i instanceof ItemStack? ((ItemStack)i).copy(): i);

		Iterator inputIt = inputList.iterator();
		while(inputIt.hasNext())
		{
			Object o = inputIt.next();
			int inputSize = (o instanceof ItemStack?((ItemStack)o).stackSize:1)*crafted;

			for(int i=0; i<query.length; i++)
				if(query[i]!=null)
					if(ApiUtils.stackMatchesObject(query[i], o))
					{
						int taken = Math.min(query[i].stackSize, inputSize);
						query[i].stackSize-=taken;
						if(query[i].stackSize<=0)
							query[i] = null;
						inputSize-=taken;
						if(inputSize<=0)
						{
							inputIt.remove();
							break;
						}
					}
		}
	}
	public ArrayList<Object> getFormattedInputs()
	{
		LinkedHashMap<Object, Integer> sumMap = new LinkedHashMap<Object, Integer>();
		for(Object o : this.inputs)
			if(o!=null)
			{
				boolean isNew = true;
				for(Object ss : sumMap.keySet())
					if(ss instanceof ItemStack && o instanceof ItemStack)
					{	
						if(OreDictionary.itemMatches((ItemStack)ss, (ItemStack)o, true))
						{
							isNew = false;
							sumMap.put(ss, sumMap.get(ss)+((ItemStack)o).stackSize);
						}
					}
					else if(ss.equals(o))
					{
						isNew = false;
						sumMap.put(ss, sumMap.get(ss)+1);
					}
				if(isNew)
					sumMap.put(o, (o instanceof ItemStack)?((ItemStack)o).stackSize: 1);
			}
		ArrayList<Object> formattedInputs = new ArrayList<Object>();  
		for(Map.Entry<Object,Integer> e : sumMap.entrySet())
		{
			Object ss = e.getKey();
			if(ss instanceof ItemStack)
				formattedInputs.add(Utils.copyStackWithAmount((ItemStack)ss, e.getValue()));
			else if(ss instanceof ArrayList)
			{
				ArrayList<ItemStack> oreListCopy = new ArrayList<ItemStack>();
				for(ItemStack oreStack : (ArrayList<ItemStack>)ss)
					oreListCopy.add(Utils.copyStackWithAmount(oreStack, e.getValue()));
				formattedInputs.add(oreListCopy);
			}
		}
		return formattedInputs;
	}

	public static void addRecipe(String blueprintCategory, ItemStack output, Object... inputs)
	{
		recipeList.put(blueprintCategory, new BlueprintCraftingRecipe(output, inputs));
		if(!blueprintCategories.contains(blueprintCategory))
			blueprintCategories.add(blueprintCategory);
	}
	public static BlueprintCraftingRecipe[] findRecipes(String blueprintCategory)
	{
		if(recipeList.containsKey(blueprintCategory))
		{
			List<BlueprintCraftingRecipe> list = recipeList.get(blueprintCategory);
			return list.toArray(new BlueprintCraftingRecipe[list.size()]);
		}
		return new BlueprintCraftingRecipe[0];
	}

	/**
	 * registers a type of blueprint to be up for sale at the IE villager. Stacksize of the price will be slightly randomized (+- 2)
	 */
	public static void addVillagerTrade(String blueprintCategory, ItemStack villagerPrice)
	{
		villagerPrices.put(blueprintCategory, villagerPrice);
	}
}