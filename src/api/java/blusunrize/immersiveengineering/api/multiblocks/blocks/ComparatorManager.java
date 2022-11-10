package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

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
