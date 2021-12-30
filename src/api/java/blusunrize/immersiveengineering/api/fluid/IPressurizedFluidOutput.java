package blusunrize.immersiveengineering.api.fluid;

import net.minecraftforge.fluids.FluidStack;

public interface IPressurizedFluidOutput
{
	default int getMaxAcceptedFluidAmount(FluidStack resource)
	{
		return IFluidPipe.AMOUNT_PRESSURIZED;
	}
}
