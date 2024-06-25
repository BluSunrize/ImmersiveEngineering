/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Unit;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Locale;

public class WarningSignBlock extends IEBaseBlock
{
	private final WarningSignIcon icon;

	public WarningSignBlock(WarningSignIcon icon, Properties properties)
	{
		super(properties);
		this.icon = icon;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(IEProperties.FACING_HORIZONTAL, BlockStateProperties.WATERLOGGED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		return super.getStateForPlacement(context).setValue(IEProperties.FACING_HORIZONTAL, context.getHorizontalDirection());
	}

	private static final CachedShapesWithTransform<Unit, Direction> SHAPES = CachedShapesWithTransform.createDirectional(
			$ -> ImmutableList.of(new AABB(.0625, .0625, 0, .9375, .9375, .0625))
	);

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos blockPos, CollisionContext context)
	{
		return SHAPES.get(Unit.INSTANCE, state.getValue(IEProperties.FACING_HORIZONTAL));
	}


	public enum WarningSignIcon implements StringRepresentable
	{
		ATTENTION,
		MAGNET,
		COLD,
		ELECTRIC,
		HOT,
		FIRE,
		FALLING,
		SOUND,
		EAR_DEFENDERS,
		CAT,
		VILLAGER,
		TURRET,
		CREEPER,
		SHRIEKER,
		WARDEN,
		ARROW_UP,
		ARROW_DOWN,
		ARROW_LEFT,
		ARROW_RIGHT,
		ARROW_DOUBLE_UP,
		ARROW_DOUBLE_DOWN,
		ARROW_DOUBLE_LEFT,
		ARROW_DOUBLE_RIGHT;

		@Override
		public String getSerializedName()
		{
			return name().toLowerCase(Locale.ENGLISH);
		}
	}
}
