package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenBarrel;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityMetalBarrel extends TileEntityWoodenBarrel
{
	@Override
	public boolean isFluidValid(FluidStack fluid)
	{
		return fluid!=null && fluid.getFluid()!=null;
	}
}
