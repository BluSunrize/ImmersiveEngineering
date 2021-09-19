/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PartialConcreteBlock extends IEBaseBlock
{
	private final VoxelShape shape;
	private final boolean full;

	public PartialConcreteBlock(String name, int pixels)
	{
		super(name, forHeight(pixels), BlockItemIE::new);
		shape = Shapes.box(0, 0, 0, 1, pixels/16F, 1);
		full = pixels==16;
	}

	private static Block.Properties forHeight(int pixels)
	{
		return Block.Properties.of(Material.STONE).strength(2, 20).noOcclusion();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		return shape;
	}
}
