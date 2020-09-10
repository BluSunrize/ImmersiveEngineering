/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.common.IEConfig.Machines.MachineRecipeConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MultiblockRecipe extends IESerializableRecipe implements IMultiblockRecipe, IJEIRecipe
{
	private final MachineRecipeConfig machineConfig;

	protected MultiblockRecipe(ItemStack outputDummy, IRecipeType<?> type, ResourceLocation id, MachineRecipeConfig machineConfig)
	{
		super(outputDummy, type, id);
		this.machineConfig = machineConfig;
	}

	@Override
	public ItemStack getRecipeOutput()
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

	protected NonNullList<ItemStack> outputList = NonNullList.withSize(0, ItemStack.EMPTY);

	@Override
	public NonNullList<ItemStack> getItemOutputs()
	{
		return outputList;
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

	int totalProcessTime;

	@Override
	public int getTotalProcessTime()
	{
		return (int)(this.totalProcessTime*machineConfig.timeModifier.get());
	}

	int totalProcessEnergy;

	@Override
	public int getTotalProcessEnergy()
	{
		return (int)(this.totalProcessEnergy*machineConfig.energyModifier.get());
	}

}