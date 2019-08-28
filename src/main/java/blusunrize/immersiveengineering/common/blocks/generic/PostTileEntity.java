/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.IPostBlock;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasObjProperty;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;

public class PostTileEntity extends IEBaseTileEntity implements IPostBlock, IHasDummyBlocks, IHasObjProperty, IBlockBounds, IHammerInteraction
{
	//TODO replace with blockstate property
	public byte dummy;

	public static TileEntityType<PostTileEntity> TYPE;

	public PostTileEntity()
	{
		super(TYPE);
	}

	public static boolean _Immovable()
	{
		return true;
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		dummy = nbt.getByte("dummy");
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.putByte("dummy", dummy);
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
			if(te instanceof PostTileEntity)//Stacked pieces
			{
				for(Direction f : Direction.BY_HORIZONTAL_INDEX)
					if(((PostTileEntity)te).hasConnection(f))
					{
						if(i==2)//Arms
						{
							PostTileEntity arm = (PostTileEntity)world.getTileEntity(pos.add(0, 1+i, 0).offset(f));
							boolean down = arm.hasConnection(Direction.DOWN);
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

	public boolean hasConnection(Direction dir)
	{
		BlockPos pos = getPos().offset(dir);
		if(dummy > 0&&dummy < 3)
		{
			BlockState state = world.getBlockState(pos);
			//TODO test
			return state.canBeConnectedTo(world, pos, dir.getOpposite());
		}
		else if(dummy==3)
		{
			TileEntity te = world.getTileEntity(pos);
			return (te instanceof PostTileEntity&&((PostTileEntity)te).dummy-3==dir.ordinal());
		}
		else if(dummy > 3)
		{
			if(world.isAirBlock(pos)||dir.getAxis()!=Axis.Y)
				return false;
			BlockState state = world.getBlockState(pos);
			if(state.getMaterial().isReplaceable())
				return false;
			VoxelShape shape = state.getShape(world, pos);
			return shapeReachesBlockFace(shape, dir.getOpposite());
		}
		return false;
	}

	private boolean shapeReachesBlockFace(VoxelShape shape, Direction face)
	{
		//TODO is 1 and 0 correct? Or is that 1 pixel/0 pixels?
		if(face.getAxisDirection()==AxisDirection.POSITIVE)
			return shape.getEnd(face.getAxis())==1;
		else
			return shape.getStart(face.getAxis())==0;
	}

	@Override
	public float[] getBlockBounds()
	{
		if(dummy==0)
			return new float[]{.25f, 0, .25f, .75f, 1, .75f};
		if(dummy <= 2)
			return new float[]{hasConnection(Direction.WEST)?0: .375f, 0, hasConnection(Direction.NORTH)?0: .375f, hasConnection(Direction.EAST)?1: .625f, 1, hasConnection(Direction.SOUTH)?1: .625f};
		if(dummy==3)
			return new float[]{hasConnection(Direction.WEST)?0: .3125f, 0, hasConnection(Direction.NORTH)?0: .3125f, hasConnection(Direction.EAST)?1: .6875f, 1, hasConnection(Direction.SOUTH)?1: .6875f};

		float down = hasConnection(Direction.DOWN)?0: .4375f;
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
	public void placeDummies(BlockItemUseContext ctx, BlockState state)
	{
		for(int i = 1; i <= 3; i++)
		{
			world.setBlockState(pos.add(0, i, 0), state);
			((PostTileEntity)world.getTileEntity(pos.add(0, i, 0))).dummy = (byte)i;
			world.addBlockEvent(pos.add(0, i, 0), getBlockState().getBlock(),
					255, 0);
		}
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		if(dummy <= 3)
			for(int i = 0; i <= 3; i++)
			{
				if(world.getTileEntity(getPos().add(0, -dummy, 0).add(0, i, 0)) instanceof PostTileEntity)
					world.removeBlock(getPos().add(0, -dummy, 0).add(0, i, 0), false);
				if(i==3)
				{
					TileEntity te;
					for(Direction facing : Direction.BY_HORIZONTAL_INDEX)
					{
						te = world.getTileEntity(getPos().add(0, -dummy, 0).add(0, i, 0).offset(facing));
						if(te instanceof PostTileEntity&&((PostTileEntity)te).dummy==(3+facing.ordinal()))
							world.removeBlock(getPos().add(0, -dummy, 0).add(0, i, 0).offset(facing), false);
					}
				}
			}
	}

	@Override
	public boolean hammerUseSide(Direction side, PlayerEntity player, float hitX, float hitY, float hitZ)
	{
		if(this.dummy==3&&side.getAxis()!=Axis.Y)
		{
			BlockPos offsetPos = getPos().offset(side);
			//No Arms if space is blocked
			if(!world.isAirBlock(offsetPos))
				return false;
			//No Arms if perpendicular arms exist
			TileEntity perpendicular = world.getTileEntity(getPos().offset(side.rotateY()));
			if(perpendicular instanceof PostTileEntity&&((PostTileEntity)perpendicular).dummy-3==side.rotateY().ordinal())
				return false;
			perpendicular = world.getTileEntity(getPos().offset(side.rotateYCCW()));
			if(perpendicular instanceof PostTileEntity&&((PostTileEntity)perpendicular).dummy-3==side.rotateYCCW().ordinal())
				return false;

			world.setBlockState(offsetPos, world.getBlockState(getPos()));
			((PostTileEntity)world.getTileEntity(offsetPos)).dummy = (byte)(3+side.ordinal());
			this.markBlockForUpdate(offsetPos, null);
			this.markBlockForUpdate(getPos().add(0, -3, 0), null);
		}
		else if(this.dummy > 3)
		{
			Direction f = Direction.byIndex(dummy-3).getOpposite();
			this.world.removeBlock(getPos(), false);
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
