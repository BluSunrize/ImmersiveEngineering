package blusunrize.immersiveengineering.api.crafting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.api.ApiUtils;

/**
 * @author BluSunrize - 23.03.2015
 * <br>
 * The recipe for the arc furnace
 */
public class ArcFurnaceRecipe
{
	public final Object input;
	public final String oreInputString;
	public final ItemStack output;
	public final ItemStack slag;
	public final Object[] additives;
	public final int time;
	public final int energyPerTick;

	public String specialRecipeType;
	public static ArrayList<String> specialRecipeTypes = new ArrayList<String>();

	public ArcFurnaceRecipe(ItemStack output, Object input, ItemStack slag, int time, int energyPerTick, Object... additives)
	{
		this.output=output;
		this.input=input==null?null:ApiUtils.convertToValidRecipeInput(input);
		this.oreInputString = input instanceof String?(String)input: null;
		this.slag=slag;
		this.time=time;
		this.energyPerTick=energyPerTick;
		if(additives==null)
			this.additives=new Object[0];
		else
		{
			this.additives=new Object[additives.length];
			for(int i=0; i<additives.length; i++)
				this.additives[i]=ApiUtils.convertToValidRecipeInput(additives[i]);
		}
	}

	public ItemStack[] getOutputs(ItemStack input, ItemStack[] additives)
	{
		return new ItemStack[]{output}; 
	}
	public boolean matches(ItemStack input, ItemStack[] additives)
	{
		if(ApiUtils.stackMatchesObject(input, this.input))
		{
			ArrayList<ItemStack> qAdd = new ArrayList<ItemStack>();
			for(ItemStack s : additives)
				qAdd.add(s);

			for(Object add : this.additives)
				if(add!=null)
				{
					int addAmount = add instanceof ItemStack?((ItemStack)add).stackSize:1;
					Iterator<ItemStack> it = qAdd.iterator();
					while(it.hasNext())
					{
						ItemStack query = it.next();
						if(query!=null)
						{
							if(ApiUtils.stackMatchesObject(query, add))
							{
								if(query.stackSize > addAmount)
								{
									query.stackSize-=addAmount;
									addAmount=0;
								}
								else
								{
									addAmount-=query.stackSize;
									query.stackSize=0;
								}
							}
							if(query.stackSize<=0)
								it.remove();
							if(addAmount<=0)
								break;
						}
					}

					if(addAmount>0)
						return false;
				}
			return true;
		}
		return false;
	}
	public boolean isValidInput(ItemStack stack)
	{
		return ApiUtils.stackMatchesObject(stack, this.input);
	}
	public boolean isValidAdditive(ItemStack stack)
	{
		for(Object add : additives)
			if(ApiUtils.stackMatchesObject(stack, add))
				return true;
		return false;
	}
	public ArcFurnaceRecipe setSpecialRecipeType(String type)
	{
		this.specialRecipeType = type;
		if(!specialRecipeTypes.contains(type))
			specialRecipeTypes.add(type);
		return this;
	}

	public static ArrayList<ArcFurnaceRecipe> recipeList = new ArrayList<ArcFurnaceRecipe>();
	public static ArcFurnaceRecipe addRecipe(ItemStack output, Object input, ItemStack slag, int time, int energyPerTick, Object... additives)
	{
		ArcFurnaceRecipe recipe = new ArcFurnaceRecipe(output, input, slag, time, energyPerTick, additives);
		if(recipe.input!=null)
			recipeList.add(recipe);
		return recipe;
	}
	public static ArcFurnaceRecipe findRecipe(ItemStack input, ItemStack[] additives)
	{
		for(ArcFurnaceRecipe recipe : recipeList)
			if(recipe!=null && recipe.matches(input, additives))
				return recipe;
		return null;
	}
	public static List<ArcFurnaceRecipe> removeRecipes(ItemStack stack)
	{
		List<ArcFurnaceRecipe> list = new ArrayList();
		Iterator<ArcFurnaceRecipe> it = recipeList.iterator();
		while(it.hasNext())
		{
			ArcFurnaceRecipe ir = it.next();
			if(OreDictionary.itemMatches(ir.output, stack, true))
			{
				list.add(ir);
				it.remove();
			}
		}
		return list;
	}

	public static boolean isValidRecipeInput(ItemStack stack)
	{
		for(ArcFurnaceRecipe recipe : recipeList)
			if(recipe!=null && recipe.isValidInput(stack))
				return true;
		return false;
	}
	public static boolean isValidRecipeAdditive(ItemStack stack)
	{
		for(ArcFurnaceRecipe recipe : recipeList)
			if(recipe!=null && recipe.isValidAdditive(stack))
				return true;
		return false;
	}
	
	public static ArrayList recyclingAllowed = new ArrayList();
	/**
	 * Set an item/oredict-entry to be considered for recycling in the arc furnace. Tools and Armor should usually be auto-detected
	 */
	public static void allowItemForRecycling(Object stack)
	{
		recyclingAllowed.add(ApiUtils.convertToValidRecipeInput(stack));
	}
	
	public static ArrayList invalidRecyclingOutput = new ArrayList();
	/**
	 * Set an item/oredict-entry to be an invalid output for the recycling process. Used for magical ingots that should be reclaimable or similar
	 */
	public static void makeItemInvalidRecyclingOutput(Object stack)
	{
		invalidRecyclingOutput.add(ApiUtils.convertToValidRecipeInput(stack));
	}
}