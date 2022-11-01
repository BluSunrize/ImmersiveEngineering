package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MultiblockPartBlock<State extends IMultiblockState> extends Block implements EntityBlock
{
	private final MultiblockRegistration<State> multiblock;

	public MultiblockPartBlock(Properties properties, MultiblockRegistration<State> multiblock)
	{
		super(properties);
		this.multiblock = multiblock;
	}

	@Override
	protected void createBlockStateDefinition(@Nonnull Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(IEProperties.MULTIBLOCKSLAVE);
		builder.add(IEProperties.FACING_HORIZONTAL);
		// TODO deal with this mess
		//if(multiblock.mirrorable())
		//	builder.add(IEProperties.MIRRORED);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state)
	{
		if(state.getValue(IEProperties.MULTIBLOCKSLAVE))
			return multiblock.dummyBE().get().create(pos, state);
		else
			return multiblock.masterBE().get().create(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
			@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> actual
	)
	{
		if(state.getValue(IEProperties.MULTIBLOCKSLAVE))
			return null;
		if(level.isClientSide())
		{
			// TODO client tickable
		}
		else
		{
			if(multiblock.logic() instanceof IServerTickableMultiblock<State> serverTickable)
				return createTickerHelper(
						actual,
						multiblock.masterBE().get(),
						($1, $2, $3, be) -> serverTickable.tickServer(be.getHelper().getContext())
				);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	protected static <E extends BlockEntity, A extends BlockEntity>
	BlockEntityTicker<A> createTickerHelper(
			BlockEntityType<A> actual, BlockEntityType<E> expected, BlockEntityTicker<? super E> ticker
	)
	{
		return expected==actual?(BlockEntityTicker<A>)ticker: null;
	}
}
