package blusunrize.immersiveengineering.common.blocks.multiblocks.process;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;

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

	default boolean additionalCanProcessCheck(MultiblockProcess<R, ?> process)
	{
		return true;
	}

	default void onProcessFinish(MultiblockProcess<R, ?> process)
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
	IItemHandlerModifiable EMPTY_ITEM_HANDLER = new EmptyHandler();
	IFluidTank[] EMPTY_TANKS = {};
}
