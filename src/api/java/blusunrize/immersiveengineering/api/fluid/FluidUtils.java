package blusunrize.immersiveengineering.api.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;

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
		if(stripPressure&&fs.hasTag()&&fs.getOrCreateTag().contains(IFluidPipe.NBT_PRESSURIZED))
		{
			CompoundTag tag = fs.getOrCreateTag();
			tag.remove(IFluidPipe.NBT_PRESSURIZED);
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

	public static boolean multiblockFluidOutput(
			Level level, BlockPos targetPos, Direction dir, FluidTank tank,
			int slotIn, int slotOut, IntFunction<ItemStack> invGet, BiConsumer<Integer, ItemStack> invSet
	)
	{
		boolean updateTile = false;
		if(tank.getFluidAmount() > 0)
		{
			// Handle container filling first, so that players can "intercept" the output
			if(slotIn >= 0&&slotOut >= 0&&invGet!=null&&invSet!=null)
				updateTile = fillFluidContainer(tank, slotIn, slotOut, invGet, invSet);

			// Then try to output into pipes or similar
			FluidStack out = copyFluidStackWithAmount(tank.getFluid(), Math.min(tank.getFluidAmount(), FluidAttributes.BUCKET_VOLUME), false);
			updateTile |= FluidUtil.getFluidHandler(level, targetPos, dir.getOpposite()).map(output -> {
				int accepted = output.fill(out, FluidAction.SIMULATE);
				if(accepted > 0)
				{
					int drained = output.fill(copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false), FluidAction.EXECUTE);
					tank.drain(drained, FluidAction.EXECUTE);
					return true;
				}
				return false;
			}).orElse(false);
		}
		return updateTile;
	}

	public static boolean fillFluidContainer(IFluidHandler handler, int slotIn, int slotOut, IntFunction<ItemStack> invGet, BiConsumer<Integer, ItemStack> invSet)
	{
		ItemStack filledContainer = fillFluidContainer(handler, invGet.apply(slotIn), invGet.apply(slotOut), null);
		if(!filledContainer.isEmpty())
		{
			if(invGet.apply(slotIn).getCount()==1&&!isFluidContainerFull(filledContainer))
				invSet.accept(slotIn, filledContainer.copy());
			else
			{
				if(!invGet.apply(slotOut).isEmpty()&&ItemHandlerHelper.canItemStacksStack(filledContainer, invGet.apply(slotOut)))
					invGet.apply(slotOut).grow(filledContainer.getCount());
				else
					invSet.accept(slotOut, filledContainer);
				invGet.apply(slotIn).shrink(1);
				if(invGet.apply(slotIn).getCount() <= 0)
					invSet.accept(slotIn, ItemStack.EMPTY);
			}
			return true;
		}
		return false;
	}

	public static ItemStack fillFluidContainer(IFluidHandler handler, ItemStack containerIn, ItemStack containerOut, @Nullable Player player)
	{
		if(containerIn==null||containerIn.isEmpty())
			return ItemStack.EMPTY;

		FluidActionResult result = FluidUtil.tryFillContainer(containerIn, handler, Integer.MAX_VALUE, player, false);
		if(result.isSuccess())
		{
			final ItemStack full = result.getResult();
			if((containerOut.isEmpty()||ItemHandlerHelper.canItemStacksStack(containerOut, full)))
			{
				if(!containerOut.isEmpty()&&containerOut.getCount()+full.getCount() > containerOut.getMaxStackSize())
					return ItemStack.EMPTY;
				result = FluidUtil.tryFillContainer(containerIn, handler, Integer.MAX_VALUE, player, true);
				if(result.isSuccess())
				{
					return result.getResult();
				}
			}
		}
		return ItemStack.EMPTY;
	}

	public static boolean isFluidContainerFull(ItemStack stack)
	{
		return FluidUtil.getFluidHandler(stack)
				.map(handler -> {
					for(int t = 0; t < handler.getTanks(); ++t)
						if(handler.getFluidInTank(t).getAmount() < handler.getTankCapacity(t))
							return false;
					return true;
				})
				.orElse(true);
	}

	public static boolean interactWithFluidHandler(Player player, InteractionHand hand, IFluidHandler handler)
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
