/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import blusunrize.immersiveengineering.common.blocks.IEEntityBlock;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class DeskBlock<T extends BlockEntity> extends IEEntityBlock<T>
{
	private static final CachedShapesWithTransform<Boolean, Direction> SHAPES = CachedShapesWithTransform.createDirectional(
			b -> {
				double xMin = b?0: 1/16.;
				double xMax = xMin+15/16.;
				return ImmutableList.of(
						new AABB(0, 13/16., 0, 1, 1, 1),
						new AABB(xMin, 0, 1/16., xMax, 13/16., 15/16.)
				);
			}
	);
	public static final Supplier<Properties> PROPERTIES = () -> Properties.of()
			.mapColor(MapColor.WOOD)
			.ignitedByLava()
			.instrument(NoteBlockInstrument.BASS)
			.sound(SoundType.WOOD)
			.strength(2, 5)
			.noOcclusion();
	public static final Property<Direction> FACING = IEProperties.FACING_HORIZONTAL;
	public static final Property<Boolean> DUMMY = IEProperties.MULTIBLOCKSLAVE;

	public DeskBlock(RegistryObject<BlockEntityType<T>> tileType, BlockBehaviour.Properties props)
	{
		super(tileType, props);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(DUMMY, FACING, BlockStateProperties.WATERLOGGED);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot)
	{
		Direction newFacing = rot.rotate(state.getValue(FACING));
		return state.setValue(FACING, newFacing);
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn)
	{
		if(mirrorIn==Mirror.NONE)
			return state;
		Direction oldFacing = state.getValue(FACING);
		Direction newFacing = mirrorIn.mirror(oldFacing);
		boolean oldDummy = state.getValue(DUMMY);
		boolean newDummy = !oldDummy;
		return state.setValue(FACING, newFacing).setValue(DUMMY, newDummy);
	}

	@Override
	public boolean canIEBlockBePlaced(BlockState newState, BlockPlaceContext context)
	{
		BlockPos start = context.getClickedPos();
		Direction tableFacing = PlacementLimitation.HORIZONTAL.getDirectionForPlacement(context);
		Direction dummyDir = DeskBlock.getDeskDummyOffset(context.getLevel(), context.getClickedPos(), tableFacing, context);
		return areAllReplaceable(start, start.relative(dummyDir), context);
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction updateSide, BlockState updatedState,
										  LevelAccessor worldIn, BlockPos currentPos, BlockPos updatedPos)
	{
		Direction facing = stateIn.getValue(FACING);
		boolean dummy = stateIn.getValue(DUMMY);
		{
			// Check if current facing is correct
			BlockPos otherHalf = currentPos.relative(facing.getClockWise(), dummy?-1: 1);
			BlockState otherState = worldIn.getBlockState(otherHalf);
			if(otherState.getBlock()==this&&otherState.getValue(FACING)==facing&&otherState.getValue(DUMMY)==!dummy)
				return stateIn;
		}
		// Find correct facing, or remove
		for(Direction candidate : FACING.getPossibleValues())
			if(candidate!=facing)
			{
				BlockPos otherHalf = currentPos.relative(candidate.getClockWise(), dummy?-1: 1);
				BlockState otherState = worldIn.getBlockState(otherHalf);
				if(otherState.getBlock()==this&&otherState.getValue(FACING)==candidate&&otherState.getValue(DUMMY)==!dummy)
					return stateIn.setValue(FACING, candidate);
			}
		return Blocks.AIR.defaultBlockState();
	}

	public static Direction getDeskDummyOffset(Level world, BlockPos pos, Direction facing, BlockPlaceContext ctx)
	{
		Direction dummyDir;
		if(facing.getAxis()==Axis.X)
			dummyDir = ctx.getClickLocation().z < .5?Direction.NORTH: Direction.SOUTH;
		else
			dummyDir = ctx.getClickLocation().x < .5?Direction.WEST: Direction.EAST;
		BlockPos dummyPos = pos.relative(dummyDir);
		if(!world.getBlockState(dummyPos).canBeReplaced(BlockPlaceContext.at(ctx, dummyPos, dummyDir)))
			dummyDir = dummyDir.getOpposite();
		return dummyDir;
	}

	public static void placeDummies(BlockState state, Level world, BlockPos pos, BlockPlaceContext ctx)
	{
		Direction facing = state.getValue(FACING);
		Direction dummyDir = DeskBlock.getDeskDummyOffset(world, pos, facing, ctx);
		BlockPos dummyPos = pos.relative(dummyDir);
		boolean mirror = dummyDir!=facing.getClockWise();
		if(mirror)
			world.setBlockAndUpdate(pos, state.setValue(DUMMY, true));
		world.setBlockAndUpdate(dummyPos, IEBaseBlock.applyLocationalWaterlogging(
				state.setValue(IEProperties.MULTIBLOCKSLAVE, !mirror), world, pos
		));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		return SHAPES.get(state.getValue(DUMMY), state.getValue(FACING));
	}
}
