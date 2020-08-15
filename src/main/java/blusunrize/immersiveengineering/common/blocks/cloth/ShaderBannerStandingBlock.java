/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import net.minecraft.block.BlockState;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

public class ShaderBannerStandingBlock extends ShaderBannerBlock
{
	public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_0_15;

	public ShaderBannerStandingBlock()
	{
		super("shader_banner", ROTATION, BlockStateProperties.WATERLOGGED);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot)
	{
		int newRotation = rot.rotate(state.get(ROTATION), 16);
		return state.with(ROTATION, newRotation);
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn)
	{
		if(mirrorIn==Mirror.NONE)
			return state;
		int newRotation = mirrorIn.mirrorRotation(state.get(ROTATION), 16);
		return state.with(ROTATION, newRotation);
	}
}