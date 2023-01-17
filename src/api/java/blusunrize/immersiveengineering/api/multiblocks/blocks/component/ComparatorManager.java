/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks.blocks.component;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent.StateWrapper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.ComparatorManager.WrappedState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class ComparatorManager<State>
		implements IServerTickableComponent<WrappedState<State>>, StateWrapper<State, WrappedState<State>>
{
	private final ComparatorValue<State> valueGetter;
	private final List<BlockPos> positions;

	public static <State>
	ComparatorManager<State> makeSimple(SimpleComparatorValue<State> value, BlockPos... positions)
	{
		return new ComparatorManager<>(value, positions);
	}

	public ComparatorManager(ComparatorValue<State> value, BlockPos... positions)
	{
		this.valueGetter = value;
		this.positions = Arrays.asList(positions);
	}

	@Override
	public void tickServer(IMultiblockContext<WrappedState<State>> ctx)
	{
		final WrappedState<State> state = ctx.getState();
		final int newValue = valueGetter.getComparatorValue(state.inner, ctx.getLevel());
		if(newValue!=state.lastValue)
		{
			for(final BlockPos position : positions)
				ctx.setComparatorOutputFor(position, newValue);
			state.lastValue = newValue;
		}
	}

	@Override
	public WrappedState<State> wrapState(State outer)
	{
		return new WrappedState<>(outer);
	}

	public interface SimpleComparatorValue<State> extends ComparatorValue<State>
	{
		static <State extends IMultiblockState> SimpleComparatorValue<State> inventory(
				Function<State, IItemHandler> getInv, int minSlot, int numSlots
		)
		{
			return state -> {
				int i = 0;
				float f = 0.0F;
				final IItemHandler inv = getInv.apply(state);
				for(int j = minSlot; j < minSlot+numSlots; ++j)
				{
					ItemStack itemstack = inv.getStackInSlot(j);
					if(!itemstack.isEmpty())
					{
						f += (float)itemstack.getCount()/(float)Math.min(inv.getSlotLimit(j), itemstack.getMaxStackSize());
						++i;
					}
				}
				f = f/(float)numSlots;
				return Mth.floor(f*14.0F)+(i > 0?1: 0);
			};
		}

		int getComparatorValue(State state);

		@Override
		default int getComparatorValue(State state, IMultiblockLevel level)
		{
			return getComparatorValue(state);
		}
	}

	public interface ComparatorValue<State>
	{
		int getComparatorValue(State state, IMultiblockLevel level);
	}

	public static class WrappedState<T>
	{
		private final T inner;
		private int lastValue = -1;

		public WrappedState(T inner)
		{
			this.inner = inner;
		}
	}
}
