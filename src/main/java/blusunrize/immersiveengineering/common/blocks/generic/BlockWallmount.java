/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import static blusunrize.immersiveengineering.common.blocks.generic.BlockWallmount.Orientation.*;

public class BlockWallmount extends BlockIEBase
{
	private static final EnumProperty<Orientation> ORIENTATION =
			EnumProperty.create("orientation", Orientation.class);

	public BlockWallmount(String name, Properties blockProps)
	{
		super(name, blockProps, ItemBlockIEBase.class, IEProperties.FACING_HORIZONTAL,
				ORIENTATION);
		setNotNormalBlock();
		lightOpacity = 0;
	}

	@Nullable
	@Override
	public IBlockState getStateForPlacement(BlockItemUseContext context)
	{
		IBlockState ret = super.getStateForPlacement(context);
		if(ret==null)
			return null;
		EnumFacing side = context.getFace();
		if(side==EnumFacing.UP)
			ret = ret.with(ORIENTATION, Orientation.VERT_UP);
		else if(side==EnumFacing.DOWN)
			ret = ret.with(ORIENTATION, Orientation.VERT_DOWN);
		else if(context.getHitY() < .5)
			ret = ret.with(ORIENTATION, Orientation.SIDE_DOWN);
		else
			ret = ret.with(ORIENTATION, SIDE_UP);
		return ret;
	}

	@Override
	public VoxelShape getShape(IBlockState state, IBlockReader world, BlockPos pos)
	{
		Orientation orientation = state.get(ORIENTATION);
		EnumFacing facing = state.get(IEProperties.FACING_HORIZONTAL);
		EnumFacing towards = orientation.attachedToSide()?facing: facing.getOpposite();
		double minX = towards==EnumFacing.WEST?0: .3125f;
		double minY = orientation==SIDE_UP?.375f: orientation==VERT_UP?.3125f: 0;
		double minZ = towards==EnumFacing.NORTH?0: .3125f;
		double maxX = towards==EnumFacing.EAST?1: .6875f;
		double maxY = orientation==SIDE_DOWN?.625f: orientation==VERT_DOWN?.6875f: 1;
		double maxZ = towards==EnumFacing.SOUTH?1: .6875f;
		return VoxelShapes.create(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public boolean hammerUseSide(EnumFacing side, EntityPlayer player, World w, BlockPos pos, float hitX, float hitY, float hitZ)
	{
		if(player.isSneaking())
		{
			IBlockState state = w.getBlockState(pos);
			Orientation old = state.get(ORIENTATION);
			Orientation newO = old.getDual();
			w.setBlockState(pos, state.with(ORIENTATION, newO));
			return true;
		}
		return false;
	}

	@Override
	public boolean canBeConnectedTo(IBlockState state, IBlockReader world, BlockPos pos, EnumFacing fromSide)
	{
		Orientation o = state.get(ORIENTATION);
		if(fromSide==EnumFacing.UP)
			return o.touchesTop();
		else if(fromSide==EnumFacing.DOWN)
			return !o.touchesTop();
		else
		{
			EnumFacing mountSide = state.get(IEProperties.FACING_HORIZONTAL);
			EnumFacing actualSide = o.attachedToSide()?mountSide: mountSide.getOpposite();
			return fromSide==actualSide;
		}
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockReader w, IBlockState state, BlockPos pos, EnumFacing side)
	{
		Orientation o = state.get(ORIENTATION);
		if(side==EnumFacing.UP)
			return o.touchesTop()?BlockFaceShape.CENTER: BlockFaceShape.UNDEFINED;
		else if(side==EnumFacing.DOWN)
			return o.touchesTop()?BlockFaceShape.UNDEFINED: BlockFaceShape.CENTER;
		else
		{
			EnumFacing mountSide = state.get(IEProperties.FACING_HORIZONTAL);
			EnumFacing actualSide = o.attachedToSide()?mountSide: mountSide.getOpposite();
			return side==actualSide?BlockFaceShape.CENTER: BlockFaceShape.UNDEFINED;
		}
	}

	enum Orientation implements IStringSerializable
	{
		//Attached to the side, other "plate" on the top/bottom
		SIDE_UP,
		SIDE_DOWN,
		//Attached to the top/bottom, other "plate" on the side
		VERT_UP,
		VERT_DOWN;

		@Override
		public String getName()
		{
			return name();
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
	}
}
