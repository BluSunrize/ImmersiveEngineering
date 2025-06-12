/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import com.google.common.collect.Lists;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author BluSunrize - 14.01.2016
 * <br>
 * The recipe for the bottling machine
 */
public class BottlingMachineRecipe extends MultiblockRecipe
{
	public static DeferredHolder<RecipeSerializer<?>, IERecipeSerializer<BottlingMachineRecipe>> SERIALIZER;
	public static final CachedRecipeList<BottlingMachineRecipe> RECIPES = new CachedRecipeList<>(IERecipeTypes.BOTTLING_MACHINE);
	public static final SetRestrictedField<RecipeMultiplier> MULTIPLIERS = SetRestrictedField.common();

	public final List<IngredientWithSize> inputs;
	public final SizedFluidIngredient fluidInput;
	public final TagOutputList output;

	public BottlingMachineRecipe(TagOutputList output, List<IngredientWithSize> inputs, SizedFluidIngredient fluidInput)
	{
		super(output.getLazyList().get(0), IERecipeTypes.BOTTLING_MACHINE, 60, 480, MULTIPLIERS);
		this.output = output;
		this.inputs = inputs;
		this.fluidInput = fluidInput;

		setInputListWithSizes(Lists.newArrayList(this.inputs));
		this.fluidInputList = Lists.newArrayList(fluidInput);
		this.outputList = this.output;
	}

	public BottlingMachineRecipe(TagOutputList output, IngredientWithSize input, SizedFluidIngredient fluidInput)
	{
		this(output, List.of(input), fluidInput);
	}

	@Override
	protected IERecipeSerializer<BottlingMachineRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	public boolean matches(ItemStack[] input, FluidStack fluid)
	{
		if(!this.fluidInput.test(fluid))
			return false;
		// create a map of available items that can be reduced
		Map<ItemStack, Integer> available = new HashMap<>();
		for(ItemStack in : input)
			available.put(in, in.getCount());
		// verify that all inputs are satisfied
		for(IngredientWithSize ingr : this.inputs)
		{
			int need = ingr.getCount();
			for(ItemStack stack : input)
				if(ingr.test(stack))
				{
					int take = Math.min(need, available.get(stack));
					need -= take;
					available.put(stack, available.get(stack)-need);
				}
			if(need > 0)
				return false;
		}
		return true;
	}

	public NonNullList<ItemStack> getDisplayStacks(ItemStack[] input)
	{
		NonNullList<ItemStack> list = NonNullList.withSize(this.inputs.size(), ItemStack.EMPTY);
		for(int i = 0; i < this.inputs.size(); i++)
			for(ItemStack stack : input)
				if(this.inputs.get(i).test(stack))
				{
					list.set(i, stack.copyWithCount(this.inputs.get(i).getCount()));
					break;
				}
		return list;
	}

	public static RecipeHolder<BottlingMachineRecipe> findRecipe(Level level, FluidStack fluid, ItemStack... input)
	{
		if(fluid.isEmpty())
			return null;
		for(RecipeHolder<BottlingMachineRecipe> recipe : RECIPES.getRecipes(level))
			if(recipe.value().matches(input, fluid))
				return recipe;
		return null;
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 0;
	}
}