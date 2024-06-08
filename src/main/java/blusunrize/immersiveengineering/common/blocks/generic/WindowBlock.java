/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.IEProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WindowBlock extends HorizontalFacingBlock
{
	private static final VoxelShape X_FACING = Block.box(6.75, 0, 0, 9.25, 16, 16);
	private static final VoxelShape Z_FACING = Block.box(0, 0, 6.75, 16, 16, 9.25);

	public WindowBlock(Properties blockProps)
	{
		super(blockProps);
		lightOpacity = 0;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		Direction facing = state.getValue(IEProperties.FACING_HORIZONTAL);
		if(facing.getAxis()==Axis.X)
			return X_FACING;
		return Z_FACING;
	}
}
