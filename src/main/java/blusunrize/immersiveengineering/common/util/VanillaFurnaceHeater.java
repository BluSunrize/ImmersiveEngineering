/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler.IExternalHeatable;
import blusunrize.immersiveengineering.mixin.accessors.FurnaceTEAccess;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

import static net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.*;

public class VanillaFurnaceHeater implements IExternalHeatable
{
	private static final int FULLY_HEATED_LIT_TIME = 200;
	private final FurnaceBlockEntity furnace;
	private long blockedUntilGameTime = 0;

	public VanillaFurnaceHeater(FurnaceBlockEntity furnace)
	{
		this.furnace = furnace;
	}

	boolean canCook()
	{
		ItemStack input = furnace.getItem(DATA_LIT_TIME);
		if(input.isEmpty())
			return false;
		Optional<? extends AbstractCookingRecipe> output = ((FurnaceTEAccess)furnace).getQuickCheck().getRecipeFor(furnace, furnace.getLevel());
		if(output.isEmpty())
			return false;
		ItemStack existingOutput = furnace.getItem(2);
		if(existingOutput.isEmpty())
			return true;
		ItemStack outStack = output.get().getResultItem(furnace.getLevel().registryAccess());
		if(!ItemStack.isSameItem(existingOutput, outStack))
			return false;
		int stackSize = existingOutput.getCount()+outStack.getCount();
		return stackSize <= furnace.getMaxStackSize()&&stackSize <= outStack.getMaxStackSize();
	}

	@Override
	public int doHeatTick(int energyAvailable, boolean redstone)
	{
		long now = furnace.getLevel().getGameTime();
		if(now < blockedUntilGameTime)
			return 0;
		int energyConsumed = 0;
		boolean canCook = canCook();
		if(canCook||redstone)
		{
			ContainerData furnaceData = ((FurnaceTEAccess)furnace).getDataAccess();
			int burnTime = furnaceData.get(DATA_LIT_TIME);
			if(burnTime < FULLY_HEATED_LIT_TIME)
			{
				final int heatEnergyRatio = Math.max(1, ExternalHeaterHandler.defaultFurnaceEnergyCost);
				if(burnTime==0&&energyAvailable < heatEnergyRatio)
				{
					// Turn off completely for one second if furnace goes out due to insufficient power to prevent fast
					// on/off cycling on weak power sources
					blockedUntilGameTime = now+20;
					return 0;
				}
				int heatAttempt = Math.min(4, FULLY_HEATED_LIT_TIME-burnTime);
				int energyToUse = Math.min(energyAvailable, heatAttempt*heatEnergyRatio);
				int heat = energyToUse/heatEnergyRatio;
				if(heat > 0)
				{
					furnaceData.set(DATA_LIT_TIME, burnTime+heat);
					energyConsumed += heat*heatEnergyRatio;
					setFurnaceActive();
				}
			}
			// Speed up once fully charged
			if(canCook&&furnaceData.get(DATA_LIT_TIME) >= FULLY_HEATED_LIT_TIME&&furnaceData.get(DATA_COOKING_PROGRESS) < furnaceData.get(DATA_COOKING_TOTAL_TIME)-1)
			{
				int energyToUse = ExternalHeaterHandler.defaultFurnaceSpeedupCost;
				if(energyAvailable-energyConsumed > energyToUse)
				{
					energyConsumed += energyToUse;
					furnaceData.set(DATA_COOKING_PROGRESS, furnaceData.get(DATA_COOKING_PROGRESS)+1);
				}
			}
		}
		return energyConsumed;
	}

	public void setFurnaceActive()
	{
		BlockState oldState = furnace.getBlockState();
		if(!oldState.getValue(AbstractFurnaceBlock.LIT))
			furnace.getLevel().setBlockAndUpdate(
					furnace.getBlockPos(), oldState.setValue(AbstractFurnaceBlock.LIT, true)
			);
	}
}
