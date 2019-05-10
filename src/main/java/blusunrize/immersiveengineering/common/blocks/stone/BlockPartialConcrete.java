/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class BlockPartialConcrete extends BlockIEBase
{
	private final VoxelShape shape;
	private final boolean full;

	public BlockPartialConcrete(String name, int pixels)
	{
		super(name, Block.Properties.create(Material.ROCK).hardnessAndResistance(2, 20),
				ItemBlockIEBase.class);
		shape = VoxelShapes.create(0, 0, 0, 1, pixels/16F, 1);
		full = pixels==16;
	}

	@Override
	public VoxelShape getShape(IBlockState p_196244_1_, IBlockReader p_196244_2_, BlockPos p_196244_3_)
	{
		return shape;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockReader world, IBlockState state, BlockPos pos, EnumFacing side)
	{
		if(side!=EnumFacing.DOWN&&!full)
			return BlockFaceShape.UNDEFINED;
		else
			return BlockFaceShape.SOLID;
	}
}
