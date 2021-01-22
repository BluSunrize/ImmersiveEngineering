package blusunrize.immersiveengineering.api.fluid;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.Optional;

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

	/**
	 * Same as {@link FluidUtil#getFluidContained(ItemStack)}, but does not create ItemStack copies for no reason (and
	 * instead assumes that fluid handlers ignore stack size)
	 */
	public static Optional<FluidStack> getFluidContained(@Nonnull ItemStack container)
	{
		if(!container.isEmpty())
			return FluidUtil.getFluidHandler(container)
					.map(handler -> handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE));
		return Optional.empty();
	}
}
