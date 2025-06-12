/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.fluid;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.math.Fraction;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;

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
		ItemStack containerCopy = container.copyWithCount(1);
		IFluidHandlerItem handler = containerCopy.getCapability(FluidHandler.ITEM);
		if(handler==null)
			return FluidActionResult.FAILURE;
		final FluidStack simulatedMoved = FluidUtil.tryFluidTransfer(
				fluidDestination, handler, maxAmount, false
		);
		if(simulatedMoved.isEmpty())
			return FluidActionResult.FAILURE;
		handler.drain(simulatedMoved, FluidAction.EXECUTE);
		fluidDestination.fill(simulatedMoved, doDrain);
		return new FluidActionResult(handler.getContainer());
	}

	public static boolean multiblockFluidOutput(
			@Nullable IFluidHandler output, FluidTank tank,
			int slotIn, int slotOut, @Nullable IItemHandlerModifiable inv
	)
	{
		boolean updateTile = false;
		if(tank.getFluidAmount() > 0)
		{
			// Handle container filling first, so that players can "intercept" the output
			if(slotIn >= 0&&slotOut >= 0&&inv!=null)
				updateTile = fillFluidContainer(tank, slotIn, slotOut, inv);

			// Then try to output into pipes or similar
			FluidStack out = tank.getFluid().copyWithAmount(Math.min(tank.getFluidAmount(), FluidType.BUCKET_VOLUME));
			if(output!=null)
			{
				int accepted = output.fill(out, FluidAction.EXECUTE);
				if(accepted > 0)
				{
					tank.drain(accepted, FluidAction.EXECUTE);
					return true;
				}
			}
		}
		return updateTile;
	}

	public static boolean fillFluidContainer(IFluidHandler handler, int slotIn, int slotOut, IItemHandlerModifiable inv)
	{
		ItemStack filledContainer = fillFluidContainer(handler, inv.getStackInSlot(slotIn), inv.getStackInSlot(slotOut), null);
		if(!filledContainer.isEmpty())
		{
			if(inv.getStackInSlot(slotIn).getCount()==1&&!isFluidContainerFull(filledContainer))
				inv.setStackInSlot(slotIn, filledContainer.copy());
			else
			{
				if(!inv.getStackInSlot(slotOut).isEmpty()&&ItemStack.isSameItemSameComponents(filledContainer, inv.getStackInSlot(slotOut)))
					inv.getStackInSlot(slotOut).grow(filledContainer.getCount());
				else
					inv.setStackInSlot(slotOut, filledContainer);
				inv.getStackInSlot(slotIn).shrink(1);
				if(inv.getStackInSlot(slotIn).getCount() <= 0)
					inv.setStackInSlot(slotIn, ItemStack.EMPTY);
			}
			return true;
		}
		return false;
	}

	@Deprecated
	public static boolean fillFluidContainer(IFluidHandler handler, int slotIn, int slotOut, IntFunction<ItemStack> invGet, BiConsumer<Integer, ItemStack> invSet)
	{
		ItemStack filledContainer = fillFluidContainer(handler, invGet.apply(slotIn), invGet.apply(slotOut), null);
		if(!filledContainer.isEmpty())
		{
			if(invGet.apply(slotIn).getCount()==1&&!isFluidContainerFull(filledContainer))
				invSet.accept(slotIn, filledContainer.copy());
			else
			{
				if(!invGet.apply(slotOut).isEmpty()&&ItemStack.isSameItemSameComponents(filledContainer, invGet.apply(slotOut)))
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
			if((containerOut.isEmpty()||ItemStack.isSameItemSameComponents(containerOut, full)))
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
					lastNonSimulated.setValue(resource.copyWithAmount(result));
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

	/** Set by IE's client config. If set to false, factions will be displayed as decimals instead. */
	public static boolean enableFractionDisplay = true;
	private static final Map<Fraction, String> FRACTION_STRINGS = new HashMap<>();
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

	static
	{
		// this is not all the existing fraction symbols, but the most commonly used
		FRACTION_STRINGS.put(Fraction.ONE_HALF, "\u00BD");
		FRACTION_STRINGS.put(Fraction.ONE_QUARTER, "\u00BC");
		FRACTION_STRINGS.put(Fraction.THREE_QUARTERS, "\u00BE");
		FRACTION_STRINGS.put(Fraction.ONE_THIRD, "\u2153");
		FRACTION_STRINGS.put(Fraction.TWO_THIRDS, "\u2154");
		FRACTION_STRINGS.put(Fraction.ONE_FIFTH, "\u2155");
		FRACTION_STRINGS.put(Fraction.TWO_FIFTHS, "\u2156");
		FRACTION_STRINGS.put(Fraction.THREE_FIFTHS, "\u2157");
		FRACTION_STRINGS.put(Fraction.FOUR_FIFTHS, "\u2158");
		FRACTION_STRINGS.put(Fraction.getFraction(1, 8), "\u215B");
		FRACTION_STRINGS.put(Fraction.getFraction(3, 8), "\u215C");
		FRACTION_STRINGS.put(Fraction.getFraction(1, 9), "\u2151");
		FRACTION_STRINGS.put(Fraction.getFraction(1, 10), "\u2152");
	}

	public static String getBucketFraction(int amount)
	{
		String ret = "";
		// if amount is bigger than bucket, consider those as full numbers
		if(amount >= FluidType.BUCKET_VOLUME)
		{
			ret += amount/FluidType.BUCKET_VOLUME;
			amount = amount%FluidType.BUCKET_VOLUME;
		}
		// remaining amount
		if(amount > 0)
		{
			Fraction key = Fraction.getReducedFraction(amount, FluidType.BUCKET_VOLUME);
			// use fraction symbols where possible
			if(enableFractionDisplay && FRACTION_STRINGS.containsKey(key))
				ret += (ret.isEmpty()?"": " ")+FRACTION_STRINGS.get(key);
			else // fall back on decimals otherwise
			{
				double decimal = amount/(double)FluidType.BUCKET_VOLUME;
				String decimalString = DECIMAL_FORMAT.format(decimal);
				if(!ret.isEmpty())
					ret += decimalString.substring(1);
				else
					ret = decimalString;
			}
		}
		return ret;
	}
}
