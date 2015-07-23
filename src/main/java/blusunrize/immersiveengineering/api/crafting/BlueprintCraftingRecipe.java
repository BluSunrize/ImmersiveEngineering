package blusunrize.immersiveengineering.api.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import net.minecraft.item.ItemStack;
import blusunrize.immersiveengineering.api.ApiUtils;

import com.google.common.collect.ArrayListMultimap;

/**
 * @author BluSunrize - 21.07.2015
 * <br>
 * These recipes are accessible in the Engineers Workbench, with a Engineers Blueprint item.<br>
 * For every "category" registered, a blueprint item will be added automatically.
 */
public class BlueprintCraftingRecipe
{
	public static LinkedHashSet<String> blueprintCategories = new LinkedHashSet<String>();
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
//			//			System.out.println(" Look for: "+o+", available queries: "+queryList);
//			Iterator<ItemStack> queryIt = queryList.iterator();
//			while(queryIt.hasNext())
//			{
//				ItemStack stack = queryIt.next();
//				//				System.out.println("  check against "+stack);
//				if(ApiUtils.stackMatchesObject(stack, o))
//				{
//					//					System.out.println("  match,");
//					if(o instanceof ItemStack)
//					{
//						int taken = Math.min(stack.stackSize, ((ItemStack)o).stackSize);
//						//						System.out.println("  reducing by "+taken);
//						stack.stackSize-=taken;
//						if(stack.stackSize<=0)
//						{
//							//							System.out.println("  removing from query list");
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
//				//				System.out.println("While checkign the "+this.output+" recipe, "+o+" was missing");
//				return false;
//			}
//		}
//		if(inputList.isEmpty())
//			return true;
		return getMaxCrafted(query)>0;
	}
	public int getMaxCrafted(ItemStack[] query)
	{
		ArrayList<Object> inputList = new ArrayList();
		for(Object i : inputs)
			if(i!=null)
				inputList.add(i instanceof ItemStack? ((ItemStack)i).copy(): i);
		ArrayList<ItemStack> queryList = new ArrayList();
		for(ItemStack q : query)
			if(q!=null)
				queryList.add(q.copy());

		Iterator inputIt = inputList.iterator();
		int maxCrafted = 0;
		while(inputIt.hasNext())
		{
			int supplied = 0;
			Object o = inputIt.next();
			Iterator<ItemStack> queryIt = queryList.iterator();
			while(queryIt.hasNext())
			{
				ItemStack stack = queryIt.next();
				if(ApiUtils.stackMatchesObject(stack, o))
					if(o instanceof ItemStack)
					{
						int taken = stack.stackSize/((ItemStack)o).stackSize;
						if(taken>0)
						{
							stack.stackSize-= taken;
							if(stack.stackSize<=0)
								queryIt.remove();
							supplied += taken;
						}
					}
					else
					{
						int taken = stack.stackSize;
						stack.stackSize-= taken;
						if(stack.stackSize<=0)
							queryIt.remove();
						supplied += taken;
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

	public static void addRecipe(String blueprintCategory, ItemStack output, Object... inputs)
	{
		recipeList.put(blueprintCategory, new BlueprintCraftingRecipe(output, inputs));
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