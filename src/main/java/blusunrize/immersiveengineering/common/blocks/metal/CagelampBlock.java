/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Supplier;

public class CagelampBlock extends IEBaseBlock
{
	public static final Supplier<Properties> PROPERTIES = () -> Properties.of()
			.mapColor(MapColor.METAL)
			.sound(SoundType.METAL)
			.strength(3, 15)
			.lightLevel(b -> b.getValue(IEProperties.ACTIVE)?14: 0)
			.noOcclusion();

	public CagelampBlock(Properties props)
	{
		super(props);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		BlockState state = super.getStateForPlacement(context);
		if(state==null)
			return null;
		state = state.setValue(IEProperties.FACING_ALL, context.getClickedFace())
				.setValue(IEProperties.INVERTED, false)
				.setValue(IEProperties.ACTIVE, false);
		return state;
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
	{
		if(level instanceof ServerLevel serverlevel)
			this.updateActiveState(blockState, serverlevel, pos);
	}

	@Override
	public void onIEBlockPlacedBy(BlockPlaceContext context, BlockState state)
	{
		if(context.getLevel() instanceof ServerLevel serverlevel)
			this.updateActiveState(state, serverlevel, context.getClickedPos());
	}

	@Override
	public InteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Level level, BlockPos pos, BlockHitResult hit)
	{
		if(level instanceof ServerLevel serverlevel)
		{
			BlockState state = level.getBlockState(pos);
			updateActiveState(state.cycle(IEProperties.INVERTED), serverlevel, pos);
		}
		return InteractionResult.sidedSuccess(level.isClientSide());
	}

	public void updateActiveState(BlockState state, ServerLevel level, BlockPos pos)
	{
		boolean rsSignal = level.hasNeighborSignal(pos);
		boolean active = rsSignal^state.getValue(IEProperties.INVERTED);

		if(active!=state.getValue(IEProperties.ACTIVE))
		{
			state = state.cycle(IEProperties.ACTIVE);
			level.playSound(
					null, pos, active?SoundEvents.COPPER_BULB_TURN_ON: SoundEvents.COPPER_BULB_TURN_OFF, SoundSource.BLOCKS
			);
			level.setBlock(pos, state, 3);
		}
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(IEProperties.FACING_ALL, BlockStateProperties.WATERLOGGED, IEProperties.ACTIVE, IEProperties.INVERTED);
	}

	private static final Map<Direction, VoxelShape> SHAPES = ImmutableMap.<Direction, VoxelShape>builder()
			.put(Direction.DOWN, Shapes.box(.1875, .3125, .1875, .8125, 1, .8125))
			.put(Direction.UP, Shapes.box(.1875, 0, .1875, .8125, .6875, .8125))
			.put(Direction.NORTH, Shapes.box(.1875, .1875, .3125, .8125, .8125, 1))
			.put(Direction.SOUTH, Shapes.box(.1875, .1875, 0, .8125, .8125, .6875))
			.put(Direction.WEST, Shapes.box(.3125, .1875, .1875, 1, .8125, .8125))
			.put(Direction.EAST, Shapes.box(0, .1875, .1875, .6875, .8125, .8125))
			.build();

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		return SHAPES.get(state.getValue(IEProperties.FACING_ALL));
	}

}
