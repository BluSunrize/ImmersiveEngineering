/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.process;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.utils.IngredientUtils;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockBlockEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MultiblockProcessInMachine<R extends MultiblockRecipe> extends MultiblockProcess<R>
{
	protected final int[] inputSlots;
	protected int[] inputAmounts = null;
	protected int[] inputTanks = new int[0];

	public MultiblockProcessInMachine(R recipe, int... inputSlots)
	{
		super(recipe);
		this.inputSlots = inputSlots;
	}

	public MultiblockProcessInMachine<R> setInputTanks(int... inputTanks)
	{
		this.inputTanks = inputTanks;
		return this;
	}

	public MultiblockProcessInMachine<R> setInputAmounts(int... inputAmounts)
	{
		this.inputAmounts = inputAmounts;
		return this;
	}

	public int[] getInputSlots()
	{
		return this.inputSlots;
	}

	@Nullable
	public int[] getInputAmounts()
	{
		return this.inputAmounts;
	}

	public int[] getInputTanks()
	{
		return this.inputTanks;
	}

	protected List<IngredientWithSize> getRecipeItemInputs(PoweredMultiblockBlockEntity<?, R> multiblock)
	{
		return recipe.getItemInputs();
	}

	protected List<FluidTagInput> getRecipeFluidInputs(PoweredMultiblockBlockEntity<?, R> multiblock)
	{
		return recipe.getFluidInputs();
	}

	@Override
	public void doProcessTick(PoweredMultiblockBlockEntity<?, R> multiblock)
	{
		NonNullList<ItemStack> inv = multiblock.getInventory();
		if(recipe.shouldCheckItemAvailability()&&recipe.getItemInputs()!=null&&inv!=null)
		{
			NonNullList<ItemStack> query = NonNullList.withSize(inputSlots.length, ItemStack.EMPTY);
			for(int i = 0; i < inputSlots.length; i++)
				if(inputSlots[i] >= 0&&inputSlots[i] < inv.size())
					query.set(i, multiblock.getInventory().get(inputSlots[i]));
			if(!IngredientUtils.stacksMatchIngredientWithSizeList(recipe.getItemInputs(), query))
			{
				this.clearProcess = true;
				return;
			}
		}
		super.doProcessTick(multiblock);
	}

	@Override
	protected void processFinish(PoweredMultiblockBlockEntity<?, R> multiblock)
	{
		super.processFinish(multiblock);
		NonNullList<ItemStack> inv = multiblock.getInventory();
		List<IngredientWithSize> itemInputList = this.getRecipeItemInputs(multiblock);
		if(inv!=null&&this.inputSlots!=null&&itemInputList!=null)
		{
			if(this.inputAmounts!=null&&this.inputSlots.length==this.inputAmounts.length)
			{
				for(int i = 0; i < this.inputSlots.length; i++)
					if(this.inputAmounts[i] > 0)
						inv.get(this.inputSlots[i]).shrink(this.inputAmounts[i]);

			}
			else
				for(IngredientWithSize ingr : new ArrayList<>(itemInputList))
				{
					int ingrSize = ingr.getCount();
					for(int slot : this.inputSlots)
						if(!inv.get(slot).isEmpty()&&ingr.test(inv.get(slot)))
						{
							int taken = Math.min(inv.get(slot).getCount(), ingrSize);
							inv.get(slot).shrink(taken);
							if(inv.get(slot).getCount() <= 0)
								inv.set(slot, ItemStack.EMPTY);
							if((ingrSize -= taken) <= 0)
								break;
						}
				}
		}
		IFluidTank[] tanks = multiblock.getInternalTanks();
		List<FluidTagInput> fluidInputList = this.getRecipeFluidInputs(multiblock);
		if(tanks!=null&&this.inputTanks!=null&&fluidInputList!=null)
		{
			for(FluidTagInput ingr : new ArrayList<>(fluidInputList))
			{
				int ingrSize = ingr.getAmount();
				for(int tank : this.inputTanks)
					if(tanks[tank]!=null&&ingr.testIgnoringAmount(tanks[tank].getFluid()))
					{
						int taken = Math.min(tanks[tank].getFluidAmount(), ingrSize);
						tanks[tank].drain(taken, FluidAction.EXECUTE);
						if((ingrSize -= taken) <= 0)
							break;
					}
			}
		}
	}

	public static <R extends MultiblockRecipe>
	MultiblockProcessInMachine<R> load(R recipe, CompoundTag data)
	{
		return new MultiblockProcessInMachine<>(recipe, data.getIntArray("process_inputSlots"))
				.setInputAmounts(data.getIntArray("process_inputAmounts"))
				.setInputTanks(data.getIntArray("process_inputTanks"));
	}

	@Override
	public void writeExtraDataToNBT(CompoundTag nbt)
	{
		if(inputSlots!=null)
			nbt.putIntArray("process_inputSlots", inputSlots);
		if(inputAmounts!=null)
			nbt.putIntArray("process_inputAmounts", inputAmounts);
		if(inputTanks!=null)
			nbt.putIntArray("process_inputTanks", inputTanks);
	}
}
