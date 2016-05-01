package blusunrize.immersiveengineering.api.fluid;

import net.minecraft.util.EnumFacing;

public interface IFluidPipe
{
	boolean canOutputPressurized(boolean consumePower);
	boolean hasOutputConnection(EnumFacing side);
}
