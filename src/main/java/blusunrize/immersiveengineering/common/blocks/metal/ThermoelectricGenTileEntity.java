/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.energy.ThermoelectricHandler;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxConnector;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ThermoelectricGenTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IIEInternalFluxConnector
{
	private int energyOutput = -1;

	public ThermoelectricGenTileEntity()
	{
		super(IETileTypes.THERMOELECTRIC_GEN.get());
	}

	@Override
	public void tick()
	{
		if(!world.isRemote)
		{
			if(world.getGameTime()%1024==((getPos().getX()^getPos().getZ())&1023))
				recalculateEnergyOutput();
			if(this.energyOutput > 0)
				outputEnergy(this.energyOutput);
		}
	}

	public void outputEnergy(int amount)
	{
		for(Direction fd : Direction.VALUES)
		{
			TileEntity te = Utils.getExistingTileEntity(world, getPos().offset(fd));
			amount -= EnergyHelper.insertFlux(te, fd.getOpposite(), amount, false);
		}
	}

	@Override
	public void onNeighborBlockChange(BlockPos pos)
	{
		super.onNeighborBlockChange(pos);
		recalculateEnergyOutput();
	}

	private void recalculateEnergyOutput()
	{
		int energy = 0;
		for(Direction fd : new Direction[]{Direction.DOWN, Direction.NORTH, Direction.WEST})
			if(!world.isAirBlock(getPos().offset(fd))&&!world.isAirBlock(getPos().offset(fd.getOpposite())))
			{
				int temp0 = getTemperature(getPos().offset(fd));
				int temp1 = getTemperature(getPos().offset(fd.getOpposite()));
				if(temp0 > -1&&temp1 > -1)
				{
					int diff = Math.abs(temp0-temp1);
					energy += (int)(Math.sqrt(diff)/2*IEServerConfig.MACHINES.thermoelectric_output.get());
				}
			}
		this.energyOutput = energy==0?-1: energy;
	}

	private int getTemperature(BlockPos pos)
	{
		Fluid f = getFluid(pos);
		if(f!=Fluids.EMPTY)
			return f.getAttributes().getTemperature(world, pos);
		BlockState state = world.getBlockState(pos);
		return ThermoelectricHandler.getTemperature(state.getBlock());
	}

	@Nullable
	Fluid getFluid(BlockPos pos)
	{
		BlockState state = world.getBlockState(pos);
		IFluidState fState = state.getFluidState();
		return fState.getFluid();
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		this.energyOutput = nbt.getInt("enegyOutput");
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.putInt("enegyOutput", this.energyOutput);
	}


	@Nonnull
	@Override
	public IOSideConfig getEnergySideConfig(@Nullable Direction facing)
	{
		return IOSideConfig.OUTPUT;
	}

	@Override
	public boolean canConnectEnergy(Direction from)
	{
		return true;
	}

	IEForgeEnergyWrapper wrapper = new IEForgeEnergyWrapper(this, null);

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing)
	{
		return wrapper;
	}
}