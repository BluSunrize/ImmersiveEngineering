/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import com.google.common.collect.Lists;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author BluSunrize - 14.01.2016
 * <br>
 * The recipe for the bottling machine
 */
public class BottlingMachineRecipe extends MultiblockRecipe
{
	public static RegistryObject<IERecipeSerializer<BottlingMachineRecipe>> SERIALIZER;
	public static final CachedRecipeList<BottlingMachineRecipe> RECIPES = new CachedRecipeList<>(IERecipeTypes.BOTTLING_MACHINE);

	public final IngredientWithSize[] inputs;
	public final FluidTagInput fluidInput;
	public final Lazy<NonNullList<ItemStack>> output;

	public BottlingMachineRecipe(ResourceLocation id, List<Lazy<ItemStack>> output, IngredientWithSize[] inputs, FluidTagInput fluidInput)
	{
		super(output.get(0), IERecipeTypes.BOTTLING_MACHINE, id);
		this.output = Lazy.of(() -> output.stream()
				.map(Lazy::get)
				.collect(Collectors.toCollection(NonNullList::create))
		);
		this.inputs = inputs;
		this.fluidInput = fluidInput;
		setTimeAndEnergy(60, 480);

		setInputListWithSizes(Lists.newArrayList(this.inputs));
		this.fluidInputList = Lists.newArrayList(this.fluidInput);
		this.outputList = this.output;
	}

	public BottlingMachineRecipe(ResourceLocation id, List<Lazy<ItemStack>> output, IngredientWithSize input, FluidTagInput fluidInput)
	{
		this(id, output, new IngredientWithSize[]{input}, fluidInput);
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
		NonNullList<ItemStack> list = NonNullList.withSize(this.inputs.length, ItemStack.EMPTY);
		for(int i = 0; i < this.inputs.length; i++)
			for(ItemStack stack : input)
				if(this.inputs[i].test(stack))
				{
					list.set(i, ItemHandlerHelper.copyStackWithSize(stack, this.inputs[i].getCount()));
					break;
				}
		return list;
	}

	public static BottlingMachineRecipe findRecipe(Level level, FluidStack fluid, ItemStack... input)
	{
		if(fluid.isEmpty())
			return null;
		for(BottlingMachineRecipe recipe : RECIPES.getRecipes(level))
			if(recipe.matches(input, fluid))
				return recipe;
		return null;
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 0;
	}
}