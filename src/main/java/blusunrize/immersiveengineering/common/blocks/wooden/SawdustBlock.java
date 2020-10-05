/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.utils.shapes.CachedVoxelShapes;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;

public class SawdustBlock extends IEBaseBlock
{
	public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS_1_8;

	protected static final CachedVoxelShapes<Integer> SHAPES = new CachedVoxelShapes<>(
			layer -> layer==0?null: ImmutableList.of(new AxisAlignedBB(0, 0, 0, 1, 0.125*layer, 1))
	);

	public SawdustBlock()
	{
		super("sawdust",
				Block.Properties.create(Material.WOOD, MaterialColor.SAND).sound(SoundType.SAND)
						.harvestTool(ToolType.SHOVEL).hardnessAndResistance(0.5F).doesNotBlockMovement().notSolid(),
				BlockItemIE::new, LAYERS);
	}

	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type)
	{
		if(type==PathType.LAND)
			return state.get(LAYERS) < 5;
		return false;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return SHAPES.get(state.get(LAYERS));
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return SHAPES.get(state.get(LAYERS)-1);
	}

	@Override
	public boolean isTransparent(BlockState state)
	{
		return true;
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos)
	{
		BlockState blockstate = worldIn.getBlockState(pos.down());
		Block block = blockstate.getBlock();
		return Block.doesSideFillSquare(blockstate.getCollisionShape(worldIn, pos.down()), Direction.UP)
				||block==this&&blockstate.get(LAYERS)==8;
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
	{
		return !stateIn.isValidPosition(worldIn, currentPos)?Blocks.AIR.getDefaultState(): super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	public boolean isReplaceable(BlockState state, BlockItemUseContext useContext)
	{
		int i = state.get(LAYERS);
		if(useContext.getItem().getItem()==this.asItem()&&i < 8)
		{
			if(useContext.replacingClickedOnBlock())
				return useContext.getFace()==Direction.UP;
			else
				return true;
		}
		else
			return i==1;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		BlockState blockstate = context.getWorld().getBlockState(context.getPos());
		if(blockstate.getBlock()==this)
			return blockstate.with(LAYERS, Math.min(8, blockstate.get(LAYERS)+1));
		else
			return super.getStateForPlacement(context);
	}
}
