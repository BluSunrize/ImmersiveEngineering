/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.Lib;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;

/**
 * @author BluSunrize - 23.03.2015
 * <br>
 * The recipe for the arc furnace
 */
public class ArcFurnaceRecipe extends MultiblockRecipe
{
	public static IRecipeType<ArcFurnaceRecipe> TYPE = IRecipeType.register(Lib.MODID+":arc_furnace");
	public static RegistryObject<IERecipeSerializer<ArcFurnaceRecipe>> SERIALIZER;

	public static float energyModifier = 1;
	public static float timeModifier = 1;

	public final IngredientWithSize input;
	public final IngredientWithSize[] additives;
	public final NonNullList<ItemStack> output;
	@Nonnull
	public final ItemStack slag;

	public String specialRecipeType;
	public static List<String> specialRecipeTypes = new ArrayList<>();
	// Initialized by reload listener
	public static Map<ResourceLocation, ArcFurnaceRecipe> recipeList;

	public ArcFurnaceRecipe(ResourceLocation id, NonNullList<ItemStack> output, IngredientWithSize input, @Nonnull ItemStack slag, int time,
							int energy, IngredientWithSize... additives)
	{
		super(output.get(0), TYPE, id);
		this.output = output;
		this.input = input;
		this.slag = slag;
		this.totalProcessTime = (int)Math.floor(time*timeModifier);
		this.totalProcessEnergy = (int)Math.floor(energy*energyModifier);
		this.additives = additives;

		List<IngredientWithSize> inputList = Lists.newArrayList(this.input);
		if(this.additives.length > 0)
			inputList.addAll(Lists.newArrayList(this.additives));
		setInputListWithSizes(inputList);
		this.outputList = this.output;
	}

	@Override
	protected IERecipeSerializer<ArcFurnaceRecipe> getIESerializer()
	{
		return SERIALIZER.get();
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

	public NonNullList<ItemStack> getOutputs(ItemStack input, NonNullList<ItemStack> additives)
	{
		return this.output;
	}

	public boolean matches(ItemStack input, NonNullList<ItemStack> additives)
	{
		if(this.input!=null&&this.input.test(input))
		{
			int[] consumed = getConsumedAdditives(additives, false);
			return consumed!=null;
		}

		return false;
	}

	public int[] getConsumedAdditives(NonNullList<ItemStack> additives, boolean consume)
	{
		int[] consumed = new int[additives.size()];
		for(IngredientWithSize add : this.additives)
			if(add!=null)
			{
				int addAmount = add.getCount();
				Iterator<ItemStack> it = additives.iterator();
				int i = 0;
				while(it.hasNext())
				{
					ItemStack query = it.next();
					if(!query.isEmpty())
					{
						if(add.test(query))
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
		return this.input!=null&&this.input.test(stack);
	}

	public boolean isValidAdditive(ItemStack stack)
	{
		for(IngredientWithSize add : additives)
			if(add!=null&&add.test(stack))
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

	public static ArcFurnaceRecipe findRecipe(ItemStack input, NonNullList<ItemStack> additives)
	{
		for(ArcFurnaceRecipe recipe : recipeList.values())
			if(recipe!=null&&recipe.matches(input, additives))
				return recipe;
		return null;
	}

	public static boolean isValidRecipeInput(ItemStack stack)
	{
		for(ArcFurnaceRecipe recipe : recipeList.values())
			if(recipe!=null&&recipe.isValidInput(stack))
				return true;
		return false;
	}

	public static boolean isValidRecipeAdditive(ItemStack stack)
	{
		for(ArcFurnaceRecipe recipe : recipeList.values())
			if(recipe!=null&&recipe.isValidAdditive(stack))
				return true;
		return false;
	}

	private static final Set<IRecipeType<?>> RECYCLING_RECIPE_TYPES = new HashSet<>();
	private static final List<Predicate<ItemStack>> RECYCLING_ALLOWED = new ArrayList<>();
	private static final List<Predicate<ItemStack>> INVALID_RECYCLING_OUTPUTS = new ArrayList<>();

	/**
	 * Add a predicate to the list of predicates determining whether an item may be recycled
	 */
	public static void allowRecipeTypeForRecycling(IRecipeType<?> recipeType)
	{
		RECYCLING_RECIPE_TYPES.add(recipeType);
	}

	/**
	 * Add a predicate to the list of predicates determining whether an item may be recycled
	 */
	public static void allowItemForRecycling(Predicate<ItemStack> predicate)
	{
		RECYCLING_ALLOWED.add(predicate);
	}

	/**
	 * Add a predicate to determine an invalid output for the recycling process.
	 * Used for magical ingots that should not be reclaimable or similar
	 */
	public static void makeItemInvalidRecyclingOutput(Predicate<ItemStack> predicate)
	{
		INVALID_RECYCLING_OUTPUTS.add(predicate);
	}

	/**
	 * @return true if the given ItemStack can be recycled
	 */
	public static boolean canRecycle(ItemStack stack)
	{
		if(stack.isEmpty())
			return false;
		for(Predicate<ItemStack> predicate : RECYCLING_ALLOWED)
			if(predicate.test(stack))
				return true;
		return false;
	}

	/**
	 * @return true if the given ItemStack should not be returned from recycling
	 */
	public static boolean isValidRecyclingOutput(ItemStack stack)
	{
		if(stack.isEmpty())
			return false;
		for(Predicate<ItemStack> predicate : INVALID_RECYCLING_OUTPUTS)
			if(predicate.test(stack))
				return false;
		return true;
	}

	/**
	 * @return a predicate for IRecipes which is used to filter the list of crafting recipes for recycling
	 */
	public static Predicate<IRecipe<?>> assembleRecyclingFilter()
	{
		return iRecipe -> {
			if(!RECYCLING_RECIPE_TYPES.contains(iRecipe.getType()))
				return false;
			return canRecycle(iRecipe.getRecipeOutput());
		};
	}
}