/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import com.google.common.collect.Lists;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.RegistryObject;

import java.util.*;

/**
 * @author BluSunrize - 20.02.2016
 * <p>
 * The recipe for the Squeezer
 */
public class MixerRecipe extends MultiblockRecipe
{
	public static RecipeType<MixerRecipe> TYPE;
	public static RegistryObject<IERecipeSerializer<MixerRecipe>> SERIALIZER;

	public final IngredientWithSize[] itemInputs;
	public final FluidTagInput fluidInput;
	public final FluidStack fluidOutput;
	public final int fluidAmount;

	public MixerRecipe(ResourceLocation id, FluidStack fluidOutput, FluidTagInput fluidInput, IngredientWithSize[] itemInputs, int energy)
	{
		super(ItemStack.EMPTY, TYPE, id);
		this.fluidOutput = fluidOutput;
		this.fluidAmount = fluidOutput.getAmount();
		this.fluidInput = fluidInput;
		this.itemInputs = itemInputs;
		setTimeAndEnergy(fluidOutput.getAmount(), energy);

		this.fluidInputList = Lists.newArrayList(this.fluidInput);
		setInputListWithSizes(Lists.newArrayList(this.itemInputs));
		this.fluidOutputList = Lists.newArrayList(this.fluidOutput);
	}

	@Override
	protected IERecipeSerializer<MixerRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	// Initialized by reload listener
	public static Map<ResourceLocation, MixerRecipe> recipeList = Collections.emptyMap();

	public static MixerRecipe findRecipe(FluidStack fluid, NonNullList<ItemStack> components)
	{
		if(fluid==null)
			return null;
		for(MixerRecipe recipe : recipeList.values())
			if(recipe.matches(fluid, components))
				return recipe;
		return null;
	}

	public FluidStack getFluidOutput(FluidStack input, NonNullList<ItemStack> components)
	{
		return this.fluidOutput;
	}

	public boolean matches(FluidStack fluid, NonNullList<ItemStack> components)
	{
		return compareToInputs(fluid, components, this.fluidInput, this.itemInputs);
	}

	protected boolean compareToInputs(FluidStack fluid, NonNullList<ItemStack> components, FluidTagInput fluidInput,
									  IngredientWithSize[] itemInputs)
	{
		if(fluid!=null&&fluidInput.test(fluid))
		{
			ArrayList<ItemStack> queryList = new ArrayList<>(components.size());
			for(ItemStack s : components)
				if(!s.isEmpty())
					queryList.add(s.copy());

			for(IngredientWithSize add : itemInputs)
				if(add!=null)
				{
					int addAmount = add.getCount();
					Iterator<ItemStack> it = queryList.iterator();
					while(it.hasNext())
					{
						ItemStack query = it.next();
						if(!query.isEmpty())
						{
							if(add.test(query))
								if(query.getCount() > addAmount)
								{
									query.shrink(addAmount);
									addAmount = 0;
								}
								else
								{
									addAmount -= query.getCount();
									query.setCount(0);
								}
							if(query.getCount() <= 0)
								it.remove();
							if(addAmount <= 0)
								break;
						}
					}
					if(addAmount > 0)
						return false;
				}
			return true;
		}
		return false;
	}


	public int[] getUsedSlots(FluidStack input, NonNullList<ItemStack> components)
	{
		Set<Integer> usedSlotSet = new HashSet<>();
		for(IngredientWithSize ingr : itemInputs)
		{
			for(int j = 0; j < components.size(); j++)
				if(!usedSlotSet.contains(j)&&!components.get(j).isEmpty()&&ingr.test(components.get(j)))
				{
					usedSlotSet.add(j);
					break;
				}
		}
		int it = 0;
		int[] processSlots = new int[usedSlotSet.size()];
		for(Integer slot : usedSlotSet)
			processSlots[it++] = slot;
		return processSlots;
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 0;
	}

	@Override
	public boolean shouldCheckItemAvailability()
	{
		return false;
	}
}