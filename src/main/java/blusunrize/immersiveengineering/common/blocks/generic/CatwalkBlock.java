/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.utils.shapes.CachedVoxelShapes;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IColouredBlock;
import blusunrize.immersiveengineering.common.blocks.generic.WallmountBlock.Orientation;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;

import static blusunrize.immersiveengineering.common.blocks.generic.WallmountBlock.Orientation.SIDE_UP;

public class CatwalkBlock extends IEBaseBlock implements IColouredBlock
{
	public static final EnumMap<Direction, BooleanProperty> RAILING_PROPERTIES = new EnumMap<>(Direction.class);

	static
	{
		RAILING_PROPERTIES.put(Direction.NORTH, PipeBlock.NORTH);
		RAILING_PROPERTIES.put(Direction.EAST, PipeBlock.EAST);
		RAILING_PROPERTIES.put(Direction.SOUTH, PipeBlock.SOUTH);
		RAILING_PROPERTIES.put(Direction.WEST, PipeBlock.WEST);
	}

	protected static final EnumProperty<DyeColor> DYE_PROPERTY = EnumProperty.create("dye", DyeColor.class);

	protected final boolean isDyeable;

	public CatwalkBlock(Properties blockProps, boolean isDyeable)
	{
		super(blockProps);
		this.isDyeable = isDyeable;
		this.lightOpacity = 0;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		builder.add(DYE_PROPERTY, BlockStateProperties.WATERLOGGED).add(RAILING_PROPERTIES.values().toArray(BooleanProperty[]::new)).add();
	}

	@Override
	protected BlockState getInitDefaultState()
	{
		BlockState ret = super.getInitDefaultState();
		for(BooleanProperty prop : RAILING_PROPERTIES.values())
			ret = ret.setValue(prop, false);
		if(this.isDyeable)
			ret = ret.setValue(DYE_PROPERTY, DyeColor.WHITE);
		return ret;
	}

	@Override
	@SuppressWarnings("deprecation")
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
								 BlockHitResult hit)
	{
		if(this.isDyeable&&Utils.isDye(player.getItemInHand(hand)))
		{
			DyeColor dye = Utils.getDye(player.getItemInHand(hand));
			if(dye!=null)
				world.setBlock(pos, state.setValue(DYE_PROPERTY, dye), 3);
			return InteractionResult.sidedSuccess(world.isClientSide);
		}
		return super.use(state, world, pos, player, hand, hit);
	}


	@Override
	public InteractionResult hammerUseSide(Direction side, Player player, InteractionHand hand, Level w, BlockPos pos, BlockHitResult hit)
	{
		if(player.isShiftKeyDown())
		{
			Direction target;
			Vec3 hitVec = hit.getLocation().subtract(Vec3.atCenterOf(pos));
			if(hitVec.x*hitVec.x > hitVec.z*hitVec.z)
				target = hitVec.x < 0?Direction.WEST: Direction.EAST;
			else
				target = hitVec.z < 0?Direction.NORTH: Direction.SOUTH;
			BooleanProperty prop = RAILING_PROPERTIES.get(target);
			if(prop!=null)
			{
				BlockState state = w.getBlockState(pos);
				w.setBlock(pos, state.setValue(prop, !state.getValue(prop)), 3);
				return InteractionResult.sidedSuccess(w.isClientSide);
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	public boolean hasCustomBlockColours()
	{
		return this.isDyeable;
	}

	@Override
	public int getRenderColour(BlockState state, @Nullable BlockGetter worldIn, @Nullable BlockPos pos, int tintIndex)
	{
		if(this.isDyeable&&tintIndex==1)
			return state.getValue(DYE_PROPERTY).getTextColor();
		return 0xffffff;
	}

	private static final CachedVoxelShapes<RailingsKey> SHAPES = new CachedVoxelShapes<>(railingsKey -> {
		ArrayList<AABB> list = new ArrayList<>();
		// BASE
		list.add(new AABB(0, 0, 0, 1, .125, 1));
		final double height = railingsKey.collision?1.625: 1;
		// SIDES
		if(railingsKey.north)
			list.add(new AABB(0, .125, 0, 1, height, .0625));
		if(railingsKey.south)
			list.add(new AABB(0, .125, .9375, 1, height, 1));
		if(railingsKey.west)
			list.add(new AABB(0, .125, 0, .0625, height, 1));
		if(railingsKey.east)
			list.add(new AABB(.9375, .125, 0, 1, height, 1));
		return list;
	});

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		return SHAPES.get(new RailingsKey(state, false));
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		return SHAPES.get(new RailingsKey(state, true));
	}

	private record RailingsKey(boolean north, boolean east, boolean south, boolean west, boolean collision)
	{
		public RailingsKey(BlockState blockState, boolean collision)
		{
			this(blockState.getValue(PipeBlock.NORTH),
					blockState.getValue(PipeBlock.EAST),
					blockState.getValue(PipeBlock.SOUTH),
					blockState.getValue(PipeBlock.WEST),
					collision);
		}
	}
}
