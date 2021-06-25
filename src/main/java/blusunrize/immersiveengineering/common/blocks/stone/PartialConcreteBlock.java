/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class PartialConcreteBlock extends IEBaseBlock
{
	private final VoxelShape shape;

	public PartialConcreteBlock(AbstractBlock.Properties properties, int pixels)
	{
		super(properties);
		shape = VoxelShapes.create(0, 0, 0, 1, pixels/16F, 1);
	}

	public static Block.Properties makeProperties()
	{
		return Block.Properties.create(Material.ROCK).hardnessAndResistance(2, 20).notSolid();
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return shape;
	}
}
