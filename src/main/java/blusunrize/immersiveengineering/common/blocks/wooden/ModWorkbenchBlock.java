/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModWorkbenchBlock extends IETileProviderBlock
{
	public static final IProperty<Direction> FACING = IEProperties.FACING_HORIZONTAL;
	public static final IProperty<Boolean> DUMMY = IEProperties.MULTIBLOCKSLAVE;

	public ModWorkbenchBlock(String name)
	{
		super(name, Block.Properties.create(Material.WOOD).sound(SoundType.WOOD).hardnessAndResistance(2, 5), BlockItemIE::new,
				DUMMY, FACING);
		setNotNormalBlock();
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world)
	{
		return new ModWorkbenchTileEntity();
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
		boolean oldDummy = state.get(DUMMY);
		boolean newDummy = !oldDummy;
		return state.with(FACING, newFacing).with(DUMMY, newDummy);
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction updateSide, BlockState updatedState,
										  IWorld worldIn, BlockPos currentPos, BlockPos updatedPos)
	{
		Direction facing = stateIn.get(FACING);
		boolean dummy = stateIn.get(DUMMY);
		{
			// Check if current facing is correct
			BlockPos otherHalf = currentPos.offset(facing.rotateY(), dummy?-1: 1);
			BlockState otherState = worldIn.getBlockState(otherHalf);
			if(otherState.getBlock()==this&&otherState.get(FACING)==facing&&otherState.get(DUMMY)==!dummy)
				return stateIn;
		}
		// Find correct facing, or remove
		for(Direction candidate : FACING.getAllowedValues())
			if(candidate!=facing)
			{
				BlockPos otherHalf = currentPos.offset(candidate.rotateY(), dummy?-1: 1);
				BlockState otherState = worldIn.getBlockState(otherHalf);
				if(otherState.getBlock()==this&&otherState.get(FACING)==candidate&&otherState.get(DUMMY)==!dummy)
					return stateIn.with(FACING, candidate);
			}
		return Blocks.AIR.getDefaultState();
	}
}
