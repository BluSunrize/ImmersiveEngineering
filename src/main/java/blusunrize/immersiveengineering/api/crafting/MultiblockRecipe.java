/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class MultiblockRecipe implements IMultiblockRecipe, IJEIRecipe
{
	protected List<IngredientStack> inputList;

	@Override
	public List<IngredientStack> getItemInputs()
	{
		return inputList;
	}

	protected NonNullList<ItemStack> outputList;

	@Override
	public NonNullList<ItemStack> getItemOutputs()
	{
		return outputList;
	}

	protected List<FluidStack> fluidInputList;

	@Override
	public List<FluidStack> getFluidInputs()
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
		return this.totalProcessTime;
	}

	int totalProcessEnergy;

	@Override
	public int getTotalProcessEnergy()
	{
		return this.totalProcessEnergy;
	}


	// =========================
	//		JEI Integration
	// =========================
	public List<ItemStack>[] jeiItemInputList;
	protected List<ItemStack> jeiTotalItemInputList;
	public List<ItemStack>[] jeiItemOutputList;
	protected List<ItemStack> jeiTotalItemOutputList;
	protected List<FluidStack> jeiFluidInputList;
	protected List<FluidStack> jeiFluidOutputList;

	public void setupJEI()
	{
		if(inputList!=null)
		{
			this.jeiItemInputList = new ArrayList[inputList.size()];
			this.jeiTotalItemInputList = new ArrayList();
			for(int i = 0; i < inputList.size(); i++)
			{
				IngredientStack ingr = inputList.get(i);
				ArrayList list = new ArrayList();
				if(ingr.oreName!=null)
					for(ItemStack stack : OreDictionary.getOres(ingr.oreName))
						list.add(ApiUtils.copyStackWithAmount(stack, ingr.inputSize));
				else if(ingr.stackList!=null)
					for(ItemStack stack : ingr.stackList)
						list.add(ApiUtils.copyStackWithAmount(stack, ingr.inputSize));
				else
					list.add(ApiUtils.copyStackWithAmount(ingr.stack, ingr.inputSize));

				this.jeiItemInputList[i] = list;
				this.jeiTotalItemInputList.addAll(list);
			}
		}
		else
			this.jeiTotalItemInputList = Collections.emptyList();
		if(outputList!=null)
		{
			this.jeiItemOutputList = new ArrayList[outputList.size()];
			this.jeiTotalItemOutputList = new ArrayList();
			for(int i = 0; i < outputList.size(); i++)
			{
				ItemStack s = outputList.get(i);
				ArrayList<ItemStack> list = Lists.newArrayList(!s.isEmpty()?s.copy(): ItemStack.EMPTY);
				this.jeiItemOutputList[i] = list;
				this.jeiTotalItemOutputList.addAll(list);
			}
		}
		else
			this.jeiTotalItemOutputList = Collections.emptyList();
		if(fluidInputList!=null)
		{
			this.jeiFluidInputList = new ArrayList();
			for(int i = 0; i < fluidInputList.size(); i++)
			{
				FluidStack fs = fluidInputList.get(i);
				if(fs!=null)
					this.jeiFluidInputList.add(fs.copy());
			}
		}
		else
			this.jeiFluidInputList = Collections.emptyList();
		if(fluidOutputList!=null)
		{
			this.jeiFluidOutputList = new ArrayList();
			for(int i = 0; i < fluidOutputList.size(); i++)
			{
				FluidStack fs = fluidOutputList.get(i);
				if(fs!=null)
					this.jeiFluidOutputList.add(fs.copy());
			}
		}
		else
			this.jeiFluidOutputList = Collections.emptyList();
	}

	@Override
	public List<ItemStack> getJEITotalItemInputs()
	{
		return jeiTotalItemInputList;
	}

	@Override
	public List<ItemStack> getJEITotalItemOutputs()
	{
		return jeiTotalItemOutputList;
	}

	@Override
	public List<FluidStack> getJEITotalFluidInputs()
	{
		return jeiFluidInputList;
	}

	@Override
	public List<FluidStack> getJEITotalFluidOutputs()
	{
		return jeiFluidOutputList;
	}

}