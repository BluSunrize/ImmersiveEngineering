/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class MultiblockRecipe extends IESerializableRecipe implements IMultiblockRecipe, IJEIRecipe
{
	private final int baseEnergy;
	private final int baseTime;
	private final RecipeMultiplier multipliers;

	protected <T extends Recipe<?>>
	MultiblockRecipe(
			TagOutput outputDummy,
			IERecipeTypes.TypeWithClass<T> type,
			int baseTime,
			int baseEnergy,
			Supplier<RecipeMultiplier> multipliers
	)
	{
		super(outputDummy, type);
		this.baseEnergy = baseEnergy;
		this.baseTime = baseTime;
		this.multipliers = multipliers.get();
	}

	@Override
	public ItemStack getResultItem(Provider access)
	{
		NonNullList<ItemStack> outputs = getItemOutputs();
		if(outputs!=null&&outputs.size() > 0)
			return outputs.get(0);
		return ItemStack.EMPTY;
	}

	private List<IngredientWithSize> inputList = new ArrayList<>(0);

	@Override
	public List<IngredientWithSize> getItemInputs()
	{
		return inputList;
	}

	protected void setInputListWithSizes(List<IngredientWithSize> inputList)
	{
		this.inputList = new ArrayList<>(inputList);
	}

	protected void setInputList(List<Ingredient> inputList)
	{
		this.inputList = inputList.stream()
				.map(IngredientWithSize::new)
				.collect(Collectors.toList());
	}

	protected TagOutputList outputList = TagOutputList.EMPTY;

	@Override
	public NonNullList<ItemStack> getItemOutputs()
	{
		return outputList.get();
	}

	protected List<SizedFluidIngredient> fluidInputList;

	@Override
	public List<SizedFluidIngredient> getFluidInputs()
	{
		return fluidInputList;
	}

	protected List<FluidStack> fluidOutputList;

	@Override
	public List<FluidStack> getFluidOutputs()
	{
		return fluidOutputList;
	}

	@Override
	public int getTotalProcessTime()
	{
		return (int)(Math.max(1, baseTime*this.multipliers.timeModifier.getAsDouble()));
	}

	@Override
	public int getTotalProcessEnergy()
	{
		return (int)(Math.max(1, baseEnergy*this.multipliers.energyModifier.getAsDouble()));
	}

	public int getBaseEnergy()
	{
		return baseEnergy;
	}

	public int getBaseTime()
	{
		return baseTime;
	}

	public record RecipeMultiplier(DoubleSupplier timeModifier, DoubleSupplier energyModifier)
	{
	}
}