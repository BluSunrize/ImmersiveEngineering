/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import net.minecraft.block.BlockState;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class BlockBlastFurnaceAdvanced extends BlockStoneMultiblock
{
	public BlockBlastFurnaceAdvanced(String name)
	{
		super(name, TileEntityBlastFurnaceAdvanced.TYPE);
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockReader world, BlockState state, BlockPos pos, Direction side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityBlastFurnaceAdvanced)
		{
			TileEntityBlastFurnaceAdvanced adv = (TileEntityBlastFurnaceAdvanced)te;
			if(adv.posInMultiblock==1||adv.posInMultiblock==4||adv.posInMultiblock==7||adv.posInMultiblock==31)
				return BlockFaceShape.SOLID;
			else
				return BlockFaceShape.UNDEFINED;
		}
		return super.getBlockFaceShape(world, state, pos, side);
	}
}
