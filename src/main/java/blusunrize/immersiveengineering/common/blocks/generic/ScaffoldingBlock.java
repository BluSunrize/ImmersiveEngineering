/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;

public class ScaffoldingBlock extends IEBaseBlock.IELadderBlock
{
	private static final VoxelShape COLLISION_SHAPE = box(1, 0, 1, 15, 16, 15);
	private static final VoxelShape FULL_SHAPE = Shapes.or(
			box(0, 0, 0, 16, 4, 16),
			box(0, 12, 0, 16, 16, 16)
	);
	public static final VoxelShape CHECK_SHAPE = Shapes.box(0, -20, 0, 1, -19, 1);

	public ScaffoldingBlock(Properties material)
	{
		super(material);
		lightOpacity = 0;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.WATERLOGGED);
	}

	@Nonnull
	@Override
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context)
	{
		return Shapes.block();
	}

	@Nonnull
	@Override
	public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, CollisionContext context)
	{
		// This checks if the entity for the context is above the given shape if the context is actually
		// entity-related, and returns the last parameter otherwise. This is necessary to allow tracks/redstone etc to
		// be placed on top of scaffolding while still making it climbable.
		boolean checkForClimbing = context.isAbove(CHECK_SHAPE, pos, false);
		if(checkForClimbing)
			return COLLISION_SHAPE;
		else
			return FULL_SHAPE;
	}

	@Override
	public boolean skipRendering(@Nonnull BlockState state, BlockState adjState, @Nonnull Direction side)
	{
		// TODO a similar check for vanilla fences was replaced by "inTag(wooden_fence) is same for both". Similar here?
		return (adjState.getBlock() instanceof ScaffoldingBlock/*&&adjState.getMaterial()==state.getMaterial()*/)
				||super.skipRendering(state, adjState, side);
	}
}
