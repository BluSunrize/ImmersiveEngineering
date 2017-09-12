package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenBarrel;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityMetalBarrel extends TileEntityWoodenBarrel
{
    @Override
	public void update() {
        if (world.isRemote || world.isBlockIndirectlyGettingPowered(getPos())>0) return;
        else super.update();
    }
	@Override
	public boolean isFluidValid(FluidStack fluid)
	{
		return fluid!=null && fluid.getFluid()!=null;
	}
}
