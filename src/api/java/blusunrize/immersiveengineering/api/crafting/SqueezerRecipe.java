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
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * @author BluSunrize - 20.02.2016
 * <p>
 * The recipe for the Squeezer
 */
public class SqueezerRecipe extends MultiblockRecipe
{
	public static IRecipeType<SqueezerRecipe> TYPE = IRecipeType.register(Lib.MODID+":squeezer");
	public static RegistryObject<IERecipeSerializer<SqueezerRecipe>> SERIALIZER;

	public IngredientWithSize input;
	public final FluidStack fluidOutput;
	@Nonnull
	public final ItemStack itemOutput;

	public SqueezerRecipe(ResourceLocation id, FluidStack fluidOutput, @Nonnull ItemStack itemOutput, IngredientWithSize input, int energy)
	{
		super(itemOutput, TYPE, id);
		this.fluidOutput = fluidOutput;
		this.itemOutput = itemOutput;
		this.input = input;
		setTimeAndEnergy(80, energy);

		setInputListWithSizes(Lists.newArrayList(this.input));
		this.fluidOutputList = Lists.newArrayList(this.fluidOutput);
		this.outputList = NonNullList.from(ItemStack.EMPTY, this.itemOutput);
	}

	@Override
	protected IERecipeSerializer<SqueezerRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	public SqueezerRecipe setInputSize(int size)
	{
		this.input = this.input.withSize(size);
		return this;
	}

	// Initialized by reload listener
	public static Map<ResourceLocation, SqueezerRecipe> recipeList = Collections.emptyMap();

	public static SqueezerRecipe findRecipe(ItemStack input)
	{
		if(input.isEmpty())
			return null;
		for(SqueezerRecipe recipe : recipeList.values())
			if(recipe.input.test(input))
				return recipe;
		return null;
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 0;
	}

	public static SortedMap<ITextComponent, Integer> getFluidValuesSorted(Fluid f, boolean inverse)
	{
		SortedMap<ITextComponent, Integer> map = new TreeMap<>(
				Comparator.comparing(
						ITextComponent::getString,
						inverse?Comparator.reverseOrder(): Comparator.reverseOrder()
				)
		);
		for(SqueezerRecipe recipe : recipeList.values())
			if(recipe.fluidOutput!=null&&recipe.fluidOutput.getFluid()==f&&!recipe.input.hasNoMatchingItems())
			{
				ItemStack is = recipe.input.getMatchingStacks()[0];
				map.put(is.getDisplayName(), recipe.fluidOutput.getAmount());
			}
		return map;
	}
}