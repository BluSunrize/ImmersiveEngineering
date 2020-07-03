/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.api.IEProperties;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

public class ShaderBannerWallBlock extends ShaderBannerBlock
{
	public static final Property<Direction> FACING = IEProperties.FACING_HORIZONTAL;

	public ShaderBannerWallBlock()
	{
		super("shader_banner_wall", FACING);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot)
	{
		Direction newFacing = rot.rotate(state.get(FACING));
		return state.with(FACING, newFacing);
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn)
	{
		if(mirrorIn==Mirror.NONE)
			return state;
		Direction oldFacing = state.get(FACING);
		Direction newFacing = mirrorIn.mirror(oldFacing);
		return state.with(FACING, newFacing);
	}
}
