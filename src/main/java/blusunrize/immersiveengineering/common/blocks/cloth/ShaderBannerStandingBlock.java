/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class ShaderBannerStandingBlock extends ShaderBannerBlock
{
	public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;

	public ShaderBannerStandingBlock()
	{
		super("shader_banner");
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(ROTATION, BlockStateProperties.WATERLOGGED);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot)
	{
		int newRotation = rot.rotate(state.getValue(ROTATION), 16);
		return state.setValue(ROTATION, newRotation);
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn)
	{
		if(mirrorIn==Mirror.NONE)
			return state;
		int newRotation = mirrorIn.mirror(state.getValue(ROTATION), 16);
		return state.setValue(ROTATION, newRotation);
	}
}