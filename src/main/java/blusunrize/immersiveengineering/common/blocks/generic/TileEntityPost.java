/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.IPostBlock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;

import static net.minecraft.block.state.BlockFaceShape.*;

public class TileEntityPost extends TileEntityIEBase implements IPostBlock, IFaceShape, IHasDummyBlocks, IHasObjProperty, IBlockBounds, IHammerInteraction
{
	//TODO replace with blockstate property
	public byte dummy;

	public static TileEntityType<TileEntityPost> TYPE;

	public TileEntityPost()
	{
		super(TYPE);
	}

	public static boolean _Immovable()
	{
		return true;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		dummy = nbt.getByte("dummy");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setByte("dummy", dummy);
	}

	@OnlyIn(Dist.CLIENT)
	private AxisAlignedBB renderAABB;

	@OnlyIn(Dist.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		return new AxisAlignedBB(getPos().add(-1, 0, -1), getPos().add(1, 4, 1));
	}

	static ArrayList<String> emptyDisplayList = new ArrayList<>();

	@Override
	public ArrayList<String> compileDisplayList()
	{
		if(dummy!=0)
			return emptyDisplayList;
		ArrayList<String> list = new ArrayList<>();
		list.add("base");
		TileEntity te;
		for(int i = 0; i <= 2; i++)
		{
			te = world.getTileEntity(getPos().add(0, 1+i, 0));
			if(te instanceof TileEntityPost)//Stacked pieces
			{
				for(EnumFacing f : EnumFacing.BY_HORIZONTAL_INDEX)
					if(((TileEntityPost)te).hasConnection(f))
					{
						if(i==2)//Arms
						{
							TileEntityPost arm = (TileEntityPost)world.getTileEntity(pos.add(0, 1+i, 0).offset(f));
							boolean down = arm.hasConnection(EnumFacing.DOWN);
							if(down)
								list.add("arm_"+f.getName2()+"_down");
							else
								list.add("arm_"+f.getName2()+"_up");
						}
						else//Simple Connectors
							list.add("con_"+i+"_"+f.getName2());
					}
			}
		}
		return list;
	}

	@Override
	public BlockFaceShape getFaceShape(EnumFacing side)
	{
		if(dummy==0)
			return side==EnumFacing.DOWN?BlockFaceShape.CENTER_BIG: BlockFaceShape.UNDEFINED;
		else if(dummy >= 3)
			return (side==EnumFacing.UP||(dummy > 3&&side==EnumFacing.DOWN))?BlockFaceShape.CENTER_BIG: BlockFaceShape.UNDEFINED;
		return BlockFaceShape.CENTER;
	}

	public boolean hasConnection(EnumFacing dir)
	{
		BlockPos pos = getPos().offset(dir);
		if(dummy > 0&&dummy < 3)
		{
			IBlockState state = world.getBlockState(pos);
			//TODO test
			return state.canBeConnectedTo(world, pos, dir.getOpposite());
		}
		else if(dummy==3)
		{
			TileEntity te = world.getTileEntity(pos);
			return (te instanceof TileEntityPost&&((TileEntityPost)te).dummy-3==dir.ordinal());
		}
		else if(dummy > 3)
		{
			if(world.isAirBlock(pos)||dir.getAxis()!=Axis.Y)
				return false;
			IBlockState state = world.getBlockState(pos);
			if(state.getMaterial().isReplaceable())
				return false;
			BlockFaceShape shape = state.getBlockFaceShape(world, pos, dir.getOpposite());
			return shape==SOLID||shape==CENTER_SMALL||shape==CENTER_BIG||shape==CENTER;
		}
		return false;
	}

