/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.config.IEServerConfig;
import net.minecraft.tileentity.TileEntityType;

//TODO tile type, but no custom class
public class CapacitorHVTileEntity extends CapacitorLVTileEntity
{
	public static TileEntityType<CapacitorHVTileEntity> TYPE;

	public CapacitorHVTileEntity()
	{
		super(TYPE, IEServerConfig.MACHINES.hvCapConfig);
	}
}