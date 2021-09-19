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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;

public class SawdustBlock extends IEBaseBlock
{
	protected static final int MAX_LAYER = 9;
	public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, MAX_LAYER);
	protected static final CachedVoxelShapes<Integer> SHAPES = new CachedVoxelShapes<>(
			layer -> {
				if(layer==0) // First layer
					return null;
				if(layer==MAX_LAYER) // Full block
					return ImmutableList.of(new AABB(0, 0, 0, 1, 1, 1));
				return ImmutableList.of(new AABB(0, 0, 0, 1, 0.0625+0.125*(layer-1), 1));
			}
	);

	public SawdustBlock()
	{
		super("sawdust",
				Block.Properties.of(Material.WOOD, MaterialColor.SAND).sound(SoundType.SAND)
						.harvestTool(ToolType.SHOVEL).strength(0.5F).noCollission().noOcclusion(),
				BlockItemIE::new);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(LAYERS);
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face)
	{
		return 60;
	}

	@Override
	public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face)
	{
		return 60;
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type)
	{
		if(type==PathComputationType.LAND)
			return state.getValue(LAYERS) < 5;
		return false;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		return SHAPES.get(state.getValue(LAYERS));
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		return SHAPES.get(state.getValue(LAYERS)-1);
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state)
	{
		return true;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos)
	{
		BlockState blockstate = worldIn.getBlockState(pos.below());
		Block block = blockstate.getBlock();
		return Block.isFaceFull(blockstate.getCollisionShape(worldIn, pos.below()), Direction.UP)
				||block==this&&blockstate.getValue(LAYERS)==MAX_LAYER;
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos)
	{
		return !stateIn.canSurvive(worldIn, currentPos)?Blocks.AIR.defaultBlockState(): super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext)
	{
		int i = state.getValue(LAYERS);
		if(useContext.getItemInHand().getItem()==this.asItem()&&i < MAX_LAYER)
		{
			if(useContext.replacingClickedOnBlock())
				return useContext.getClickedFace()==Direction.UP;
			else
				return true;
		}
		else
			return i==1;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
		if(blockstate.getBlock()==this)
			return blockstate.setValue(LAYERS, Math.min(MAX_LAYER, blockstate.getValue(LAYERS)+1));
		else
			return super.getStateForPlacement(context);
	}
}
