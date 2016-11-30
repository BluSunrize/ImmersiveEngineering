package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
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

	public interface IIEInternalFluxHandler extends IFluxReceiver, IEnergyReceiver, IFluxProvider, IEnergyProvider
	{
		@Nonnull FluxStorage getFluxStorage();
		@Nonnull SideConfig getEnergySideConfig(@Nullable EnumFacing facing);

		@Override
		default boolean canConnectEnergy(EnumFacing fd)
		{
			return getEnergySideConfig(fd) != SideConfig.NONE;
		}

		default void postEnergyTransferUpdate(int energy, boolean simulate)
		{

		}

		@Override
		default int extractEnergy(EnumFacing fd, int amount, boolean simulate)
		{
			if(((TileEntity)this).getWorld().isRemote || getEnergySideConfig(fd) != SideConfig.OUTPUT)
				return 0;
			int r = getFluxStorage().extractEnergy(amount, simulate);
			postEnergyTransferUpdate(-r, simulate);
			return r;
		}

		@Override
		default int getEnergyStored(EnumFacing fd)
		{
			return getFluxStorage().getEnergyStored();
		}

		@Override
		default int getMaxEnergyStored(EnumFacing fd)
		{
			return getFluxStorage().getMaxEnergyStored();
		}

		@Override
		default int receiveEnergy(EnumFacing fd, int amount, boolean simulate)
		{
			if(((TileEntity)this).getWorld().isRemote || getEnergySideConfig(fd) != SideConfig.INPUT)
				return 0;
			int r = getFluxStorage().receiveEnergy(amount, simulate);
			postEnergyTransferUpdate(r, simulate);
			return r;
		}

		IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing);
	}

	public static class IEForgeEnergyWrapper implements IEnergyStorage
	{
		final IIEInternalFluxHandler fluxHandler;
		public final EnumFacing side;

		public IEForgeEnergyWrapper(IIEInternalFluxHandler fluxHandler, EnumFacing side)
		{
			this.fluxHandler = fluxHandler;
			this.side = side;
		}

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate)
		{
			return fluxHandler.receiveEnergy(side, maxReceive, simulate);
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate)
		{
			return fluxHandler.extractEnergy(side, maxExtract, simulate);
		}

		@Override
		public int getEnergyStored()
		{
			return fluxHandler.getEnergyStored(side);
		}

		@Override
		public int getMaxEnergyStored()
		{
			return fluxHandler.getMaxEnergyStored(side);
		}

		@Override
		public boolean canExtract()
		{
			return fluxHandler.getFluxStorage().getLimitExtract() > 0;
		}

		@Override
		public boolean canReceive()
		{
			return fluxHandler.getFluxStorage().getLimitReceive() > 0;
		}

		public static IEForgeEnergyWrapper[] getDefaultWrapperArray(IIEInternalFluxHandler handler)
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
