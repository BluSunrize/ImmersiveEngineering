package blusunrize.immersiveengineering.api.fluid;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.Optional;

public class FluidUtils
{
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

	public static FluidActionResult tryEmptyContainer(
			@Nonnull ItemStack container, IFluidHandler fluidDestination, int maxAmount, FluidAction doDrain
	)
	{
		ItemStack containerCopy = ItemHandlerHelper.copyStackWithSize(container, 1);
		return containerCopy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).map(handler -> {
			final FluidStack simulatedMoved = FluidUtil.tryFluidTransfer(
					fluidDestination, handler, maxAmount, false
			);
			if(simulatedMoved.isEmpty())
				return FluidActionResult.FAILURE;
			handler.drain(simulatedMoved, FluidAction.EXECUTE);
			fluidDestination.fill(simulatedMoved, doDrain);
			return new FluidActionResult(handler.getContainer());
		}).orElse(FluidActionResult.FAILURE);
	}
}
