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
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IETileTypes;
import blusunrize.immersiveengineering.common.temp.IETickableBlockEntity;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxConnector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

public class ThermoelectricGenTileEntity extends IEBaseTileEntity implements IETickableBlockEntity, IIEInternalFluxConnector
{
	private int energyOutput = -1;
	private final Map<Direction, CapabilityReference<IEnergyStorage>> energyWrappers = new EnumMap<>(Direction.class);

	public ThermoelectricGenTileEntity(BlockPos pos, BlockState state)
	{
		super(IETileTypes.THERMOELECTRIC_GEN.get(), pos, state);
		for(Direction d : DirectionUtils.VALUES)
			energyWrappers.put(d, CapabilityReference.forNeighbor(this, CapabilityEnergy.ENERGY, d));
	}

	@Override
	public void tickServer()
	{
		if(level.getGameTime()%1024==((getBlockPos().getX()^getBlockPos().getZ())&1023))
			recalculateEnergyOutput();
		if(this.energyOutput > 0)
			outputEnergy(this.energyOutput);
	}

	public void outputEnergy(int amount)
	{
		for(Direction fd : DirectionUtils.VALUES)
		{
			IEnergyStorage forSide = energyWrappers.get(fd).getNullable();
			if(forSide!=null)
				amount -= forSide.receiveEnergy(amount, false);
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
			if(!level.isEmptyBlock(getBlockPos().relative(fd))&&!level.isEmptyBlock(getBlockPos().relative(fd.getOpposite())))
			{
				int temp0 = getTemperature(getBlockPos().relative(fd));
				int temp1 = getTemperature(getBlockPos().relative(fd.getOpposite()));
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
			return f.getAttributes().getTemperature(level, pos);
		BlockState state = level.getBlockState(pos);
		return ThermoelectricHandler.getTemperature(state.getBlock());
	}

	@Nullable
	Fluid getFluid(BlockPos pos)
	{
		BlockState state = level.getBlockState(pos);
		FluidState fState = state.getFluidState();
		return fState.getType();
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		this.energyOutput = nbt.getInt("enegyOutput");
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
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