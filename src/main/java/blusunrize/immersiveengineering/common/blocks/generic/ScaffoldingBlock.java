/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;

public class ScaffoldingBlock extends IEBaseBlock.IELadderBlock
{
	private static final VoxelShape COLLISION_SHAPE = makeCuboidShape(1, 0, 1, 15, 16, 15);
	private static final VoxelShape FULL_SHAPE = VoxelShapes.or(
			makeCuboidShape(0, 0, 0, 16, 4, 16),
			makeCuboidShape(0, 12, 0, 16, 16, 16)
	);
	public static final VoxelShape CHECK_SHAPE = VoxelShapes.create(0, -20, 0, 1, -19, 1);

	public ScaffoldingBlock(String name, Properties material)
	{
		super(name, material, BlockItemIE::new, BlockStateProperties.WATERLOGGED);
		setNotNormalBlock();
		setBlockLayer(BlockRenderLayer.CUTOUT);
		lightOpacity = 0;
	}

	@Nonnull
	@Override
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos, @Nonnull ISelectionContext context)
	{
		return VoxelShapes.fullCube();
	}

	@Nonnull
	@Override
	public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos, ISelectionContext context)
	{
		// This checks if the entity for the context is above the given shape if the context is actually
		// entity-related, and returns the last parameter otherwise. This is necessary to allow tracks/redstone etc to
		// be placed on top of scaffolding while still making it climbable.
		boolean checkForClimbing = context.func_216378_a(CHECK_SHAPE, pos, false);
		if(checkForClimbing)
			return COLLISION_SHAPE;
		else
			return FULL_SHAPE;
	}

	@Override
	public boolean isSideInvisible(@Nonnull BlockState state, BlockState adjState, @Nonnull Direction side)
	{
		return (adjState.getBlock() instanceof ScaffoldingBlock&&adjState.getMaterial()==state.getMaterial())
				||super.isSideInvisible(state, adjState, side);
	}
}
