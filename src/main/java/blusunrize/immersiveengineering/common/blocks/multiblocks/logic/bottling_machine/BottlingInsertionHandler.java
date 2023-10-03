/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic.bottling_machine;


import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.bottling_machine.BottlingMachineLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInWorld;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor.InWorldProcessor;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class BottlingInsertionHandler implements IItemHandler
{
	private final Supplier<Level> level;
	private final InWorldProcessor<BottlingMachineRecipe> processor;
	private final State state;

	public BottlingInsertionHandler(
			Supplier<Level> level, InWorldProcessor<BottlingMachineRecipe> processor, State state
	)
	{
		this.level = level;
		this.processor = processor;
		this.state = state;
	}

	@Override
	public int getSlots()
	{
		return 1;
	}

	@Nonnull
	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return ItemStack.EMPTY;
	}

	@Nonnull
	@Override
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
	{
		stack = stack.copy();
		BottlingMachineRecipe recipe = BottlingMachineRecipe.findRecipe(level.get(), state.tank.getFluid(), stack);
		if(recipe==null&&!Utils.isFluidRelatedItemStack(stack))
			return stack;

		MultiblockProcessInWorld<BottlingMachineRecipe> process;
		int inputAmount = 1;
		if(recipe==null)
			process = new BottlingProcess(stack.copy(), stack.copy(), state);
		else
		{
			ItemStack displayStack = recipe.getDisplayStack(stack);
			process = new BottlingProcess(recipe, Utils.createNonNullItemStackListFromItemStack(displayStack), state);
			inputAmount = displayStack.getCount();
		}

		if(processor.addProcessToQueue(process, level.get(), simulate))
			stack.shrink(inputAmount);
		return stack;
	}

	@Nonnull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack)
	{
		return true;//TODO
	}
}
