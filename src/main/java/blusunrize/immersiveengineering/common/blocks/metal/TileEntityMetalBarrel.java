package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraftforge.fluids.FluidStack;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenBarrel;

public class TileEntityMetalBarrel extends TileEntityWoodenBarrel
{
	@Override
	public boolean isFluidValid(FluidStack fluid)
	{
		return fluid!=null && fluid.getFluid()!=null;
	}
}
