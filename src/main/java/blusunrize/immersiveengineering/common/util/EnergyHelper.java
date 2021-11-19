/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * @author BluSunrize - 29.11.2016
 */
public class EnergyHelper
{

	static HashMap<Item, Boolean> reverseInsertion = new HashMap<>();

	public static int forceExtractFlux(ItemStack stack, int energy, boolean simulate)
	{
		if(stack.isEmpty())
			return 0;
		Boolean b = reverseInsertion.get(stack.getItem());
		if(b==Boolean.TRUE)
		{
			int stored = getEnergyStored(stack);
			insertFlux(stack, -energy, simulate);
			return stored-getEnergyStored(stack);
		}
		else
		{
			int drawn = extractFlux(stack, energy, simulate);
			if(b==null)
			{
				int stored = getEnergyStored(stack);
				insertFlux(stack, -energy, simulate);
				drawn = stored-getEnergyStored(stack);
				//if reverse insertion was succesful, it'll be the default approach in future
				reverseInsertion.put(stack.getItem(), drawn > 0?Boolean.TRUE: Boolean.FALSE);
			}
			return drawn;
		}
	}

	public static int getEnergyStored(ICapabilityProvider stack)
	{
		return getEnergyStored(stack, null);
	}

	public static int getEnergyStored(ICapabilityProvider stack, @Nullable Direction side)
	{
		if(stack==null)
			return 0;
		return stack.getCapability(CapabilityEnergy.ENERGY, side)
				.map(IEnergyStorage::getEnergyStored)
				.orElse(0);
	}

	public static int getMaxEnergyStored(ICapabilityProvider stack)
	{
		return getMaxEnergyStored(stack, null);
	}

	public static int getMaxEnergyStored(ICapabilityProvider stack, @Nullable Direction side)
	{
		if(stack==null)
			return 0;
		return stack.getCapability(CapabilityEnergy.ENERGY, side)
				.map(IEnergyStorage::getMaxEnergyStored)
				.orElse(0);
	}

	public static boolean isFluxReceiver(ICapabilityProvider tile)
	{
		return isFluxReceiver(tile, null);
	}

	public static boolean isFluxReceiver(ICapabilityProvider tile, @Nullable Direction facing)
	{
		if(tile==null)
			return false;
		return tile.getCapability(CapabilityEnergy.ENERGY, facing)
				.map(IEnergyStorage::canReceive)
				.orElse(false);
	}

	public static boolean isFluxRelated(ICapabilityProvider tile)
	{
		return isFluxRelated(tile, null);
	}

	public static boolean isFluxRelated(ICapabilityProvider tile, @Nullable Direction facing)
	{
		if(tile==null)
			return false;
		return tile.getCapability(CapabilityEnergy.ENERGY, facing).isPresent();
	}

	public static int insertFlux(ICapabilityProvider tile, int energy, boolean simulate)
	{
		return insertFlux(tile, null, energy, simulate);
	}

	public static int insertFlux(ICapabilityProvider tile, @Nullable Direction facing, int energy, boolean simulate)
	{
		if(tile==null)
			return 0;
		return tile.getCapability(CapabilityEnergy.ENERGY, facing)
				.map(storage -> storage.receiveEnergy(energy, simulate))
				.orElse(0);
	}

	public static int extractFlux(ICapabilityProvider tile, int energy, boolean simulate)
	{
		return extractFlux(tile, null, energy, simulate);
	}

	public static int extractFlux(ICapabilityProvider tile, @Nullable Direction facing, int energy, boolean simulate)
	{
		if(tile==null)
			return 0;
		return tile.getCapability(CapabilityEnergy.ENERGY, facing)
				.map(storage -> storage.extractEnergy(energy, simulate))
				.orElse(0);
	}

	/**
	 * This method takes a list of IEnergyStorages and a total output amount. It sorts the storage by how much they
	 * accept, distributes the energy evenly between them, starting with the lowest acceptance.
	 * Overflow lands in the storage with the highest acceptance.
	 *
	 * @param storages a collection of outputs
	 * @param amount   the total amount to be distributed
	 * @param simulate true if no energy should be inserted into the outputs
	 * @return the amount of energy remaining after insertion
	 */
	public static int distributeFlux(Collection<IEnergyStorage> storages, int amount, boolean simulate)
	{
		final int finalAmount = amount;
		storages = storages.stream()
				// Remove null storages
				.filter(Objects::nonNull)
				// Map to how much each storage can accept
				.map(storage -> Pair.of(storage, storage.receiveEnergy(finalAmount, true)))
				// Sort ascending by acceptance
				.sorted(Comparator.comparingInt(Pair::getSecond))
				// Unmap them
				.map(Pair::getFirst)
				// Collect
				.collect(Collectors.toList());

		int remainingOutputs = storages.size();
		for(IEnergyStorage storage : storages)
		{
			int possibleOutput = (int)Math.ceil(amount/(float)remainingOutputs);
			int inserted = storage.receiveEnergy(possibleOutput, simulate);
			amount -= inserted;
			remainingOutputs--;
		}
		return amount;
	}

	public static class ItemEnergyStorage implements IEnergyStorage
	{
		private final ItemStack stack;
		private final ToIntFunction<ItemStack> getCapacity;

		public ItemEnergyStorage(ItemStack item, ToIntFunction<ItemStack> getCapacity)
		{
			this.stack = item;
			this.getCapacity = getCapacity;
		}

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate)
		{
			int stored = getEnergyStored();
			int accepted = Math.min(maxReceive, getMaxEnergyStored()-stored);
			if(!simulate)
			{
				stored += accepted;
				ItemNBTHelper.putInt(stack, "energy", stored);
			}
			return accepted;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate)
		{
			int stored = getEnergyStored();
			int extracted = Math.min(maxExtract, stored);
			if(!simulate)
			{
				stored -= extracted;
				ItemNBTHelper.putInt(stack, "energy", stored);
			}
			return extracted;
		}

		@Override
		public int getEnergyStored()
		{
			return ItemNBTHelper.getInt(stack, "energy");
		}

		@Override
		public int getMaxEnergyStored()
		{
			return getCapacity.applyAsInt(stack);
		}

		@Override
		public boolean canExtract()
		{
			return true;
		}

		@Override
		public boolean canReceive()
		{
			return true;
		}
	}
}
