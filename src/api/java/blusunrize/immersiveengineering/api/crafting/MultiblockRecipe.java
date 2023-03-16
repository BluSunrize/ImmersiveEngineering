/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.stream.Collectors;

public abstract class MultiblockRecipe extends IESerializableRecipe implements IMultiblockRecipe, IJEIRecipe
{
	protected <T extends Recipe<?>>
	MultiblockRecipe(Lazy<ItemStack> outputDummy, IERecipeTypes.TypeWithClass<T> type, ResourceLocation id)
	{
		super(outputDummy, type, id);
	}

	@Override
	public ItemStack getResultItem(RegistryAccess access)
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

	protected Lazy<NonNullList<ItemStack>> outputList = Lazy.of(NonNullList::create);

	@Override
	public NonNullList<ItemStack> getItemOutputs()
	{
		return outputList.get();
	}

	protected List<FluidTagInput> fluidInputList;

	@Override
	public List<FluidTagInput> getFluidInputs()
	{
		return fluidInputList;
	}

	protected List<FluidStack> fluidOutputList;

	@Override
	public List<FluidStack> getFluidOutputs()
	{
		return fluidOutputList;
	}

	Lazy<Integer> totalProcessTime;

	@Override
	public int getTotalProcessTime()
	{
		return this.totalProcessTime.get();
	}

	Lazy<Integer> totalProcessEnergy;

	@Override
	public int getTotalProcessEnergy()
	{
		return this.totalProcessEnergy.get();
	}

	void setTimeAndEnergy(int time, int energy)
	{
		totalProcessEnergy = Lazy.of(() -> energy);
		totalProcessTime = Lazy.of(() -> time);
	}

	public void modifyTimeAndEnergy(DoubleSupplier timeModifier, DoubleSupplier energyModifier)
	{
		final Lazy<Integer> oldTime = totalProcessTime;
		final Lazy<Integer> oldEnergy = totalProcessEnergy;
		this.totalProcessTime = Lazy.of(() -> (int)(Math.max(1, oldTime.get()*timeModifier.getAsDouble())));
		this.totalProcessEnergy = Lazy.of(() -> (int)(Math.max(1, oldEnergy.get()*energyModifier.getAsDouble())));
	}
}