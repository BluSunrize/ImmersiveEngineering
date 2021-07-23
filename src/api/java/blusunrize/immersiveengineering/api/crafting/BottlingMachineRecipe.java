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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.RegistryObject;

import java.util.Collections;
import java.util.Map;

/**
 * @author BluSunrize - 14.01.2016
 * <br>
 * The recipe for the bottling machine
 */
public class BottlingMachineRecipe extends MultiblockRecipe
{
	public static RecipeType<BottlingMachineRecipe> TYPE;
	public static RegistryObject<IERecipeSerializer<BottlingMachineRecipe>> SERIALIZER;

	public final Ingredient input;
	public final FluidTagInput fluidInput;
	public final ItemStack output;

	public BottlingMachineRecipe(ResourceLocation id, ItemStack output, Ingredient input, FluidTagInput fluidInput)
	{
		super(output, TYPE, id);
		this.output = output;
		this.input = input;
		this.fluidInput = fluidInput;

		setInputList(Lists.newArrayList(this.input));
		this.fluidInputList = Lists.newArrayList(this.fluidInput);
		this.outputList = NonNullList.of(ItemStack.EMPTY, this.output);
	}

	@Override
	protected IERecipeSerializer<BottlingMachineRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	// Initialized by reload listener
	public static Map<ResourceLocation, BottlingMachineRecipe> recipeList = Collections.emptyMap();

	public static BottlingMachineRecipe findRecipe(ItemStack input, FluidStack fluid)
	{
		if(!input.isEmpty()&&!fluid.isEmpty())
			for(BottlingMachineRecipe recipe : recipeList.values())
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