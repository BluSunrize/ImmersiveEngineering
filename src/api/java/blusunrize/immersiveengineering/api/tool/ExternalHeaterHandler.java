/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import blusunrize.immersiveengineering.mixin.accessors.FurnaceTEAccess;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIntArray;

import java.util.HashMap;
import java.util.Optional;

public class ExternalHeaterHandler
{
	//These are set on IE loading
	public static int defaultFurnaceEnergyCost;
	public static int defaultFurnaceSpeedupCost;

	/**
	 * @author BluSunrize - 09.12.2015
	 * <p>
	 * An interface to be implemented by TileEntities that want to allow direct interaction with the external heater
	 */
	public interface IExternalHeatable
	{
		/**
		 * Called each tick<br>
		 * Handle fueling as well as possible smelting speed increases here
		 *
		 * @param energyAvailable the amount of RF the furnace heater has stored and can supply
		 * @param redstone        whether a redstone signal is applied to the furnace heater. To keep the target warm, but not do speed increases
		 * @return the amount of RF consumed that tick. Should be lower or equal to "energyAvailable", obviously
		 */
		int doHeatTick(int energyAvailable, boolean redstone);
	}

	public static HashMap<Class<? extends TileEntity>, HeatableAdapter> adapterMap = new HashMap<Class<? extends TileEntity>, HeatableAdapter>();

	/**
	 * @author BluSunrize - 09.12.2015
	 * <p>
	 * An adapter to appyl to TileEntities that can't implement the IExternalHeatable interface
	 */
	public abstract static class HeatableAdapter<E extends TileEntity>
	{
		/**
		 * Called each tick<br>
		 * Handle fueling as well as possible smelting speed increases here
		 *
		 * @param energyAvailable the amount of RF the furnace heater has stored and can supply
		 * @param canHeat         whether a redstone signal is applied to the furnace heater. To keep the target warm, but not do speed increases
		 * @return the amount of RF consumed that tick. Should be lower or equal to "energyAvailable", obviously
		 */
		public abstract int doHeatTick(E tileEntity, int energyAvailable, boolean canHeat);
	}

	/**
	 * registers a HeatableAdapter to a TileEnttiy class. Should really only be used when implementing the interface is not an option
	 */
	public static void registerHeatableAdapter(Class<? extends TileEntity> c, HeatableAdapter adapter)
	{
		adapterMap.put(c, adapter);
	}

	/**
	 * @return a HeatableAdapter for the given TileEntity class
	 */
	public static HeatableAdapter getHeatableAdapter(Class<? extends TileEntity> c)
	{
		HeatableAdapter adapter = adapterMap.get(c);
		if(adapter==null&&c!=TileEntity.class&&c.getSuperclass()!=TileEntity.class)
		{
			adapter = getHeatableAdapter((Class<? extends TileEntity>)c.getSuperclass());
			adapterMap.put(c, adapter);
		}
		return adapter;
	}

	public static class DefaultFurnaceAdapter extends HeatableAdapter<FurnaceTileEntity>
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
					int heatEnergyRatio = Math.max(1, defaultFurnaceEnergyCost);
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
					int energyToUse = defaultFurnaceSpeedupCost;
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
}