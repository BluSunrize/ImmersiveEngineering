/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import net.minecraft.tileentity.TileEntity;

/**
 * @author BluSunrize - 06.05.2017
 */
public class ConveyorUncontrolled extends ConveyorBasic
{
	@Override
	public boolean isActive(TileEntity tile)
	{
		return true;
	}
}
