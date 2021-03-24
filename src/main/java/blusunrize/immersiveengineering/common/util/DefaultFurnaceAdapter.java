package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler.HeatableAdapter;
import blusunrize.immersiveengineering.mixin.accessors.FurnaceTEAccess;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIntArray;

import java.util.Optional;

public class DefaultFurnaceAdapter extends HeatableAdapter<FurnaceTileEntity>
{
	boolean canCook(FurnaceTileEntity tileEntity)
	{
		ItemStack input = tileEntity.getStackInSlot(0);
		if(input.isEmpty())
			return false;
		IRecipeType<? extends AbstractCookingRecipe> type = ((FurnaceTEAccess)tileEntity).getRecipeType();
		Optional<? extends AbstractCookingRecipe> output = tileEntity.getWorld().getRecipeManager().getRecipe(type, tileEntity, tileEntity.getWorld());
		if(!output.isPresent())
			return false;
		ItemStack existingOutput = tileEntity.getStackInSlot(2);
		if(existingOutput.isEmpty())
			return true;
		ItemStack outStack = output.get().getRecipeOutput();
		if(!existingOutput.isItemEqual(outStack))
			return false;
		int stackSize = existingOutput.getCount()+outStack.getCount();
		return stackSize <= tileEntity.getInventoryStackLimit()&&stackSize <= outStack.getMaxStackSize();
	}

	@Override
	public int doHeatTick(FurnaceTileEntity tileEntity, int energyAvailable, boolean redstone)
	{
		int energyConsumed = 0;
		boolean canCook = canCook(tileEntity);
		if(canCook||redstone)
		{
			BlockState tileState = tileEntity.getWorld().getBlockState(tileEntity.getPos());
			boolean burning = tileState.get(AbstractFurnaceBlock.LIT);
			IIntArray furnaceData = ((FurnaceTEAccess)tileEntity).getFurnaceData();
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

	public void updateFurnace(TileEntity tileEntity, boolean active)
	{
		BlockState oldState = tileEntity.getWorld().getBlockState(tileEntity.getPos());
		tileEntity.getWorld().setBlockState(tileEntity.getPos(), oldState.with(AbstractFurnaceBlock.LIT, active));
	}
}
