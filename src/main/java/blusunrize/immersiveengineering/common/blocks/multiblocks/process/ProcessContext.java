/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.process;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;

// TODO this is basically a copy of the API in PoweredMultiblockBE, and should be cleaned up at some point
public interface ProcessContext<R extends MultiblockRecipe>
{
	AveragingEnergyStorage getEnergy();

	default IItemHandlerModifiable getInventory()
	{
		return EMPTY_ITEM_HANDLER;
	}

	default IFluidTank[] getInternalTanks()
	{
		return EMPTY_TANKS;
	}

	default boolean additionalCanProcessCheck(MultiblockProcess<R, ?> process, Level level)
	{
		return true;
	}

	default void onProcessFinish(MultiblockProcess<R, ?> process, Level level)
	{
	}

	interface ProcessContextInWorld<R extends MultiblockRecipe> extends ProcessContext<R>
	{
		default void doProcessOutput(ItemStack result, IMultiblockLevel level)
		{
			throw new RuntimeException("Should be implemented in machines that output items!");
		}

		default void doProcessFluidOutput(FluidStack output)
		{
			throw new RuntimeException("Should be implemented in machines that output fluids!");
		}
	}

	interface ProcessContextInMachine<R extends MultiblockRecipe> extends ProcessContext<R>
	{
		default int[] getOutputTanks()
		{
			return EMPTY_INTS;
		}

		default int[] getOutputSlots()
		{
			return EMPTY_INTS;
		}
	}

	int[] EMPTY_INTS = {};
	IItemHandlerModifiable EMPTY_ITEM_HANDLER = new EmptyItemHandler();
	IFluidTank[] EMPTY_TANKS = {};
}
