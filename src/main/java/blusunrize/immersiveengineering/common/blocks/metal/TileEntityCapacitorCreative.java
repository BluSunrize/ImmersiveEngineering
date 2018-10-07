/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class TileEntityCapacitorCreative extends TileEntityCapacitorLV
{
	public TileEntityCapacitorCreative()
	{
		super();
		for(int i = 0; i < sideConfig.length; i++)
			sideConfig[i] = SideConfig.OUTPUT;
	}

	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate)
	{
		if(world.isRemote||from.ordinal() >= sideConfig.length||sideConfig[from.ordinal()]!=SideConfig.INPUT)
			return 0;
		return maxReceive;
	}

	@Override
	public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate)
	{
		if(world.isRemote||from.ordinal() >= sideConfig.length||sideConfig[from.ordinal()]!=SideConfig.OUTPUT)
			return 0;
		return maxExtract;
	}

	@Override
	public int getEnergyStored(EnumFacing from)
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from)
	{
		return Integer.MAX_VALUE;
	}

	@Override
	protected void transferEnergy(int side)
	{
		if(sideConfig[side]!=SideConfig.OUTPUT)
			return;
		EnumFacing to = EnumFacing.byIndex(side);
		TileEntity te = Utils.getExistingTileEntity(world, pos.offset(to));
		EnergyHelper.insertFlux(te, to.getOpposite(), Integer.MAX_VALUE, false);
	}
}
