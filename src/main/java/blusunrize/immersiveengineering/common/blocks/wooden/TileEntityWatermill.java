/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.IRotationAcceptor;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasObjProperty;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class TileEntityWatermill extends TileEntityIEBase implements ITickable, IDirectionalTile, IHasDummyBlocks, IHasObjProperty
{
	public EnumFacing facing = EnumFacing.NORTH;
	public int[] offset = {0, 0};
	public float rotation = 0;
	private Vec3d rotationVec = null;
	public boolean canTurn = false;
	public boolean multiblock = false;
	public float prevRotation = 0;
	private boolean formed = true;
	public double perTick;

//	@Override
//	public boolean hasFastRenderer()
//	{
//		return true;
//	}

	@Override
	public void update()
	{
		ApiUtils.checkForNeedlessTicking(this);
		if(offset[0]!=0||offset[1]!=0||world==null)
			return;
		if(isBlocked())
		{
			canTurn = false;
			return;
		}
		else
			canTurn = multiblock||getRotationVec().length()!=0;

		if(world.getTotalWorldTime()%64==((getPos().getX()^getPos().getZ())&63))
		{
			rotationVec = null;
		}
		prevRotation = rotation;

		TileEntity acc = Utils.getExistingTileEntity(world, getPos().offset(facing.getOpposite()));
		if(!multiblock&&acc instanceof IRotationAcceptor)
		{
			double power = getPower();
			int l = 1;
			TileEntity tileEntity = Utils.getExistingTileEntity(world, getPos().offset(facing, l));
			while(l < 3
					&&canUse(tileEntity))
			{
				power += ((TileEntityWatermill)tileEntity).getPower();
				l++;
				tileEntity = Utils.getExistingTileEntity(world, getPos().offset(facing, l));
			}

			perTick = 1f/1440*power/l;
			canTurn = perTick!=0;
			rotation += perTick;
			rotation %= 1;
			for(int l2 = 1; l2 < l; l2++)
			{
				tileEntity = world.getTileEntity(getPos().offset(facing, l2));
				if(tileEntity instanceof TileEntityWatermill)
				{
					((TileEntityWatermill)tileEntity).rotation = rotation;
					((TileEntityWatermill)tileEntity).canTurn = canTurn;
					((TileEntityWatermill)tileEntity).perTick = perTick;
					((TileEntityWatermill)tileEntity).multiblock = true;
				}
			}

			if(!world.isRemote)
			{
				IRotationAcceptor dynamo = (IRotationAcceptor)acc;
				//				if((facing.getAxis()==Axis.Z)&&dynamo.facing!=2&&dynamo.facing!=3)
				//					return;
				//				else if((facing.getAxis()==Axis.X)&&dynamo.facing!=4&&dynamo.facing!=5)
				//					return;
				dynamo.inputRotation(Math.abs(power*.75), facing.getOpposite());
			}
		}
		else if(!multiblock)
		{
			perTick = 1f/1440*getPower();
			canTurn = perTick!=0;
			rotation += perTick;
			rotation %= 1;
		}
		if(multiblock)
			multiblock = false;
	}

	private boolean canUse(@Nullable TileEntity tileEntity)
	{
		return tileEntity instanceof TileEntityWatermill
				&&((TileEntityWatermill)tileEntity).offset[0]==0
				&&((TileEntityWatermill)tileEntity).offset[1]==0
				&&(((TileEntityWatermill)tileEntity).facing==facing||((TileEntityWatermill)tileEntity).facing==facing.getOpposite())
				&&!((TileEntityWatermill)tileEntity).isBlocked()
				&&!((TileEntityWatermill)tileEntity).multiblock;
	}

	public boolean isBlocked()
	{
		if(world==null)
			return true;
		for(EnumFacing fdY : new EnumFacing[]{EnumFacing.UP, EnumFacing.DOWN})
			for(EnumFacing fdW : facing.getAxis()==Axis.Z?new EnumFacing[]{EnumFacing.EAST, EnumFacing.WEST}: new EnumFacing[]{EnumFacing.SOUTH, EnumFacing.NORTH})
			{
				BlockPos pos = getPos().offset(fdW, 2).offset(fdY, 2);
				IBlockState state = world.getBlockState(pos);
				if(state==null)
					return false;
				if(state.isSideSolid(world, pos, fdW.getOpposite()))
					return true;
				if(state.isSideSolid(world, pos, fdY.getOpposite()))
					return true;
			}
		return false;
	}

	public double getPower()
	{
		return facing.getAxis()==Axis.Z?-getRotationVec().x: getRotationVec().z;
	}

	public void resetRotationVec()
	{
		rotationVec = null;
	}

	public Vec3d getRotationVec()
	{
		if(rotationVec==null)
		{
			rotationVec = new Vec3d(0, 0, 0);
			Vec3d dirHoz = getHorizontalVec();
			Vec3d dirVer = getVerticalVec();
			rotationVec = Utils.addVectors(rotationVec, dirHoz);
			rotationVec = Utils.addVectors(rotationVec, dirVer);
			//			world.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), (int)((float)rotationVec.xCoord*10000f), (int)((float)rotationVec.zCoord*10000f));
		}
		return rotationVec;
	}

	Vec3d getHorizontalVec()
	{
		Vec3d dir = new Vec3d(0, 0, 0);
		boolean faceZ = facing.ordinal() <= 3;
		dir = Utils.addVectors(dir, Utils.getFlowVector(world, getPos().add(-(faceZ?1: 0), +3, -(faceZ?0: 1))));
		dir = Utils.addVectors(dir, Utils.getFlowVector(world, getPos().add(0, +3, 0)));
		dir = Utils.addVectors(dir, Utils.getFlowVector(world, getPos().add(+(faceZ?1: 0), +3, +(faceZ?0: 1))));

		dir = Utils.addVectors(dir, Utils.getFlowVector(world, getPos().add(-(faceZ?2: 0), +2, -(faceZ?0: 2))));
		dir = Utils.addVectors(dir, Utils.getFlowVector(world, getPos().add(+(faceZ?2: 0), +2, +(faceZ?0: 2))));

		dir = dir.subtract(Utils.getFlowVector(world, getPos().add(-(faceZ?2: 0), -2, -(faceZ?0: 2))));
		dir = dir.subtract(Utils.getFlowVector(world, getPos().add(+(faceZ?2: 0), -2, +(faceZ?0: 2))));
		dir = dir.subtract(Utils.getFlowVector(world, getPos().add(-(faceZ?1: 0), -3, -(faceZ?0: 1))));
		dir = dir.subtract(Utils.getFlowVector(world, getPos().add(0, -3, 0)));
		dir = dir.subtract(Utils.getFlowVector(world, getPos().add(+(faceZ?1: 0), -3, +(faceZ?0: 1))));

		return dir;
	}

	Vec3d getVerticalVec()
	{
		Vec3d dir = new Vec3d(0, 0, 0);

		Vec3d dirNeg = new Vec3d(0, 0, 0);
		dirNeg = Utils.addVectors(dirNeg, Utils.getFlowVector(world, getPos().add(-(facing.getAxis()==Axis.Z?2: 0), 2, -(facing.getAxis()==Axis.Z?0: 2))));
		dirNeg = Utils.addVectors(dirNeg, Utils.getFlowVector(world, getPos().add(-(facing.getAxis()==Axis.Z?3: 0), 1, -(facing.getAxis()==Axis.Z?0: 3))));
		dirNeg = Utils.addVectors(dirNeg, Utils.getFlowVector(world, getPos().add(-(facing.getAxis()==Axis.Z?3: 0), 0, -(facing.getAxis()==Axis.Z?0: 3))));
		dirNeg = Utils.addVectors(dirNeg, Utils.getFlowVector(world, getPos().add(-(facing.getAxis()==Axis.Z?3: 0), -1, -(facing.getAxis()==Axis.Z?0: 3))));
		dirNeg = Utils.addVectors(dirNeg, Utils.getFlowVector(world, getPos().add(-(facing.getAxis()==Axis.Z?2: 0), -2, -(facing.getAxis()==Axis.Z?0: 2))));
		Vec3d dirPos = new Vec3d(0, 0, 0);
		dirPos = Utils.addVectors(dirPos, Utils.getFlowVector(world, getPos().add((facing.getAxis()==Axis.Z?2: 0), 2, (facing.getAxis()==Axis.Z?0: 2))));
		dirPos = Utils.addVectors(dirPos, Utils.getFlowVector(world, getPos().add((facing.getAxis()==Axis.Z?3: 0), 1, (facing.getAxis()==Axis.Z?0: 3))));
		dirPos = Utils.addVectors(dirPos, Utils.getFlowVector(world, getPos().add((facing.getAxis()==Axis.Z?3: 0), 0, (facing.getAxis()==Axis.Z?0: 3))));
		dirPos = Utils.addVectors(dirPos, Utils.getFlowVector(world, getPos().add((facing.getAxis()==Axis.Z?3: 0), -1, (facing.getAxis()==Axis.Z?0: 3))));
		dirPos = Utils.addVectors(dirPos, Utils.getFlowVector(world, getPos().add((facing.getAxis()==Axis.Z?2: 0), -2, (facing.getAxis()==Axis.Z?0: 2))));
		if(facing.getAxis()==Axis.Z)
			dir = dir.add(dirNeg.y-dirPos.y, 0, 0);
		else
			dir = dir.add(0, 0, dirNeg.y-dirPos.y);
		return dir;
	}

	public static boolean _Immovable()
	{
		return true;
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		rotationVec = new Vec3d(id/10000f, 0, arg/10000f);
		return true;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = EnumFacing.byIndex(nbt.getInteger("facing"));
		prevRotation = nbt.getFloat("prevRotation");
		offset = nbt.getIntArray("offset");
		rotation = nbt.getFloat("rotation");

		if(offset==null||offset.length < 2)
			offset = new int[]{0, 0};
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing", facing.ordinal());
		nbt.setFloat("prevRotation", prevRotation);
		nbt.setIntArray("offset", offset);
		nbt.setFloat("rotation", rotation);
	}

	@SideOnly(Side.CLIENT)
	private AxisAlignedBB renderAABB;

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(renderAABB==null)
			if(offset[0]==0&&offset[1]==0)
				renderAABB = new AxisAlignedBB(getPos().getX()-(facing.getAxis()==Axis.Z?2: 0), getPos().getY()-2, getPos().getZ()-(facing.getAxis()==Axis.Z?0: 2), getPos().getX()+(facing.getAxis()==Axis.Z?3: 0), getPos().getY()+3, getPos().getZ()+(facing.getAxis()==Axis.Z?0: 3));
			else
				renderAABB = new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX()+1, getPos().getY()+1, getPos().getZ()+1);
		return renderAABB;
	}

	@Override
	public EnumFacing getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 6;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return false;
	}

	@Override
	public boolean isDummy()
	{
		return offset[0]!=0||offset[1]!=0;
	}

	@Override
	public void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		for(int hh = -2; hh <= 2; hh++)
			for(int ww = -2; ww <= 2; ww++)
				if((hh > -2&&hh < 2)||(ww > -2&&ww < 2))
				{
					BlockPos pos2 = pos.add(facing.getAxis()==Axis.Z?ww: 0, hh, facing.getAxis()==Axis.Z?0: ww);
					world.setBlockState(pos2, state);
					TileEntityWatermill dummy = (TileEntityWatermill)world.getTileEntity(pos2);
					dummy.facing = facing;
					dummy.offset = new int[]{ww, hh};
				}
	}

	@Override
	public void breakDummies(BlockPos pos, IBlockState state)
	{
		if(!formed)
			return;
		BlockPos initPos = pos.add(facing.getAxis()==Axis.Z?-offset[0]: 0, -offset[1], facing.getAxis()==Axis.X?-offset[0]: 0);
		for(int hh = -2; hh <= 2; hh++)
			for(int ww = -2; ww <= 2; ww++)
				if((hh > -2&&hh < 2)||(ww > -2&&ww < 2))
				{
					BlockPos pos2 = initPos.add(facing.getAxis()==Axis.Z?ww: 0, hh, facing.getAxis()==Axis.X?ww: 0);
					TileEntity te = world.getTileEntity(pos2);
					if(te instanceof TileEntityWatermill)
					{
						((TileEntityWatermill)te).formed = false;
						world.setBlockToAir(pos2);
					}
				}
	}

	static ArrayList<String> emptyDisplayList = new ArrayList();

	@Override
	public ArrayList<String> compileDisplayList()
	{
		return emptyDisplayList;
	}
}
