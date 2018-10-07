/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenBarrel;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityMetalBarrel extends TileEntityWoodenBarrel
{
	@Override
	public void update()
	{
		if(world.isRemote||world.getRedstonePowerFromNeighbors(getPos()) > 0) return;
		else super.update();
	}

	@Override
	public boolean isFluidValid(FluidStack fluid)
	{
		return fluid!=null&&fluid.getFluid()!=null;
	}
}
