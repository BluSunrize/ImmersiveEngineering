/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.IEConfig;
import net.minecraft.tileentity.TileEntityType;

public class CapacitorMVTileEntity extends CapacitorLVTileEntity
{
	public static TileEntityType<CapacitorMVTileEntity> TYPE;

	public CapacitorMVTileEntity()
	{
		super(TYPE);
	}

	@Override
	public int getMaxStorage()
	{
		return IEConfig.MACHINES.capacitorMvStorage.get();
	}

	@Override
	public int getMaxInput()
	{
		return IEConfig.MACHINES.capacitorMvInput.get();
	}

	@Override
	public int getMaxOutput()
	{
		return IEConfig.MACHINES.capacitorMvOutput.get();
	}

}