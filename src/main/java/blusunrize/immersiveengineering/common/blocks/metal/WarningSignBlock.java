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
import net.minecraft.stats.Stats;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Locale;
import java.util.function.Supplier;

public class WarningSignBlock extends IEBaseBlock
{
	private static final BooleanProperty GLOWING = BooleanProperty.create("glowing");
	public static final Supplier<Properties> PROPERTIES = () -> Block.Properties.of()
			.mapColor(MapColor.METAL)
			.sound(SoundType.METAL)
			.strength(3, 15)
			.requiresCorrectToolForDrops()
			.isViewBlocking((state, blockReader, pos) -> false)
			.lightLevel(b -> b.getValue(GLOWING)?9: 0);

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
		builder.add(IEProperties.FACING_HORIZONTAL, BlockStateProperties.WATERLOGGED, GLOWING);
	}

	@Override
	protected BlockState getInitDefaultState()
	{
		return super.getInitDefaultState().setValue(GLOWING, false);
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

	@Override
	public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		if(stack.is(Items.GLOW_INK_SAC))
		{
			if(!level.isClientSide())
			{
				player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
				state = state.setValue(GLOWING, true);
				level.setBlock(pos, state, 3);
				level.gameEvent(GameEvent.BLOCK_CHANGE, pos, Context.of(player, state));
				stack.consume(1, player);
			}
			return ItemInteractionResult.SUCCESS;
		}
		return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
	}

	public enum WarningSignIcon implements StringRepresentable
	{
		ATTENTION(false),
		MAGNET(true),
		COLD(true),
		ELECTRIC(true),
		HOT(true),
		FIRE(true),
		FALLING(true),
		SOUND(true),
		EAR_DEFENDERS(true),
		CAT(true),
		VILLAGER(false),
		TURRET(true),
		CREEPER(false),
		SHRIEKER(false),
		WARDEN(true),
		ARROW_UP(false),
		ARROW_DOWN(false),
		ARROW_LEFT(false),
		ARROW_RIGHT(false),
		ARROW_DOUBLE_UP(false),
		ARROW_DOUBLE_DOWN(false),
		ARROW_DOUBLE_LEFT(false),
		ARROW_DOUBLE_RIGHT(false);

		private final boolean hasBanner;

		WarningSignIcon(boolean hasBanner)
		{
			this.hasBanner = hasBanner;
		}

		public boolean hasBanner()
		{
			return hasBanner;
		}

		@Override
		public String getSerializedName()
		{
			return name().toLowerCase(Locale.ENGLISH);
		}
	}
}
