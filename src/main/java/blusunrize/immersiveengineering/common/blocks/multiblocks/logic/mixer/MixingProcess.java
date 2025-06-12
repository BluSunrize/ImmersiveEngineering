/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic.mixer;

import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext.ProcessContextInMachine;
import blusunrize.immersiveengineering.common.util.inventory.MultiFluidTank;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public class MixingProcess extends MultiblockProcessInMachine<MixerRecipe>
{
	private final MultiFluidTank tank;

	public MixingProcess(RecipeHolder<MixerRecipe> recipe, MultiFluidTank tank, int... inputSlots)
	{
		super(recipe, inputSlots);
		this.tank = tank;
	}

	public MixingProcess(
			BiFunction<Level, ResourceLocation, MixerRecipe> getRecipe, CompoundTag data, MultiFluidTank tank
	)
	{
		super(getRecipe, data);
		this.tank = tank;
	}

	@Override
	protected List<FluidStack> getRecipeFluidOutputs(Level level)
	{
		return Collections.emptyList();
	}

	@Override
	protected List<SizedFluidIngredient> getRecipeFluidInputs(ProcessContextInMachine<MixerRecipe> context, Level level)
	{
		return Collections.emptyList();
	}

	@Override
	public void doProcessTick(ProcessContextInMachine<MixerRecipe> context, IMultiblockLevel level)
	{
		LevelDependentData<MixerRecipe> levelData = getLevelData(level.getRawLevel());
		if(levelData.recipe()==null)
		{
			this.clearProcess = true;
			return;
		}
		// store current process tick, then increment
		int processPre = this.processTick;
		super.doProcessTick(context, level);
		// calculate increment step for converting some fluid
		int timerStep = Math.max(levelData.maxTicks()/levelData.recipe().fluidAmount, 1);
		// catch up on elapsed ticks, performing fluid conversion for each step passed
		while(processPre < this.processTick)
		{
			if(processPre%timerStep==0)
			{
				int amount = levelData.recipe().fluidAmount/levelData.maxTicks();
				int leftover = levelData.recipe().fluidAmount%levelData.maxTicks();
				if(leftover > 0)
				{
					double distBetweenExtra = levelData.maxTicks()/(double)leftover;
					if(Math.floor(processTick/distBetweenExtra)!=Math.floor((processTick-1)/distBetweenExtra))
						amount++;
				}
				FluidStack drained = this.tank.drain(levelData.recipe().fluidInput.ingredient(), amount, FluidAction.EXECUTE);
				if(!drained.isEmpty())
				{
					NonNullList<ItemStack> components = NonNullList.withSize(this.inputSlots.length, ItemStack.EMPTY);
					for(int i = 0; i < components.size(); i++)
						components.set(i, context.getInventory().getStackInSlot(this.inputSlots[i]));
					FluidStack output = levelData.recipe().getFluidOutput(drained, components);

					FluidStack fs = output.copyWithAmount(drained.getAmount());
					this.tank.fillRecipe(fs, FluidAction.EXECUTE);
				}
			}
			processPre++;
		}
	}

	@Override
	public boolean canProcess(ProcessContextInMachine<MixerRecipe> context, Level level)
	{
		LevelDependentData<MixerRecipe> levelData = getLevelData(level);
		if(levelData.recipe()==null)
			return true;
		// we don't need to check filling since after draining 1 mB of input fluid there will be space for 1 mB of output fluid
		return context.getEnergy().extractEnergy(levelData.energyPerTick(), true)==levelData.energyPerTick()&&
				!tank.drain(levelData.recipe().fluidInput.ingredient(), 1, FluidAction.SIMULATE).isEmpty();
	}
}
