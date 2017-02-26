package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.energy.immersiveflux.*;
import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyContainerItem;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author BluSunrize - 29.11.2016
 */
public class EnergyHelper
{
	public static boolean isFluxItem(ItemStack stack)
	{
		if(stack==null || stack.getItem()==null)
			return false;
		if(stack.getItem() instanceof IFluxContainerItem)
			return true;
		if(stack.getItem() instanceof IEnergyContainerItem)
			return true;
		return stack.hasCapability(CapabilityEnergy.ENERGY, null);
	}
	public static int getEnergyStored(ItemStack stack)
	{
		if(stack==null || stack.getItem()==null)
			return 0;
		if(stack.getItem() instanceof IFluxContainerItem)
			return ((IFluxContainerItem)stack.getItem()).getEnergyStored(stack);
		if(stack.getItem() instanceof IEnergyContainerItem)
			return ((IEnergyContainerItem)stack.getItem()).getEnergyStored(stack);
		if(stack.hasCapability(CapabilityEnergy.ENERGY, null))
			return stack.getCapability(CapabilityEnergy.ENERGY, null).getEnergyStored();
		return 0;
	}
	public static int getMaxEnergyStored(ItemStack stack)
	{
		if(stack==null || stack.getItem()==null)
			return 0;
		if(stack.getItem() instanceof IFluxContainerItem)
			return ((IFluxContainerItem)stack.getItem()).getMaxEnergyStored(stack);
		if(stack.getItem() instanceof IEnergyContainerItem)
			return ((IEnergyContainerItem)stack.getItem()).getMaxEnergyStored(stack);
		if(stack.hasCapability(CapabilityEnergy.ENERGY, null))
			return stack.getCapability(CapabilityEnergy.ENERGY, null).getMaxEnergyStored();
		return 0;
	}
	public static int insertFlux(ItemStack stack, int energy, boolean simulate)
	{
		if(stack==null || stack.getItem()==null)
			return 0;
		if(stack.getItem() instanceof IFluxContainerItem)
			return ((IFluxContainerItem)stack.getItem()).receiveEnergy(stack, energy, simulate);
		if(stack.getItem() instanceof IEnergyContainerItem)
			return ((IEnergyContainerItem)stack.getItem()).receiveEnergy(stack, energy, simulate);
		if(stack.hasCapability(CapabilityEnergy.ENERGY, null))
			return stack.getCapability(CapabilityEnergy.ENERGY, null).receiveEnergy(energy, simulate);
		return 0;
	}

	public static boolean isFluxReceiver(TileEntity tile, EnumFacing facing)
	{
		if(tile == null)
			return false;
		if(tile instanceof IFluxReceiver && ((IFluxReceiver)tile).canConnectEnergy(facing))
			return true;
		if(tile instanceof IEnergyReceiver && ((IEnergyReceiver)tile).canConnectEnergy(facing))
			return true;
		if(tile.hasCapability(CapabilityEnergy.ENERGY, facing))
			return tile.getCapability(CapabilityEnergy.ENERGY, facing).canReceive();
		return false;
	}

	public static int insertFlux(TileEntity tile, EnumFacing facing, int energy, boolean simulate)
	{
		if(tile == null)
			return 0;
		if(tile instanceof IFluxReceiver && ((IFluxReceiver)tile).canConnectEnergy(facing))
			return ((IFluxReceiver)tile).receiveEnergy(facing, energy, simulate);
		if(tile instanceof IEnergyReceiver && ((IEnergyReceiver)tile).canConnectEnergy(facing))
			return ((IEnergyReceiver)tile).receiveEnergy(facing, energy, simulate);
		if(tile.hasCapability(CapabilityEnergy.ENERGY, facing))
			return tile.getCapability(CapabilityEnergy.ENERGY, facing).receiveEnergy(energy, simulate);
		return 0;
	}

	public interface IIEInternalFluxHandler extends IIEInternalFluxConnector, IFluxReceiver, IEnergyReceiver, IFluxProvider, IEnergyProvider
	{
		@Nonnull FluxStorage getFluxStorage();

		default void postEnergyTransferUpdate(int energy, boolean simulate)
		{

		}

		@Override
		default int extractEnergy(@Nullable EnumFacing fd, int amount, boolean simulate)
		{
			if(((TileEntity)this).getWorld().isRemote || getEnergySideConfig(fd) != SideConfig.OUTPUT)
				return 0;
			int r = getFluxStorage().extractEnergy(amount, simulate);
			postEnergyTransferUpdate(-r, simulate);
			return r;
		}

		@Override
		default int getEnergyStored(@Nullable EnumFacing fd)
		{
			return getFluxStorage().getEnergyStored();
		}

		@Override
		default int getMaxEnergyStored(@Nullable EnumFacing fd)
		{
			return getFluxStorage().getMaxEnergyStored();
		}

		@Override
		default int receiveEnergy(@Nullable EnumFacing fd, int amount, boolean simulate)
		{
			if(((TileEntity)this).getWorld().isRemote || getEnergySideConfig(fd) != SideConfig.INPUT)
				return 0;
			int r = getFluxStorage().receiveEnergy(amount, simulate);
			postEnergyTransferUpdate(r, simulate);
			return r;
		}
	}

	public interface IIEInternalFluxConnector extends IFluxConnection, IEnergyConnection
	{
		@Nonnull SideConfig getEnergySideConfig(@Nullable EnumFacing facing);

		@Override
		default boolean canConnectEnergy(@Nullable EnumFacing fd)
		{
			return getEnergySideConfig(fd) != SideConfig.NONE;
		}

		IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing);
	}

	public static class IEForgeEnergyWrapper implements IEnergyStorage
	{
		final IIEInternalFluxConnector fluxHandler;
		public final EnumFacing side;

		public IEForgeEnergyWrapper(IIEInternalFluxConnector fluxHandler, EnumFacing side)
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
					new IEForgeEnergyWrapper(handler, EnumFacing.DOWN),
					new IEForgeEnergyWrapper(handler, EnumFacing.UP),
					new IEForgeEnergyWrapper(handler, EnumFacing.NORTH),
					new IEForgeEnergyWrapper(handler, EnumFacing.SOUTH),
					new IEForgeEnergyWrapper(handler, EnumFacing.WEST),
					new IEForgeEnergyWrapper(handler, EnumFacing.EAST)
			};
		}
	}
}