	@Override
	public float[] getBlockBounds()
	{
		if(dummy==0)
			return new float[]{.25f, 0, .25f, .75f, 1, .75f};
		if(dummy <= 2)
			return new float[]{hasConnection(EnumFacing.WEST)?0: .375f, 0, hasConnection(EnumFacing.NORTH)?0: .375f, hasConnection(EnumFacing.EAST)?1: .625f, 1, hasConnection(EnumFacing.SOUTH)?1: .625f};
		if(dummy==3)
			return new float[]{hasConnection(EnumFacing.WEST)?0: .3125f, 0, hasConnection(EnumFacing.NORTH)?0: .3125f, hasConnection(EnumFacing.EAST)?1: .6875f, 1, hasConnection(EnumFacing.SOUTH)?1: .6875f};

		float down = hasConnection(EnumFacing.DOWN)?0: .4375f;
		float up = down > 0?1: .5625f;
		if(dummy-3==2)
			return new float[]{.3125f, down, .3125f, .6875f, up, 1};
		if(dummy-3==3)
			return new float[]{.3125f, down, 0, .6875f, up, .6875f};
		if(dummy-3==4)
			return new float[]{.3125f, down, .3125f, 1, up, .6875f};
		if(dummy-3==5)
			return new float[]{0, down, .3125f, .6875f, up, .6875f};
		return null;
	}

	@Override
	public boolean isDummy()
	{
		return dummy!=0;
	}

	@Override
	public void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		for(int i = 1; i <= 3; i++)
		{
			world.setBlockState(pos.add(0, i, 0), state);
			((TileEntityPost)world.getTileEntity(pos.add(0, i, 0))).dummy = (byte)i;
			world.addBlockEvent(pos.add(0, i, 0), getBlockState().getBlock(),
					255, 0);
		}
	}

	@Override
	public void breakDummies(BlockPos pos, IBlockState state)
	{
		if(dummy <= 3)
			for(int i = 0; i <= 3; i++)
			{
				if(world.getTileEntity(getPos().add(0, -dummy, 0).add(0, i, 0)) instanceof TileEntityPost)
					world.removeBlock(getPos().add(0, -dummy, 0).add(0, i, 0));
				if(i==3)
				{
					TileEntity te;
					for(EnumFacing facing : EnumFacing.BY_HORIZONTAL_INDEX)
					{
						te = world.getTileEntity(getPos().add(0, -dummy, 0).add(0, i, 0).offset(facing));
						if(te instanceof TileEntityPost&&((TileEntityPost)te).dummy==(3+facing.ordinal()))
							world.removeBlock(getPos().add(0, -dummy, 0).add(0, i, 0).offset(facing));
					}
				}
			}
	}

	@Override
	public boolean hammerUseSide(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ)
	{
		if(this.dummy==3&&side.getAxis()!=Axis.Y)
		{
			BlockPos offsetPos = getPos().offset(side);
			//No Arms if space is blocked
			if(!world.isAirBlock(offsetPos))
				return false;
			//No Arms if perpendicular arms exist
			TileEntity perpendicular = world.getTileEntity(getPos().offset(side.rotateY()));
			if(perpendicular instanceof TileEntityPost&&((TileEntityPost)perpendicular).dummy-3==side.rotateY().ordinal())
				return false;
			perpendicular = world.getTileEntity(getPos().offset(side.rotateYCCW()));
			if(perpendicular instanceof TileEntityPost&&((TileEntityPost)perpendicular).dummy-3==side.rotateYCCW().ordinal())
				return false;

			world.setBlockState(offsetPos, world.getBlockState(getPos()));
			((TileEntityPost)world.getTileEntity(offsetPos)).dummy = (byte)(3+side.ordinal());
			this.markBlockForUpdate(offsetPos, null);
			this.markBlockForUpdate(getPos().add(0, -3, 0), null);
		}
		else if(this.dummy > 3)
		{
			EnumFacing f = EnumFacing.byIndex(dummy-3).getOpposite();
			this.world.removeBlock(getPos());
			this.markBlockForUpdate(getPos().offset(f).add(0, -3, 0), null);
		}
		return false;
	}

	@Override
	public boolean canConnectTransformer(IBlockReader world, BlockPos pos)
	{
		return this.dummy > 0&&this.dummy <= 3;
	}
}
