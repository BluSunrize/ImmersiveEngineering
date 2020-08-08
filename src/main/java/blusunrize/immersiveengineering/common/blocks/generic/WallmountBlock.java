/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import java.util.Locale;

import static blusunrize.immersiveengineering.common.blocks.generic.WallmountBlock.Orientation.*;

public class WallmountBlock extends IEBaseBlock
{
	public static final EnumProperty<Orientation> ORIENTATION =
			EnumProperty.create("orientation", Orientation.class);

	public WallmountBlock(String name, Properties blockProps)
	{
		super(name, blockProps, BlockItemIE::new, IEProperties.FACING_HORIZONTAL,
				ORIENTATION, BlockStateProperties.WATERLOGGED);
		setNotNormalBlock();
		lightOpacity = 0;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		BlockState ret = super.getStateForPlacement(context);
		if(ret==null)
			return null;
		Direction side = context.getFace();
		Direction facing = Direction.fromAngle(context.getPlacementYaw());
		if(side.getAxis()==Axis.Y)
			facing = facing.getOpposite();
		if(side==Direction.UP)
			ret = ret.with(ORIENTATION, Orientation.VERT_UP);
		else if(side==Direction.DOWN)
			ret = ret.with(ORIENTATION, Orientation.VERT_DOWN);
		else if(context.getHitVec().y-context.getPos().getY() < .5)
			ret = ret.with(ORIENTATION, Orientation.SIDE_DOWN);
		else
			ret = ret.with(ORIENTATION, SIDE_UP);
		ret = ret.with(IEProperties.FACING_HORIZONTAL, facing);
		return ret;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context)
	{
		Orientation orientation = state.get(ORIENTATION);
		Direction facing = state.get(IEProperties.FACING_HORIZONTAL);
		Direction towards = orientation.attachedToSide()?facing: facing.getOpposite();
		double minX = towards==Direction.WEST?0: .3125f;
		double minY = orientation==SIDE_UP?.375f: orientation==VERT_DOWN?.3125f: 0;
		double minZ = towards==Direction.NORTH?0: .3125f;
		double maxX = towards==Direction.EAST?1: .6875f;
		double maxY = orientation==SIDE_DOWN?.625f: orientation==VERT_UP?.6875f: 1;
		double maxZ = towards==Direction.SOUTH?1: .6875f;
		return VoxelShapes.create(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public boolean hammerUseSide(Direction side, PlayerEntity player, Hand hand, World w, BlockPos pos, BlockRayTraceResult hit)
	{
		if(player.isSneaking())
		{
			BlockState state = w.getBlockState(pos);
			Orientation old = state.get(ORIENTATION);
			Orientation newO = old.getDual();
			w.setBlockState(pos, state.with(ORIENTATION, newO));
			return true;
		}
		return false;
	}

	@Override
	public boolean canBeConnectedTo(BlockState state, IBlockReader world, BlockPos pos, Direction fromSide)
	{
		Orientation o = state.get(ORIENTATION);
		if(fromSide==Direction.UP)
			return o.touchesTop();
		else if(fromSide==Direction.DOWN)
			return !o.touchesTop();
		else
		{
			Direction mountSide = state.get(IEProperties.FACING_HORIZONTAL);
			Direction actualSide = o.attachedToSide()?mountSide: mountSide.getOpposite();
			return fromSide==actualSide;
		}
	}

	//ordinal matches <=1.12 value
	public enum Orientation implements IStringSerializable
	{
		//Attached to the side, other "plate" on the top/bottom
		SIDE_UP,
		SIDE_DOWN,
		//Attached to the top/bottom, other "plate" on the side
		VERT_DOWN,
		VERT_UP;

		@Override
		public String getName()
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
			return getName();
		}
	}
}
