/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Machines.CapacitorConfig;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CapacitorCreativeTileEntity extends CapacitorTileEntity
{
	public CapacitorCreativeTileEntity(BlockPos pos, BlockState state)
	{
		super(CapacitorConfig.CREATIVE, pos, state);
		for(Direction d : DirectionUtils.VALUES)
			sideConfig.put(d, IOSideConfig.OUTPUT);
	}

	@Override
	public int receiveEnergy(Direction from, int maxReceive, boolean simulate)
	{
		if(level.isClientSide||sideConfig.get(from)!=IOSideConfig.INPUT)
			return 0;
		return maxReceive;
	}

	@Override
	public int extractEnergy(Direction from, int maxExtract, boolean simulate)
	{
		if(level.isClientSide||sideConfig.get(from)!=IOSideConfig.OUTPUT)
			return 0;
		return maxExtract;
	}

	@Override
	public int getEnergyStored(Direction from)
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public int getMaxEnergyStored(Direction from)
	{
		return Integer.MAX_VALUE;
	}

	@Override
	protected void transferEnergy(Direction side)
	{
		if(sideConfig.get(side)!=IOSideConfig.OUTPUT)
			return;
		BlockEntity te = Utils.getExistingTileEntity(level, worldPosition.relative(side));
		EnergyHelper.insertFlux(te, side.getOpposite(), Integer.MAX_VALUE, false);
	}
}
