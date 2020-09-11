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
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

public class CapacitorCreativeTileEntity extends CapacitorLVTileEntity
{
	public static TileEntityType<CapacitorCreativeTileEntity> TYPE;

	public CapacitorCreativeTileEntity()
	{
		super(TYPE, CapacitorConfig.CREATIVE);
		for(Direction d : Direction.VALUES)
			sideConfig.put(d, IOSideConfig.OUTPUT);
	}

	@Override
	public int receiveEnergy(Direction from, int maxReceive, boolean simulate)
	{
		if(world.isRemote||sideConfig.get(from)!=IOSideConfig.INPUT)
			return 0;
		return maxReceive;
	}

	@Override
	public int extractEnergy(Direction from, int maxExtract, boolean simulate)
	{
		if(world.isRemote||sideConfig.get(from)!=IOSideConfig.OUTPUT)
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
		TileEntity te = Utils.getExistingTileEntity(world, pos.offset(side));
		EnergyHelper.insertFlux(te, side.getOpposite(), Integer.MAX_VALUE, false);
	}
}
