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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author BluSunrize - 14.01.2016
 * <br>
 * The recipe for the bottling machine
 */
public class BottlingMachineRecipe extends MultiblockRecipe
{
	public static RecipeType<BottlingMachineRecipe> TYPE;
	public static RegistryObject<IERecipeSerializer<BottlingMachineRecipe>> SERIALIZER;
	public static final CachedRecipeList<BottlingMachineRecipe> RECIPES = new CachedRecipeList<>(() -> TYPE, BottlingMachineRecipe.class);

	public final Ingredient input;
	public final FluidTagInput fluidInput;
	public final Lazy<NonNullList<ItemStack>> output;

	public BottlingMachineRecipe(ResourceLocation id, List<Lazy<ItemStack>> output, Ingredient input, FluidTagInput fluidInput)
	{
		super(output.get(0), TYPE, id);
		this.output = Lazy.of(() -> output.stream()
				.map(Lazy::get)
				.collect(Collectors.toCollection(NonNullList::create))
		);
		this.input = input;
		this.fluidInput = fluidInput;

		setInputList(Lists.newArrayList(this.input));
		this.fluidInputList = Lists.newArrayList(this.fluidInput);
		this.outputList = this.output;
	}

	@Override
	protected IERecipeSerializer<BottlingMachineRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	public static BottlingMachineRecipe findRecipe(Level level, ItemStack input, FluidStack fluid)
	{
		if(!input.isEmpty()&&!fluid.isEmpty())
			for(BottlingMachineRecipe recipe : RECIPES.getRecipes(level))
				if(recipe.input.test(input)&&recipe.fluidInput.test(fluid))
					return recipe;
		return null;
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 0;
	}
}