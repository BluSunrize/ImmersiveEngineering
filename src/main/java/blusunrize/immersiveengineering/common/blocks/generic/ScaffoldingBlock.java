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
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class ScaffoldingBlock extends IEBaseBlock.IELadderBlock
{
	private static final VoxelShape COLLISION_SHAPE = makeCuboidShape(1, 0, 1, 15, 16, 15);

	public ScaffoldingBlock(String name, Properties material)
	{
		super(name, material, BlockItemIE::new);
		setNotNormalBlock();
		setBlockLayer(BlockRenderLayer.CUTOUT);
		lightOpacity = 0;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return VoxelShapes.fullCube();
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return COLLISION_SHAPE;
	}

	@Override
	public boolean isSideInvisible(BlockState state, BlockState adjState, Direction side)
	{
		return (adjState.getBlock() instanceof ScaffoldingBlock&&adjState.getMaterial()==state.getMaterial())
				||super.isSideInvisible(state, adjState, side);
	}
}
