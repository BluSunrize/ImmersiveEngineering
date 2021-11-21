/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Machines.CapacitorConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.IEnergyStorage;

public class CapacitorCreativeBlockEntity extends CapacitorBlockEntity
{
	public CapacitorCreativeBlockEntity(BlockPos pos, BlockState state)
	{
		super(CapacitorConfig.CREATIVE, pos, state);
		for(Direction d : DirectionUtils.VALUES)
			sideConfig.put(d, IOSideConfig.OUTPUT);
	}

	@Override
	protected IEnergyStorage makeMainEnergyStorage()
	{
		return InfiniteEnergyStorage.INSTANCE;
	}

	private static class InfiniteEnergyStorage implements IEnergyStorage
	{
		public static final IEnergyStorage INSTANCE = new InfiniteEnergyStorage();

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate)
		{
			return maxReceive;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate)
		{
			return maxExtract;
		}

		@Override
		public int getEnergyStored()
		{
			return Integer.MAX_VALUE;
		}

		@Override
		public int getMaxEnergyStored()
		{
			return Integer.MAX_VALUE;
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
