/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.generic.AnyFacingEntityBlock;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;

public class BlueprintShelfBlock extends AnyFacingEntityBlock<BlueprintShelfBlockEntity>
{
	public static final BooleanProperty[] BLUEPRINT_SLOT_FILLED = new BooleanProperty[9];
	private static final EnumMap<Direction, VoxelShape> SHAPES = new EnumMap<Direction, VoxelShape>(Direction.class);

	static
	{
		for(int i = 0; i < BLUEPRINT_SLOT_FILLED.length; i++)
			BLUEPRINT_SLOT_FILLED[i] = BooleanProperty.create("blueprint_"+i);
		for(Direction d : Direction.values())
			SHAPES.put(d, Block.box(d==Direction.WEST?3: 0, 0, d==Direction.NORTH?3: 0, d==Direction.EAST?13: 16, d==Direction.UP?13: 16, d==Direction.SOUTH?13: 16));
	}

	public BlueprintShelfBlock(Properties blockProps)
	{
		super(IEBlockEntities.BLUEPRINT_SHELF, blockProps);
		BlockState defaultState = this.stateDefinition.any().setValue(IEProperties.FACING_ALL, Direction.NORTH);
		for(BooleanProperty booleanProperty : BLUEPRINT_SLOT_FILLED)
			defaultState = defaultState.setValue(booleanProperty, false);
		this.registerDefaultState(defaultState);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(BLUEPRINT_SLOT_FILLED);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		return SHAPES.get(state.getValue(IEProperties.FACING_ALL));
	}
}
