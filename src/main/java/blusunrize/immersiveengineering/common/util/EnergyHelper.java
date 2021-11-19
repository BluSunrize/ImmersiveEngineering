/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.common.immersiveflux.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

	@Deprecated(forRemoval = true)
	public interface IIEInternalFluxHandler extends IIEInternalFluxConnector, IFluxReceiver, IFluxProvider
	{
		@Nonnull
		FluxStorage getFluxStorage();

		default void postEnergyTransferUpdate(int energy, boolean simulate)
		{

		}

		@Override
		default int extractEnergy(@Nullable Direction fd, int amount, boolean simulate)
		{
			if(((BlockEntity)this).getLevel().isClientSide||getEnergySideConfig(fd)!=IOSideConfig.OUTPUT)
				return 0;
			int r = getFluxStorage().extractEnergy(amount, simulate);
			postEnergyTransferUpdate(-r, simulate);
			return r;
		}

		@Override
		default int getEnergyStored(@Nullable Direction fd)
		{
			return getFluxStorage().getEnergyStored();
		}

		@Override
		default int getMaxEnergyStored(@Nullable Direction fd)
		{
			return getFluxStorage().getMaxEnergyStored();
		}

		@Override
		default int receiveEnergy(@Nullable Direction fd, int amount, boolean simulate)
		{
			if(((BlockEntity)this).getLevel().isClientSide||getEnergySideConfig(fd)!=IOSideConfig.INPUT)
				return 0;
			int r = getFluxStorage().receiveEnergy(amount, simulate);
			postEnergyTransferUpdate(r, simulate);
			return r;
		}
	}

	@Deprecated(forRemoval = true)
	public interface IIEInternalFluxConnector extends IFluxConnection
	{
		@Nonnull
		IOSideConfig getEnergySideConfig(@Nullable Direction facing);

		@Override
		default boolean canConnectEnergy(@Nullable Direction fd)
		{
			return getEnergySideConfig(fd)!=IOSideConfig.NONE;
		}

		@Nullable
		IEForgeEnergyWrapper getCapabilityWrapper(Direction facing);
	}

	@Deprecated(forRemoval = true)
	public static class IEForgeEnergyWrapper implements IEnergyStorage
	{
		final IIEInternalFluxConnector fluxHandler;
		public final Direction side;

		public IEForgeEnergyWrapper(IIEInternalFluxConnector fluxHandler, Direction side)
		{
			this.fluxHandler = fluxHandler;
			this.side = side;
		}

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate)
		{
			if(fluxHandler instanceof IIEInternalFluxHandler)
				return ((IIEInternalFluxHandler)fluxHandler).receiveEnergy(side, maxReceive, simulate);
			return 0;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate)
		{
			if(fluxHandler instanceof IIEInternalFluxHandler)
				return ((IIEInternalFluxHandler)fluxHandler).extractEnergy(side, maxExtract, simulate);
			return 0;
		}

		@Override
		public int getEnergyStored()
		{
			if(fluxHandler instanceof IIEInternalFluxHandler)
				return ((IIEInternalFluxHandler)fluxHandler).getEnergyStored(side);
			return 0;
		}

		@Override
		public int getMaxEnergyStored()
		{
			if(fluxHandler instanceof IIEInternalFluxHandler)
				return ((IIEInternalFluxHandler)fluxHandler).getMaxEnergyStored(side);
			return 0;
		}

		@Override
		public boolean canExtract()
		{
			if(fluxHandler instanceof IIEInternalFluxHandler)
				return ((IIEInternalFluxHandler)fluxHandler).getFluxStorage().getLimitExtract() > 0;
			return false;
		}

		@Override
		public boolean canReceive()
		{
			if(fluxHandler instanceof IIEInternalFluxHandler)
				return ((IIEInternalFluxHandler)fluxHandler).getFluxStorage().getLimitReceive() > 0;
			return false;
		}

		public static IEForgeEnergyWrapper[] getDefaultWrapperArray(IIEInternalFluxConnector handler)
		{
			return new IEForgeEnergyWrapper[]{
					new IEForgeEnergyWrapper(handler, Direction.DOWN),
					new IEForgeEnergyWrapper(handler, Direction.UP),
					new IEForgeEnergyWrapper(handler, Direction.NORTH),
					new IEForgeEnergyWrapper(handler, Direction.SOUTH),
					new IEForgeEnergyWrapper(handler, Direction.WEST),
					new IEForgeEnergyWrapper(handler, Direction.EAST)
			};
		}
	}

	@Deprecated(forRemoval = true)
	public interface IIEEnergyItem extends IFluxContainerItem
	{
		@Override
		default int receiveEnergy(ItemStack container, int energy, boolean simulate)
		{
			return ItemNBTHelper.insertFluxItem(container, energy, getMaxEnergyStored(container), simulate);
		}

		@Override
		default int extractEnergy(ItemStack container, int energy, boolean simulate)
		{
			return ItemNBTHelper.extractFluxFromItem(container, energy, simulate);
		}

		@Override
		default int getEnergyStored(ItemStack container)
		{
			return ItemNBTHelper.getFluxStoredInItem(container);
		}
	}

	@Deprecated(forRemoval = true)
	public static class ItemEnergyStorage implements IEnergyStorage
	{
		ItemStack stack;
		IIEEnergyItem ieEnergyItem;

		public ItemEnergyStorage(ItemStack item)
		{
			assert (item.getItem() instanceof IIEEnergyItem);
			this.stack = item;
			this.ieEnergyItem = (IIEEnergyItem)item.getItem();
		}

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate)
		{
			return this.ieEnergyItem.receiveEnergy(stack, maxReceive, simulate);
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate)
		{
			return this.ieEnergyItem.extractEnergy(stack, maxExtract, simulate);
		}

		@Override
		public int getEnergyStored()
		{
			return this.ieEnergyItem.getEnergyStored(stack);
		}

		@Override
		public int getMaxEnergyStored()
		{
			return this.ieEnergyItem.getMaxEnergyStored(stack);
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
