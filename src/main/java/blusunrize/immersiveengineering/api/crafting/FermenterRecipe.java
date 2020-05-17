/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.ListUtils;
import com.google.common.collect.Lists;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author BluSunrize - 01.03.2016
 * <p>
 * The recipe for the Fermenter
 */
public class FermenterRecipe extends MultiblockRecipe
{
	public static IRecipeType<FermenterRecipe> TYPE = IRecipeType.register(Lib.MODID+":fermenter");
	public static RegistryObject<IERecipeSerializer<FermenterRecipe>> SERIALIZER;

	public static float energyModifier = 1;
	public static float timeModifier = 1;

	public IngredientWithSize input;
	public final FluidStack fluidOutput;
	@Nonnull
	public final ItemStack itemOutput;

	public FermenterRecipe(ResourceLocation id, FluidStack fluidOutput, @Nonnull ItemStack itemOutput, IngredientWithSize input, int energy)
	{
		super(itemOutput, TYPE, id);
		this.fluidOutput = fluidOutput;
		this.itemOutput = itemOutput;
		this.input = input;
		this.totalProcessEnergy = (int)Math.floor(energy*energyModifier);
		this.totalProcessTime = (int)Math.floor(80*timeModifier);

		setInputListWithSizes(Lists.newArrayList(this.input));
		this.fluidOutputList = Lists.newArrayList(this.fluidOutput);
		this.outputList = ListUtils.fromItem(this.itemOutput);
	}

	@Override
	protected IERecipeSerializer<FermenterRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	public FermenterRecipe setInputSize(int size)
	{
		this.input = this.input.withSize(size);
		return this;
	}

	// Initialized by reload listener
	public static Map<ResourceLocation, FermenterRecipe> recipeList;

	public static FermenterRecipe findRecipe(ItemStack input)
	{
		if(input.isEmpty())
			return null;
		for(FermenterRecipe recipe : recipeList.values())
			if(recipe.input.test(input))
				return recipe;
		return null;
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 0;
	}

	public static SortedMap<String, Integer> getFluidValuesSorted(Fluid f, boolean inverse)
	{
		SortedMap<String, Integer> map = new TreeMap<>(
				inverse?Comparator.<String>reverseOrder(): Comparator.<String>reverseOrder()
		);
		for(FermenterRecipe recipe : recipeList.values())
			if(recipe.fluidOutput!=null&&recipe.fluidOutput.getFluid()==f&&!recipe.input.hasNoMatchingItems())
			{
				ItemStack is = recipe.input.getMatchingStacks()[0];
				map.put(is.getDisplayName().getFormattedText(), recipe.fluidOutput.getAmount());
			}
		return map;
	}
}