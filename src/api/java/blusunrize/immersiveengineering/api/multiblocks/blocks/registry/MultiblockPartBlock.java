/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks.blocks.registry;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration.ExtraComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

/**
 * TODO more documentation, this is just the things that seemed hard to discover while writing them<br>
 * Loot table should be generated to include {@code blusunrize.immersiveengineering.data.loot.LootUtils#getMultiblockDropBuilder}
 */
public class MultiblockPartBlock<State extends IMultiblockState> extends Block implements EntityBlock
{
	private final MultiblockRegistration<State> multiblock;
	private final boolean needsServerTicker;
	private final boolean needsClientTicker;

	public MultiblockPartBlock(Properties properties, MultiblockRegistration<State> multiblock)
	{
		super(properties.dynamicShape());
		this.multiblock = multiblock;
		final boolean hasMirrorProperty = getStateDefinition().getProperties().contains(IEProperties.MIRRORED);
		Preconditions.checkState(this.multiblock.mirrorable()==hasMirrorProperty);
		if(multiblock.logic() instanceof IServerTickableComponent<?>)
			needsServerTicker = true;
		else
			needsServerTicker = multiblock.extraComponents().stream()
					.map(ExtraComponent::component)
					.anyMatch(c -> c instanceof IServerTickableComponent<?>);
		if(multiblock.logic() instanceof IClientTickableComponent<?>)
			needsClientTicker = true;
		else
			needsClientTicker = multiblock.extraComponents().stream()
					.map(ExtraComponent::component)
					.anyMatch(c -> c instanceof IClientTickableComponent<?>);
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
		if(level.isClientSide&&needsClientTicker)
			return createTickerHelper(actual, ($1, $2, $3, blockEntity) -> blockEntity.getHelper().tickClient());
		if(!level.isClientSide&&needsServerTicker)
			return createTickerHelper(actual, ($1, $2, $3, blockEntity) -> blockEntity.getHelper().tickServer());
		return null;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	protected <A extends BlockEntity>
	BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> actual, BlockEntityTicker<MultiblockBlockEntityMaster<State>> ticker)
	{
		return multiblock.masterBE().get()==actual?(BlockEntityTicker<A>)ticker: null;
	}

	@Override
	public VoxelShape getCollisionShape(
			@Nonnull BlockState state,
			@Nonnull BlockGetter level,
			@Nonnull BlockPos pos,
			@Nonnull CollisionContext context
	)
	{
		return getShape(level, pos, context, ShapeType.COLLISION);
	}

	@Override
	public VoxelShape getShape(
			@Nonnull BlockState state,
			@Nonnull BlockGetter level,
			@Nonnull BlockPos pos,
			@Nonnull CollisionContext context
	)
	{
		return getShape(level, pos, context, ShapeType.SELECTION);
	}

	@Override
	@Nonnull
	public VoxelShape getBlockSupportShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos)
	{
		return getShape(level, pos, null, ShapeType.BLOCK_SUPPORT);
	}

	@Nonnull
	private VoxelShape getShape(
			@Nonnull BlockGetter level,
			@Nonnull BlockPos pos,
			@Nullable CollisionContext context,
			@Nonnull ShapeType type
	)
	{
		final BlockEntity bEntity = level.getBlockEntity(pos);
		if(bEntity instanceof IMultiblockBE<?> multiblockBE)
			return multiblockBE.getHelper().getShape(context, type);
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
		final BlockEntity bEntity = level.getBlockEntity(pos);
		if(bEntity instanceof IMultiblockBE<?> multiblockBE)
			return multiblockBE.getHelper().click(player, hand, hit);
		else
			return InteractionResult.PASS;
	}

	@Override
	public void entityInside(@Nonnull BlockState state, Level level, @Nonnull BlockPos pos, @Nonnull Entity entity)
	{
		final BlockEntity bEntity = level.getBlockEntity(pos);
		if(bEntity instanceof IMultiblockBE<?> multiblockBE)
			multiblockBE.getHelper().onEntityCollided(entity);
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

	@Override
	@Nonnull
	public ItemStack getCloneItemStack(@Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state)
	{
		if(level.getBlockEntity(pos) instanceof IMultiblockBE<?> multiblockBE)
			return multiblockBE.getHelper().getPickBlock();
		else
			return ItemStack.EMPTY;
	}

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
