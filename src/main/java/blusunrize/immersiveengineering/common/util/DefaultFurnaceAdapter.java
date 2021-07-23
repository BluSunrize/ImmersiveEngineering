package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler.HeatableAdapter;
import blusunrize.immersiveengineering.mixin.accessors.FurnaceTEAccess;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class DefaultFurnaceAdapter extends HeatableAdapter<FurnaceBlockEntity>
{
	boolean canCook(FurnaceBlockEntity tileEntity)
	{
		ItemStack input = tileEntity.getItem(0);
		if(input.isEmpty())
			return false;
		RecipeType<? extends AbstractCookingRecipe> type = ((FurnaceTEAccess)tileEntity).getRecipeType();
		Optional<? extends AbstractCookingRecipe> output = tileEntity.getLevel().getRecipeManager().getRecipeFor(type, tileEntity, tileEntity.getLevel());
		if(!output.isPresent())
			return false;
		ItemStack existingOutput = tileEntity.getItem(2);
		if(existingOutput.isEmpty())
			return true;
		ItemStack outStack = output.get().getResultItem();
		if(!existingOutput.sameItem(outStack))
			return false;
		int stackSize = existingOutput.getCount()+outStack.getCount();
		return stackSize <= tileEntity.getMaxStackSize()&&stackSize <= outStack.getMaxStackSize();
	}

	@Override
	public int doHeatTick(FurnaceBlockEntity tileEntity, int energyAvailable, boolean redstone)
	{
		int energyConsumed = 0;
		boolean canCook = canCook(tileEntity);
		if(canCook||redstone)
		{
			BlockState tileState = tileEntity.getLevel().getBlockState(tileEntity.getBlockPos());
			boolean burning = tileState.getValue(AbstractFurnaceBlock.LIT);
			ContainerData furnaceData = ((FurnaceTEAccess)tileEntity).getDataAccess();
			int burnTime = furnaceData.get(0);
			if(burnTime < 200)
			{
				int heatAttempt = 4;
				heatAttempt = Math.min(heatAttempt, 200-burnTime);
				int heatEnergyRatio = Math.max(1, ExternalHeaterHandler.defaultFurnaceEnergyCost);
				int energyToUse = Math.min(energyAvailable, heatAttempt*heatEnergyRatio);
				int heat = energyToUse/heatEnergyRatio;
				if(heat > 0)
				{
					furnaceData.set(0, burnTime+heat);
					energyConsumed += heat*heatEnergyRatio;
					if(!burning)
						updateFurnace(tileEntity, furnaceData.get(0) > 0);
				}
			}
			if(canCook&&furnaceData.get(0) >= 200&&furnaceData.get(2) < 199)
			{
				int energyToUse = ExternalHeaterHandler.defaultFurnaceSpeedupCost;
				if(energyAvailable-energyConsumed > energyToUse)
				{
					energyConsumed += energyToUse;
					furnaceData.set(2, furnaceData.get(2)+1);
				}
			}
		}
		return energyConsumed;
	}

	public void updateFurnace(BlockEntity tileEntity, boolean active)
	{
		BlockState oldState = tileEntity.getLevel().getBlockState(tileEntity.getBlockPos());
		tileEntity.getLevel().setBlockAndUpdate(tileEntity.getBlockPos(), oldState.setValue(AbstractFurnaceBlock.LIT, active));
	}
}
