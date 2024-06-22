/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

public class MetalLadderBlock extends LadderBlock
{
	private static final Map<Direction, VoxelShape> FRAMES = new EnumMap<>(Direction.class);
	private static final Map<Direction, VoxelShape> FRAMES_OPEN = new EnumMap<>(Direction.class);

	static
	{
		for(Direction dir : DirectionUtils.BY_HORIZONTAL_INDEX)
		{
			VoxelShape forDir = Shapes.empty();
			if(dir!=Direction.NORTH)
				forDir = merge(forDir, new AABB(0, 0, .9375, 1, 1, 1));
			if(dir!=Direction.EAST)
				forDir = merge(forDir, new AABB(0, 0, 0, .0625, 1, 1));
			if(dir!=Direction.SOUTH)
				forDir = merge(forDir, new AABB(0, 0, 0, 1, 1, .0625));
			if(dir!=Direction.WEST)
				forDir = merge(forDir, new AABB(.9375, 0, 0, 1, 1, 1));
			FRAMES.put(dir, forDir);
		}
		for(Direction dir : DirectionUtils.BY_HORIZONTAL_INDEX)
		{
			VoxelShape forDir = Shapes.empty();
			if(dir.getAxis()==Axis.Z)
			{
				forDir = merge(forDir, new AABB(0, 0, .9375, 1, 1, 1));
				forDir = merge(forDir, new AABB(.9375, 0, 0, 1, 1, 1));
			}
			else
			{
				forDir = merge(forDir, new AABB(0, 0, 0, .0625, 1, 1));
				forDir = merge(forDir, new AABB(0, 0, 0, 1, 1, .0625));
			}
			FRAMES_OPEN.put(dir, forDir);
		}
	}

	private static VoxelShape merge(VoxelShape a, AABB b)
	{
		return Shapes.joinUnoptimized(a, Shapes.create(b), BooleanOp.OR);
	}

	private final CoverType type;

	public MetalLadderBlock(CoverType type, Properties props)
	{
		super(props);
		this.type = type;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx)
	{
		VoxelShape base = super.getShape(state, world, pos, ctx);
		if(type==CoverType.NONE)
			return base;
		else
		{
			Direction ladderSide = state.getValue(LadderBlock.FACING);
			return state.getValue(IEProperties.ACTIVE) ? Shapes.joinUnoptimized(base, FRAMES_OPEN.get(ladderSide), BooleanOp.OR) : Shapes.joinUnoptimized(base, FRAMES.get(ladderSide), BooleanOp.OR);
		}
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx)
	{
		BlockState baseState = super.getStateForPlacement(ctx);
		if(baseState==null) return baseState;
		baseState = baseState.setValue(IEProperties.ACTIVE, false);
		if(type==CoverType.NONE)
			return baseState;
		else
			return baseState.setValue(LadderBlock.FACING, Direction.fromYRot(ctx.getRotation()).getOpposite());
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
	{
		if(type==CoverType.NONE)
			return super.canSurvive(state, world, pos);
		else
			return true;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> state) {
		state.add(FACING, WATERLOGGED, IEProperties.ACTIVE);
	}

	@Override
	@SuppressWarnings("deprecation")
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
	{
		ItemStack activeStack = player.getItemInHand(hand);
		if(type!=CoverType.NONE&&activeStack.is(IETags.hammers))
		{
			System.out.println(state.getValue(IEProperties.ACTIVE));
			boolean b = world.setBlockAndUpdate(pos, state.setValue(IEProperties.ACTIVE, !state.getValue(IEProperties.ACTIVE)));
			if(b)
				return InteractionResult.SUCCESS;
			else
				return InteractionResult.FAIL;
		}
		else
			return super.use(state, world, pos, player, hand, hit);
	}

	public enum CoverType
	{
		NONE,
		ALU,
		STEEL;
	}
}