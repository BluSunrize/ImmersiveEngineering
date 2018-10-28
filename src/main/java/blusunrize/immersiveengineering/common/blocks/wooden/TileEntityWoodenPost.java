/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IPostBlock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration2;
import com.google.common.collect.Lists;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

public class TileEntityWoodenPost extends TileEntityIEBase implements IPostBlock, IFaceShape, IHasDummyBlocks, IHasObjProperty, IBlockBounds, IHammerInteraction
{
	public static ArrayList<? extends Enum> postMetaProperties = Lists.newArrayList(BlockTypes_WoodenDevice1.POST, BlockTypes_MetalDecoration2.ALUMINUM_POST, BlockTypes_MetalDecoration2.STEEL_POST);

	public byte dummy;

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

	@SideOnly(Side.CLIENT)
	private AxisAlignedBB renderAABB;

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		return new AxisAlignedBB(getPos().add(-1, 0, -1), getPos().add(1, 4, 1));
	}

	static ArrayList<String> emptyDisplayList = new ArrayList();

	@Override
	public ArrayList<String> compileDisplayList()
	{
		if(dummy!=0)
			return emptyDisplayList;
		ArrayList<String> list = new ArrayList();
		list.add("base");
		TileEntity te;
		for(int i = 0; i <= 2; i++)
		{
			te = world.getTileEntity(getPos().add(0, 1+i, 0));
			if(te instanceof TileEntityWoodenPost)//Stacked pieces
			{
				for(EnumFacing f : EnumFacing.HORIZONTALS)
					if(((TileEntityWoodenPost)te).hasConnection(f))
					{
						if(i==2)//Arms
						{
							TileEntityWoodenPost arm = (TileEntityWoodenPost)world.getTileEntity(pos.add(0, 1+i, 0).offset(f));
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
			for(Enum meta : postMetaProperties)
				if(state.getProperties().containsValue(meta))
					return false;
			AxisAlignedBB boundingBox = state.getBoundingBox(world, pos);
			double minX = boundingBox.minX;
			double maxX = boundingBox.maxX;
			double minZ = boundingBox.minZ;
			double maxZ = boundingBox.maxZ;
			boolean connect = dir==EnumFacing.NORTH?maxZ==1: dir==EnumFacing.SOUTH?minZ==0: dir==EnumFacing.WEST?maxX==1: minX==0;
			return connect&&((dir.getAxis()==Axis.Z&&minX > 0&&maxX < 1)||(dir.getAxis()==Axis.X&&minZ > 0&&maxZ < 1));
		}
		else if(dummy==3)
		{
			TileEntity te = world.getTileEntity(pos);
			return (te instanceof TileEntityWoodenPost&&((TileEntityWoodenPost)te).dummy-3==dir.ordinal());
		}
		else if(dummy > 3)
		{
			if(world.isAirBlock(pos))
				return false;
			IBlockState state = world.getBlockState(pos);
			if(state.getMaterial().isReplaceable())
				return false;
			AxisAlignedBB boundingBox = state.getBoundingBox(world, pos);
			return dir==EnumFacing.UP?boundingBox.minY==0: dir==EnumFacing.DOWN&&boundingBox.maxY==1;
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
			((TileEntityWoodenPost)world.getTileEntity(pos.add(0, i, 0))).dummy = (byte)i;
			world.addBlockEvent(pos.add(0, i, 0), getBlockType(), 255, 0);
		}
	}

	@Override
	public void breakDummies(BlockPos pos, IBlockState state)
	{
		if(dummy <= 3)
			for(int i = 0; i <= 3; i++)
			{
				if(world.getTileEntity(getPos().add(0, -dummy, 0).add(0, i, 0)) instanceof TileEntityWoodenPost)
					world.setBlockToAir(getPos().add(0, -dummy, 0).add(0, i, 0));
				if(i==3)
				{
					TileEntity te;
					for(EnumFacing facing : EnumFacing.HORIZONTALS)
					{
						te = world.getTileEntity(getPos().add(0, -dummy, 0).add(0, i, 0).offset(facing));
						if(te instanceof TileEntityWoodenPost&&((TileEntityWoodenPost)te).dummy==(3+facing.ordinal()))
							world.setBlockToAir(getPos().add(0, -dummy, 0).add(0, i, 0).offset(facing));
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
			if(perpendicular instanceof TileEntityWoodenPost&&((TileEntityWoodenPost)perpendicular).dummy-3==side.rotateY().ordinal())
				return false;
			perpendicular = world.getTileEntity(getPos().offset(side.rotateYCCW()));
			if(perpendicular instanceof TileEntityWoodenPost&&((TileEntityWoodenPost)perpendicular).dummy-3==side.rotateYCCW().ordinal())
				return false;

			world.setBlockState(offsetPos, world.getBlockState(getPos()));
			((TileEntityWoodenPost)world.getTileEntity(offsetPos)).dummy = (byte)(3+side.ordinal());
			this.markBlockForUpdate(offsetPos, null);
			this.markBlockForUpdate(getPos().add(0, -3, 0), null);
		}
		else if(this.dummy > 3)
		{
			EnumFacing f = EnumFacing.byIndex(dummy-3).getOpposite();
			this.world.setBlockToAir(getPos());
			this.markBlockForUpdate(getPos().offset(f).add(0, -3, 0), null);
		}
		return false;
	}

	@Override
	public boolean canConnectTransformer(IBlockAccess world, BlockPos pos)
	{
		return this.dummy > 0&&this.dummy <= 3;
	}
}
