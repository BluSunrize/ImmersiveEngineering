/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class BlockBlastFurnaceAdvanced extends BlockStoneMultiblock
{
	public BlockBlastFurnaceAdvanced(String name)
	{
		super(name, TileEntityBlastFurnaceAdvanced.TYPE);
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockReader world, IBlockState state, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityBlastFurnaceAdvanced)
		{
			TileEntityBlastFurnaceAdvanced adv = (TileEntityBlastFurnaceAdvanced)te;
			if(adv.pos==1||adv.pos==4||adv.pos==7||adv.pos==31)
				return BlockFaceShape.SOLID;
			else
				return BlockFaceShape.UNDEFINED;
		}
		return super.getBlockFaceShape(world, state, pos, side);
	}
}
