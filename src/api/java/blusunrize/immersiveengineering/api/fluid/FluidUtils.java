package blusunrize.immersiveengineering.api.fluid;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;

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

	public static boolean interactWithFluidHandler(PlayerEntity player, Hand hand, IFluidHandler handler)
	{
		Mutable<FluidStack> lastNonSimulated = new MutableObject<>();
		MutableBoolean isInsert = new MutableBoolean();
		IFluidHandler simulationWrapper = new WrapperFluidHandler(handler)
		{
			@Override
			public int fill(FluidStack resource, FluidAction action)
			{
				int result = handler.fill(resource, FluidAction.SIMULATE);
				if(action==FluidAction.EXECUTE)
				{
					lastNonSimulated.setValue(new FluidStack(resource, result));
					isInsert.setTrue();
				}
				return result;
			}

			@Nonnull
			@Override
			public FluidStack drain(FluidStack resource, FluidAction action)
			{
				FluidStack result = handler.drain(resource, FluidAction.SIMULATE);
				if(action==FluidAction.EXECUTE)
				{
					isInsert.setFalse();
					lastNonSimulated.setValue(result.copy());
				}
				return result;
			}

			@Nonnull
			@Override
			public FluidStack drain(int maxDrain, FluidAction action)
			{
				FluidStack result = handler.drain(maxDrain, FluidAction.SIMULATE);
				if(action==FluidAction.EXECUTE)
				{
					isInsert.setFalse();
					lastNonSimulated.setValue(result.copy());
				}
				return result;
			}
		};
		// The Forge method is broken for stacks of >1 item, in that case tryFill/EmptyContainer is invoked with
		// doFill=false, which still performs non-simulated calls on the (tile) handler. Instead of creating a custom
		// implementation of the complete logic that needs to be kept in sync with Forge we just replace all EXECUTE
		// calls with SIMULATE and perform the relevant EXECUTE call at the end as necessary
		final boolean success = FluidUtil.interactWithFluidHandler(player, hand, simulationWrapper);
		if(success)
		{
			if(isInsert.booleanValue())
				handler.fill(lastNonSimulated.getValue(), FluidAction.EXECUTE);
			else
				handler.drain(lastNonSimulated.getValue(), FluidAction.EXECUTE);
		}
		return success;
	}

	public static abstract class WrapperFluidHandler implements IFluidHandler
	{
		private final IFluidHandler handler;

		protected WrapperFluidHandler(IFluidHandler handler)
		{
			this.handler = handler;
		}

		@Override
		public int getTanks()
		{
			return handler.getTanks();
		}

		@Nonnull
		@Override
		public FluidStack getFluidInTank(int tank)
		{
			return handler.getFluidInTank(tank);
		}

		@Override
		public int getTankCapacity(int tank)
		{
			return handler.getTankCapacity(tank);
		}

		@Override
		public boolean isFluidValid(int tank, @Nonnull FluidStack stack)
		{
			return handler.isFluidValid(tank, stack);
		}
	}
}
