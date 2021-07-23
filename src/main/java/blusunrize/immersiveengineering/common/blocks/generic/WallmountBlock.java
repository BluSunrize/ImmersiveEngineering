/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Locale;

import static blusunrize.immersiveengineering.common.blocks.generic.WallmountBlock.Orientation.*;

public class WallmountBlock extends IEBaseBlock
{
	public static final EnumProperty<Orientation> ORIENTATION =
			EnumProperty.create("orientation", Orientation.class);

	public WallmountBlock(Properties blockProps)
	{
		super(blockProps);
		lightOpacity = 0;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(IEProperties.FACING_HORIZONTAL, ORIENTATION, BlockStateProperties.WATERLOGGED);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		BlockState ret = super.getStateForPlacement(context);
		if(ret==null)
			return null;
		Direction side = context.getClickedFace();
		Direction facing = Direction.fromYRot(context.getRotation());
		if(side.getAxis()==Axis.Y)
			facing = facing.getOpposite();
		if(side==Direction.UP)
			ret = ret.setValue(ORIENTATION, Orientation.VERT_UP);
		else if(side==Direction.DOWN)
			ret = ret.setValue(ORIENTATION, Orientation.VERT_DOWN);
		else if(context.getClickLocation().y-context.getClickedPos().getY() < .5)
			ret = ret.setValue(ORIENTATION, Orientation.SIDE_DOWN);
		else
			ret = ret.setValue(ORIENTATION, SIDE_UP);
		ret = ret.setValue(IEProperties.FACING_HORIZONTAL, facing);
		return ret;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		Orientation orientation = state.getValue(ORIENTATION);
		Direction facing = state.getValue(IEProperties.FACING_HORIZONTAL);
		Direction towards = orientation.attachedToSide()?facing: facing.getOpposite();
		double minX = towards==Direction.WEST?0: .3125f;
		double minY = orientation==SIDE_UP?.375f: orientation==VERT_DOWN?.3125f: 0;
		double minZ = towards==Direction.NORTH?0: .3125f;
		double maxX = towards==Direction.EAST?1: .6875f;
		double maxY = orientation==SIDE_DOWN?.625f: orientation==VERT_UP?.6875f: 1;
		double maxZ = towards==Direction.SOUTH?1: .6875f;
		return Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public InteractionResult hammerUseSide(Direction side, Player player, InteractionHand hand, Level w, BlockPos pos, BlockHitResult hit)
	{
		if(player.isShiftKeyDown())
		{
			BlockState state = w.getBlockState(pos);
			Orientation old = state.getValue(ORIENTATION);
			Orientation newO = old.getDual();
			w.setBlockAndUpdate(pos, state.setValue(ORIENTATION, newO));
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	//ordinal matches <=1.12 value
	public enum Orientation implements StringRepresentable
	{
		//Attached to the side, other "plate" on the top/bottom
		SIDE_UP,
		SIDE_DOWN,
		//Attached to the top/bottom, other "plate" on the side
		VERT_DOWN,
		VERT_UP;

		@Override
		public String getSerializedName()
		{
			return name().toLowerCase(Locale.ENGLISH);
		}

		public boolean attachedToSide()
		{
			return this==SIDE_DOWN||this==SIDE_UP;
		}

		public boolean touchesTop()
		{
			return this==SIDE_UP||this==VERT_UP;
		}

		public Orientation getDual()
		{
			switch(this)
			{
				case SIDE_UP:
					return SIDE_DOWN;
				case SIDE_DOWN:
					return SIDE_UP;
				case VERT_UP:
					return VERT_DOWN;
				case VERT_DOWN:
				default:
					return VERT_UP;
			}
		}

		public String modelSuffix()
		{
			switch(this)
			{
				case SIDE_UP:
					return "";
				case SIDE_DOWN:
					return "_inverted";
				case VERT_DOWN:
					return "_sideways";
				case VERT_UP:
				default:
					return "_sideways_inverted";
			}
		}

		@Override
		public String toString()
		{
			return getSerializedName();
		}
	}
}
