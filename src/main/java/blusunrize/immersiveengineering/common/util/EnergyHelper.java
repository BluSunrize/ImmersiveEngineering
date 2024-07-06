/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author BluSunrize - 29.11.2016
 */
public class EnergyHelper
{
	public static final String LEGACY_ENERGY_KEY = "ifluxEnergy";
	public static final String ENERGY_KEY = "energy";
	static HashMap<Item, Boolean> reverseInsertion = new HashMap<>();

	public static void deserializeFrom(EnergyStorage storage, CompoundTag mainTag, Provider provider)
	{
		Tag subtag;
		if(mainTag.contains(LEGACY_ENERGY_KEY, Tag.TAG_INT))
			subtag = mainTag.get(LEGACY_ENERGY_KEY);
		else if(mainTag.contains(ENERGY_KEY, Tag.TAG_INT))
			subtag = mainTag.get(ENERGY_KEY);
		else
			subtag = IntTag.valueOf(0);
		storage.deserializeNBT(provider, subtag);
	}

	public static void serializeTo(EnergyStorage storage, CompoundTag mainTag, Provider provider)
	{
		mainTag.put(ENERGY_KEY, storage.serializeNBT(provider));
	}

	public static int forceExtractFlux(ItemStack stack, int energy, boolean simulate)
	{
		IEnergyStorage stackEnergy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
		if(stackEnergy==null)
			return 0;
		Boolean b = reverseInsertion.get(stack.getItem());
		if(b==Boolean.TRUE)
		{
			int stored = stackEnergy.getEnergyStored();
			insertFlux(stack, -energy, simulate);
			return stored-stackEnergy.getEnergyStored();
		}
		else
		{
			int drawn = stackEnergy.extractEnergy(energy, simulate);
			if(b==null)
			{
				int stored = stackEnergy.getEnergyStored();
				insertFlux(stack, -energy, simulate);
				drawn = stored-stackEnergy.getEnergyStored();
				//if reverse insertion was succesful, it'll be the default approach in future
				reverseInsertion.put(stack.getItem(), drawn > 0?Boolean.TRUE: Boolean.FALSE);
			}
			return drawn;
		}
	}

	public static int getEnergyStored(ItemStack stack)
	{
		IEnergyStorage storage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
		return storage!=null?storage.getEnergyStored(): 0;
	}

	public static int getMaxEnergyStored(ItemStack stack)
	{
		IEnergyStorage storage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
		return storage!=null?storage.getMaxEnergyStored(): 0;
	}

	public static boolean isFluxReceiver(ItemStack stack)
	{
		return stack.getCapability(Capabilities.EnergyStorage.ITEM)!=null;
	}

	public static boolean isFluxRelated(ItemStack stack)
	{
		return stack.getCapability(Capabilities.EnergyStorage.ITEM)!=null;
	}

	public static int insertFlux(ItemStack stack, int energy, boolean simulate)
	{
		IEnergyStorage storage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
		if(storage!=null)
			return storage.receiveEnergy(energy, simulate);
		else
			return 0;
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
}
