package blusunrize.immersiveengineering.api.multiblocks.blocks.registry;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IClientTickableMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IServerTickableMultiblock;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiConsumer;

public class MultiblockPartBlock<State extends IMultiblockState> extends Block implements EntityBlock
{
	private final MultiblockRegistration<State> multiblock;

	public MultiblockPartBlock(Properties properties, MultiblockRegistration<State> multiblock)
	{
		super(properties.dynamicShape());
		this.multiblock = multiblock;
		final var hasMirrorProperty = getStateDefinition().getProperties().contains(IEProperties.MIRRORED);
		Preconditions.checkState(this.multiblock.mirrorable()==hasMirrorProperty);
	}

	@Override
	protected void createBlockStateDefinition(@Nonnull Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(IEProperties.MULTIBLOCKSLAVE);
		builder.add(IEProperties.FACING_HORIZONTAL);
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
		if(level.isClientSide&&multiblock.logic() instanceof IClientTickableMultiblock<State> clientTickable)
			return makeTicker(actual, IClientTickableMultiblock::tickClient, clientTickable);
		if(!level.isClientSide&&multiblock.logic() instanceof IServerTickableMultiblock<State> serverTickable)
			return makeTicker(actual, IServerTickableMultiblock::tickServer, serverTickable);
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

	@Nonnull
	@Override
	public VoxelShape getShape(
			@Nonnull BlockState state,
			@Nonnull BlockGetter level,
			@Nonnull BlockPos pos,
			@Nonnull CollisionContext context
	)
	{
		final var bEntity = level.getBlockEntity(pos);
		if(bEntity instanceof IMultiblockBE<?> multiblockBE)
			return multiblockBE.getHelper().getShape(context);
		else
			return Shapes.block();
	}

	@Override
	public void onRemove(BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, BlockState newState, boolean isMoving)
	{
		if(state.getBlock()!=newState.getBlock())
		{
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity instanceof IMultiblockBE<?> multiblockBE)
			{
				// Remove the BE here before disassembling: The block is already gone, so setting the block state here
				// to a block providing a BE will produce strange results otherwise
				super.onRemove(state, level, pos, newState, isMoving);
				multiblockBE.getHelper().disassemble();
				return;
			}
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Nonnull
	@Override
	public InteractionResult use(
			@Nonnull BlockState state,
			@Nonnull Level level,
			@Nonnull BlockPos pos,
			@Nonnull Player player,
			@Nonnull InteractionHand hand,
			@Nonnull BlockHitResult hit
	)
	{
		final var bEntity = level.getBlockEntity(pos);
		if(bEntity instanceof IMultiblockBE<?> multiblockBE)
			return multiblockBE.getHelper().click(player, hand, hit);
		else
			return InteractionResult.PASS;
	}

	@Override
	public void entityInside(@Nonnull BlockState state, Level level, @Nonnull BlockPos pos, @Nonnull Entity entity)
	{
		final var bEntity = level.getBlockEntity(pos);
		if(bEntity instanceof IMultiblockBE<?> multiblockBE)
			multiblockBE.getHelper().onEntityCollided(entity);
	}

	private <O, A extends BlockEntity> BlockEntityTicker<A> makeTicker(
			BlockEntityType<A> actual, BiConsumer<O, IMultiblockContext<State>> tick, O obj
	)
	{
		return createTickerHelper(
				actual,
				multiblock.masterBE().get(),
				($1, $2, $3, be) -> tick.accept(obj, be.getHelper().getContext())
		);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean hasAnalogOutputSignal(@Nonnull BlockState state)
	{
		return multiblock.hasComparatorOutput();
	}

	@SuppressWarnings("deprecation")
	@Override
	public int getAnalogOutputSignal(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos)
	{
		if(multiblock.hasComparatorOutput()&&level.getBlockEntity(pos) instanceof IMultiblockBE<?> multiblockBE)
			return multiblockBE.getHelper().getComparatorValue();
		return super.getAnalogOutputSignal(state, level, pos);
	}

	@Override
	public void neighborChanged(
			@Nonnull BlockState state,
			@Nonnull Level level,
			@Nonnull BlockPos pos,
			@Nonnull Block block,
			@Nonnull BlockPos fromPos,
			boolean isMoving
	)
	{
		super.neighborChanged(state, level, pos, block, fromPos, isMoving);
		if(multiblock.redstoneInputAware()&&level.getBlockEntity(pos) instanceof IMultiblockBE<?> multiblockBE)
			multiblockBE.getHelper().onNeighborChanged(fromPos);
	}

	// TODO loot table
	// TODO pick block

	public static class WithMirrorState<State extends IMultiblockState> extends MultiblockPartBlock<State>
	{
		public WithMirrorState(Properties properties, MultiblockRegistration<State> multiblock)
		{
			super(properties, multiblock);
		}

		@Override
		protected void createBlockStateDefinition(@Nonnull Builder<Block, BlockState> builder)
		{
			super.createBlockStateDefinition(builder);
			builder.add(IEProperties.MIRRORED);
		}
	}
}
