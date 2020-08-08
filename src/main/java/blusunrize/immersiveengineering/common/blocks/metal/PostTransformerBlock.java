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
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Connectors;
import blusunrize.immersiveengineering.common.blocks.generic.MiscConnectorBlock;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;

public class PostTransformerBlock extends MiscConnectorBlock
{
	public PostTransformerBlock()
	{
		super("post_transformer", () -> PostTransformerTileEntity.TYPE,
				ImmutableList.of(IEProperties.FACING_HORIZONTAL, BlockStateProperties.WATERLOGGED),
				ImmutableList.of(), (b, p) -> null);
	}

	@Override
	public Item asItem()
	{
		return Connectors.transformer.asItem();
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
										  BlockPos currentPos, BlockPos facingPos)
	{
		BlockState baseState = super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
		return stateForPos(baseState, currentPos, worldIn, Blocks.AIR.getDefaultState());
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		BlockState baseState = super.getStateForPlacement(context);
		return stateForPos(baseState, context.getPos(), context.getWorld(), null);
	}

	private BlockState stateForPos(@Nullable BlockState baseState, BlockPos pos, IBlockReader world, BlockState empty)
	{
		if(baseState==null||baseState.getBlock()!=this)
			return empty;
		Direction preferred = baseState.get(IEProperties.FACING_HORIZONTAL);
		Optional<Direction> newFacing = findAttacheablePost(pos, world, preferred);
		if(newFacing.isPresent())
			return baseState.with(IEProperties.FACING_HORIZONTAL, newFacing.get());
		else
			return empty;
	}

	private static Optional<Direction> findAttacheablePost(BlockPos transformerPos, IBlockReader world, Direction preferred)
	{
		Optional<Direction> ret = Optional.empty();
		for(Direction d : Direction.BY_HORIZONTAL_INDEX)
			if(isAttacheablePost(transformerPos.offset(d), world))
			{
				ret = Optional.of(d);
				if(d==preferred)
					break;
			}
		return ret;
	}

	public static boolean isAttacheablePost(BlockPos possiblePost, IBlockReader w)
	{
		BlockState postState = w.getBlockState(possiblePost);
		if(!(postState.getBlock() instanceof IPostBlock))
			return false;
		IPostBlock post = (IPostBlock)postState.getBlock();
		return post.canConnectTransformer(w, possiblePost);
	}
}
