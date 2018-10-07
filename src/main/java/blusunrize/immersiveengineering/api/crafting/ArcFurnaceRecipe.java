/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.util.ListUtils;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author BluSunrize - 23.03.2015
 * <br>
 * The recipe for the arc furnace
 */
public class ArcFurnaceRecipe extends MultiblockRecipe
{
	public static float energyModifier = 1;
	public static float timeModifier = 1;

	public final IngredientStack input;
	public final String oreInputString;
	public final IngredientStack[] additives;
	public final ItemStack output;
	@Nonnull
	public final ItemStack slag;

	public String specialRecipeType;
	public static ArrayList<String> specialRecipeTypes = new ArrayList<String>();
	public static ArrayList<ArcFurnaceRecipe> recipeList = new ArrayList<ArcFurnaceRecipe>();

	public ArcFurnaceRecipe(ItemStack output, Object input, @Nonnull ItemStack slag, int time, int energyPerTick, Object... additives)
	{
		this.output = output;
		this.input = ApiUtils.createIngredientStack(input);
		this.oreInputString = input instanceof String?(String)input: null;
		this.slag = slag;
		this.totalProcessTime = (int)Math.floor(time*timeModifier);
		this.totalProcessEnergy = (int)Math.floor(energyPerTick*energyModifier)*totalProcessTime;
		if(additives==null)
			this.additives = new IngredientStack[0];
		else
		{
			this.additives = new IngredientStack[additives.length];
			for(int i = 0; i < additives.length; i++)
				this.additives[i] = ApiUtils.createIngredientStack(additives[i]);
		}

		this.inputList = Lists.newArrayList(this.input);
		if(this.additives.length > 0)
			this.inputList.addAll(Lists.newArrayList(this.additives));
		this.outputList = ListUtils.fromItem(this.output);
	}

	@Override
	public void setupJEI()
	{
		super.setupJEI();
//		List<ItemStack>[] newJeiItemOutputList = new ArrayList[jeiItemOutputList.length+1];
//		System.arraycopy(jeiItemOutputList,0, newJeiItemOutputList,0, jeiItemOutputList.length);
//		newJeiItemOutputList[jeiItemOutputList.length] = Lists.newArrayList(slag);
//		jeiItemOutputList = newJeiItemOutputList;
		this.jeiTotalItemOutputList.add(slag);
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 0;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		nbt.setTag("input", input.writeToNBT(new NBTTagCompound()));
		if(this.additives.length > 0)
		{
			NBTTagList list = new NBTTagList();
			for(IngredientStack add : this.additives)
				list.appendTag(add.writeToNBT(new NBTTagCompound()));
			nbt.setTag("additives", list);
		}
		return nbt;
	}

	public static ArcFurnaceRecipe loadFromNBT(NBTTagCompound nbt)
	{
		IngredientStack input = IngredientStack.readFromNBT(nbt.getCompoundTag("input"));
		IngredientStack[] additives = null;
		if(nbt.hasKey("additives"))
		{
			NBTTagList list = nbt.getTagList("additives", 10);
			additives = new IngredientStack[list.tagCount()];
			for(int i = 0; i < additives.length; i++)
				additives[i] = IngredientStack.readFromNBT(list.getCompoundTagAt(i));
		}
		for(ArcFurnaceRecipe recipe : recipeList)
			if(recipe.input.equals(input))
			{
				if(additives==null&&recipe.additives.length < 1)
					return recipe;
				else if(additives!=null&&recipe.additives.length==additives.length)
				{
					boolean b = true;
					for(int i = 0; i < additives.length; i++)
						if(!additives[i].equals(recipe.additives[i]))
						{
							b = false;
							break;
						}
					if(b)
						return recipe;
				}
			}
		return null;
	}

	public NonNullList<ItemStack> getOutputs(ItemStack input, NonNullList<ItemStack> additives)
	{
		NonNullList<ItemStack> outputs = NonNullList.create();
		outputs.add(output);
		return outputs;
	}

	public boolean matches(ItemStack input, NonNullList<ItemStack> additives)
	{
		if(this.input!=null&&this.input.matches(input))
		{
			int[] consumed = getConsumedAdditives(additives, false);
			return consumed!=null;
		}

		return false;
	}

	public int[] getConsumedAdditives(NonNullList<ItemStack> additives, boolean consume)
	{
		int[] consumed = new int[additives.size()];
		for(IngredientStack add : this.additives)
			if(add!=null)
			{
				int addAmount = add.inputSize;
				Iterator<ItemStack> it = additives.iterator();
				int i = 0;
				while(it.hasNext())
				{
					ItemStack query = it.next();
					if(!query.isEmpty())
					{
						if(add.matches(query))
						{
							if(query.getCount() > addAmount)
							{
								query.shrink(addAmount);
								consumed[i] = addAmount;
								addAmount = 0;
							}
							else
							{
								addAmount -= query.getCount();
								consumed[i] = query.getCount();
								query.setCount(0);
							}
						}
						if(addAmount <= 0)
							break;
					}
					i++;
				}

				if(addAmount > 0)
				{
					for(int j = 0; j < consumed.length; j++)
						additives.get(j).grow(consumed[j]);
					return null;
				}
			}
		if(!consume)
			for(int j = 0; j < consumed.length; j++)
				additives.get(j).grow(consumed[j]);
		return consumed;
	}


	public boolean isValidInput(ItemStack stack)
	{
		return this.input!=null&&this.input.matches(stack);
	}

	public boolean isValidAdditive(ItemStack stack)
	{
		for(IngredientStack add : additives)
			if(add!=null&&add.matches(stack))
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

	public static ArcFurnaceRecipe addRecipe(ItemStack output, Object input, @Nonnull ItemStack slag, int time, int energyPerTick, Object... additives)
	{
		ArcFurnaceRecipe recipe = new ArcFurnaceRecipe(output, input, slag, time, energyPerTick, additives);
		if(recipe.input!=null)
			recipeList.add(recipe);
		return recipe;
	}

	public static ArcFurnaceRecipe findRecipe(ItemStack input, NonNullList<ItemStack> additives)
	{
		for(ArcFurnaceRecipe recipe : recipeList)
			if(recipe!=null&&recipe.matches(input, additives))
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
			if(recipe!=null&&recipe.isValidInput(stack))
				return true;
		return false;
	}

	public static boolean isValidRecipeAdditive(ItemStack stack)
	{
		for(ArcFurnaceRecipe recipe : recipeList)
			if(recipe!=null&&recipe.isValidAdditive(stack))
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
	 * Set an item/oredict-entry to be an invalid output for the recycling process.
	 * Used for magical ingots that should not be reclaimable or similar
	 */
	public static void makeItemInvalidRecyclingOutput(Object stack)
	{
		invalidRecyclingOutput.add(ApiUtils.convertToValidRecipeInput(stack));
	}
}