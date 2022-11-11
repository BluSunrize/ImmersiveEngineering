package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ComparatorManager<State extends IMultiblockState>
{
	private final List<ComparatorPosition<State>> comparators = new ArrayList<>();

	public ComparatorManager<State> addComparator(IComparatorValue<State> value, BlockPos... positions)
	{
		comparators.add(new ComparatorPosition<>(value, List.of(positions)));
		return this;
	}

	public void updateComparators(IMultiblockContext<State> ctx)
	{
		for(final var comparator : comparators)
			comparator.updateValue(ctx);
	}

	public interface IComparatorValue<State extends IMultiblockState>
	{
		static <State extends IMultiblockState> IComparatorValue<State> inventory(
				Function<State, IItemHandler> getInv, int minSlot, int numSlots
		)
		{
			return state -> {
				int i = 0;
				float f = 0.0F;
				final var inv = getInv.apply(state);
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
	}

	private static class ComparatorPosition<State extends IMultiblockState>
	{
		private final IComparatorValue<State> valueGetter;
		private final List<BlockPos> positions;
		private int lastValue = -1;

		public ComparatorPosition(IComparatorValue<State> valueGetter, List<BlockPos> positions)
		{
			this.valueGetter = valueGetter;
			this.positions = positions;
		}

		public void updateValue(IMultiblockContext<State> ctx)
		{
			final int newValue = valueGetter.getComparatorValue(ctx.getState());
			if(newValue==lastValue)
				return;
			this.lastValue = newValue;
			for(final var position : positions)
				ctx.setComparatorOutputFor(position, newValue);
		}
	}
}
