/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.energy.NullEnergyStorage;
import blusunrize.immersiveengineering.api.energy.ThermoelectricSource;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.CachedRecipe;
import blusunrize.immersiveengineering.common.util.ResettableCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public class ThermoelectricGenBlockEntity extends IEBaseBlockEntity implements IEServerTickableBE
{
	private int energyOutput = -1;
	private final Map<Direction, CapabilityReference<IEnergyStorage>> energyWrappers = CapabilityReference.forAllNeighbors(
			this, CapabilityEnergy.ENERGY
	);

	public ThermoelectricGenBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.THERMOELECTRIC_GEN.get(), pos, state);
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
				int temp0 = getTemperature(fd);
				int temp1 = getTemperature(fd.getOpposite());
				if(temp0 > -1&&temp1 > -1)
				{
					int diff = Math.abs(temp0-temp1);
					energy += (int)(Math.sqrt(diff)/2*IEServerConfig.MACHINES.thermoelectric_output.get());
				}
			}
		this.energyOutput = energy==0?-1: energy;
	}

	private final Map<Direction, Function<Block, Integer>> temperatureGetters = new EnumMap<>(Direction.class);

	{
		for(Direction d : DirectionUtils.VALUES)
			temperatureGetters.put(d, CachedRecipe.cached(ThermoelectricSource::getSource).andThen(
					source -> source==null?-1: source.getTemperature()
			));
	}

	private int getTemperature(Direction offset)
	{
		BlockPos pos = worldPosition.relative(offset);
		BlockState state = level.getBlockState(pos);
		Fluid f = getFluid(state);
		if(f!=Fluids.EMPTY)
			return f.getAttributes().getTemperature(level, pos);
		return temperatureGetters.get(offset).apply(state.getBlock());
	}

	Fluid getFluid(BlockState state)
	{
		return state.getFluidState().getType();
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

	private final ResettableCapability<IEnergyStorage> energyCap = registerCapability(NullEnergyStorage.INSTANCE);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		if(cap==CapabilityEnergy.ENERGY)
			return energyCap.cast();
		return super.getCapability(cap, side);
	}
}