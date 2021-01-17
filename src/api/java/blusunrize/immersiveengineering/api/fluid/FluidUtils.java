package blusunrize.immersiveengineering.api.fluid;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;

public class FluidUtils
{
	public static FluidStack copyFluidStackWithAmount(FluidStack stack, int amount)
	{
		return copyFluidStackWithAmount(stack, amount, false);
	}

	public static FluidStack copyFluidStackWithAmount(FluidStack stack, int amount, boolean stripPressure)
	{
		if(stack==null)
			return null;
		FluidStack fs = new FluidStack(stack, amount);
		if(stripPressure&&fs.hasTag()&&fs.getOrCreateTag().contains("pressurized"))
		{
			CompoundNBT tag = fs.getOrCreateTag();
			tag.remove("pressurized");
			if(tag.isEmpty())
				fs.setTag(null);
		}
		return fs;
	}

}
