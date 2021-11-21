/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IPostBlock;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.common.blocks.generic.ConnectorBlock;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEBlocks.Connectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nullable;
import java.util.Optional;

public class PostTransformerBlock extends ConnectorBlock<PostTransformerBlockEntity>
{
	public PostTransformerBlock(Properties props)
	{
		super(props, IEBlockEntities.POST_TRANSFORMER);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(IEProperties.FACING_HORIZONTAL, BlockStateProperties.WATERLOGGED);
	}

	@Override
	public Item asItem()
	{
		return Connectors.TRANSFORMER.get().asItem();
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn,
										  BlockPos currentPos, BlockPos facingPos)
	{
		BlockState baseState = super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
		return stateForPos(baseState, currentPos, worldIn, Blocks.AIR.defaultBlockState());
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		BlockState baseState = super.getStateForPlacement(context);
		return stateForPos(baseState, context.getClickedPos(), context.getLevel(), null);
	}

	private BlockState stateForPos(@Nullable BlockState baseState, BlockPos pos, BlockGetter world, BlockState empty)
	{
		if(baseState==null||baseState.getBlock()!=this)
			return empty;
		Direction preferred = baseState.getValue(IEProperties.FACING_HORIZONTAL);
		Optional<Direction> newFacing = findAttacheablePost(pos, world, preferred);
		if(newFacing.isPresent())
			return baseState.setValue(IEProperties.FACING_HORIZONTAL, newFacing.get());
		else
			return empty;
	}

	private static Optional<Direction> findAttacheablePost(BlockPos transformerPos, BlockGetter world, Direction preferred)
	{
		Optional<Direction> ret = Optional.empty();
		for(Direction d : DirectionUtils.BY_HORIZONTAL_INDEX)
			if(isAttacheablePost(transformerPos.relative(d), world))
			{
				ret = Optional.of(d);
				if(d==preferred)
					break;
			}
		return ret;
	}

	public static boolean isAttacheablePost(BlockPos possiblePost, BlockGetter w)
	{
		BlockState postState = w.getBlockState(possiblePost);
		if(!(postState.getBlock() instanceof IPostBlock))
			return false;
		IPostBlock post = (IPostBlock)postState.getBlock();
		return post.canConnectTransformer(w, possiblePost);
	}
}
