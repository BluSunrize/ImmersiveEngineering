/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.process;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.utils.IngredientUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext.ProcessContextInMachine;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class MultiblockProcessInMachine<R extends MultiblockRecipe>
		extends MultiblockProcess<R, ProcessContextInMachine<R>>
{
	protected final int[] inputSlots;
	protected int[] inputAmounts = null;
	protected int[] inputTanks = new int[0];

	public MultiblockProcessInMachine(ResourceLocation recipeId, BiFunction<Level, ResourceLocation, R> getRecipe, int... inputSlots)
	{
		super(recipeId, getRecipe);
		this.inputSlots = inputSlots;
	}

	public MultiblockProcessInMachine(RecipeHolder<R> recipe, int... inputSlots)
	{
		super(recipe);
		this.inputSlots = inputSlots;
	}

	public MultiblockProcessInMachine(BiFunction<Level, ResourceLocation, R> getRecipe, CompoundTag data)
	{
		super(getRecipe, data);
		this.inputSlots = data.getIntArray("process_inputSlots");
		setInputAmounts(data.getIntArray("process_inputAmounts"));
		setInputTanks(data.getIntArray("process_inputTanks"));
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

	protected List<IngredientWithSize> getRecipeItemInputs(ProcessContextInMachine<R> context, Level level)
	{
		R recipe = getLevelData(level).recipe();
		return recipe==null?List.of(): recipe.getItemInputs();
	}

	protected List<SizedFluidIngredient> getRecipeFluidInputs(ProcessContextInMachine<R> context, Level level)
	{
		R recipe = getLevelData(level).recipe();
		return recipe==null?List.of(): recipe.getFluidInputs();
	}

	@Override
	protected boolean canOutputItem(ProcessContextInMachine<R> context, ItemStack output)
	{
		int[] outputSlots = context.getOutputSlots();
		for(int iOutputSlot : outputSlots)
		{
			final IItemHandlerModifiable inv = context.getInventory();
			ItemStack s = inv.getStackInSlot(iOutputSlot);
			if(s.isEmpty())
				return true;
			final boolean match = ItemStack.isSameItemSameComponents(s, output);
			if(match&&s.getCount()+output.getCount() <= inv.getSlotLimit(iOutputSlot))
				return true;
		}
		return false;
	}

	@Override
	protected boolean canOutputFluid(ProcessContextInMachine<R> context, FluidStack output)
	{
		IFluidTank[] tanks = context.getInternalTanks();
		int[] outputTanks = context.getOutputTanks();
		for(int iOutputTank : outputTanks)
			if(tanks[iOutputTank].fill(output, FluidAction.SIMULATE)==output.getAmount())
				return true;
		return false;
	}

	@Override
	protected void outputFluid(ProcessContextInMachine<R> context, FluidStack output)
	{
		IFluidTank[] tanks = context.getInternalTanks();
		int[] outputTanks = context.getOutputTanks();
		for(int iOutputTank : outputTanks)
			if(tanks[iOutputTank].fill(output, FluidAction.SIMULATE)==output.getAmount())
			{
				tanks[iOutputTank].fill(output, FluidAction.EXECUTE);
				break;
			}
	}

	@Override
	protected void outputItem(ProcessContextInMachine<R> context, ItemStack output, IMultiblockLevel level)
	{
		int[] outputSlots = context.getOutputSlots();
		for(int iOutputSlot : outputSlots)
		{
			final IItemHandlerModifiable inv = context.getInventory();
			ItemStack s = inv.getStackInSlot(iOutputSlot);
			if(s.isEmpty())
			{
				inv.setStackInSlot(iOutputSlot, output.copy());
				break;
			}
			else if(ItemStack.isSameItemSameComponents(s, output)&&s.getCount()+output.getCount() <= inv.getSlotLimit(iOutputSlot))
			{
				s.grow(output.getCount());
				break;
			}
		}
	}

	@Override
	public void doProcessTick(ProcessContextInMachine<R> context, IMultiblockLevel level)
	{
		R recipe = getLevelData(level.getRawLevel()).recipe();
		if(recipe==null)
			return;
		IItemHandlerModifiable inv = context.getInventory();
		if(recipe.shouldCheckItemAvailability()&&recipe.getItemInputs()!=null&&inv!=null)
		{
			NonNullList<ItemStack> query = NonNullList.withSize(inputSlots.length, ItemStack.EMPTY);
			for(int i = 0; i < inputSlots.length; i++)
				if(inputSlots[i] >= 0&&inputSlots[i] < inv.getSlots())
					query.set(i, context.getInventory().getStackInSlot(inputSlots[i]));
			if(!IngredientUtils.stacksMatchIngredientWithSizeList(recipe.getItemInputs(), query))
			{
				this.clearProcess = true;
				return;
			}
		}
		super.doProcessTick(context, level);
	}

	@Override
	protected void processFinish(ProcessContextInMachine<R> context, IMultiblockLevel level)
	{
		super.processFinish(context, level);
		IItemHandlerModifiable inv = context.getInventory();
		List<IngredientWithSize> itemInputList = this.getRecipeItemInputs(context, level.getRawLevel());
		if(inv!=null&&this.inputSlots!=null&&itemInputList!=null)
		{
			if(this.inputAmounts!=null&&this.inputSlots.length==this.inputAmounts.length)
			{
				for(int i = 0; i < this.inputSlots.length; i++)
					if(this.inputAmounts[i] > 0)
						inv.getStackInSlot(this.inputSlots[i]).shrink(this.inputAmounts[i]);

			}
			else
				for(IngredientWithSize ingr : new ArrayList<>(itemInputList))
				{
					int ingrSize = ingr.getCount();
					for(int slot : this.inputSlots)
						if(!inv.getStackInSlot(slot).isEmpty()&&ingr.test(inv.getStackInSlot(slot)))
						{
							int taken = Math.min(inv.getStackInSlot(slot).getCount(), ingrSize);
							inv.getStackInSlot(slot).shrink(taken);
							if(inv.getStackInSlot(slot).getCount() <= 0)
								inv.setStackInSlot(slot, ItemStack.EMPTY);
							if((ingrSize -= taken) <= 0)
								break;
						}
				}
		}
		IFluidTank[] tanks = context.getInternalTanks();
		List<SizedFluidIngredient> fluidInputList = this.getRecipeFluidInputs(context, level.getRawLevel());
		if(tanks!=null&&this.inputTanks!=null&&fluidInputList!=null)
		{
			for(SizedFluidIngredient ingr : new ArrayList<>(fluidInputList))
			{
				int ingrSize = ingr.amount();
				for(int tank : this.inputTanks)
					if(tanks[tank]!=null&&ingr.ingredient().test(tanks[tank].getFluid()))
					{
						int taken = Math.min(tanks[tank].getFluidAmount(), ingrSize);
						tanks[tank].drain(taken, FluidAction.EXECUTE);
						if((ingrSize -= taken) <= 0)
							break;
					}
			}
		}
	}

	@Override
	public void writeExtraDataToNBT(CompoundTag nbt, Provider provider)
	{
		if(inputSlots!=null)
			nbt.putIntArray("process_inputSlots", inputSlots);
		if(inputAmounts!=null)
			nbt.putIntArray("process_inputAmounts", inputAmounts);
		if(inputTanks!=null)
			nbt.putIntArray("process_inputTanks", inputTanks);
	}
}
