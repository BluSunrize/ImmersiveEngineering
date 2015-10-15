package blusunrize.immersiveengineering.common.blocks.wooden;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDynamo;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityWatermill extends TileEntityIEBase
{
	public int facing = 2;
	public int[] offset={0,0};
	public float rotation=0;
	private Vec3 rotationVec = null;
	public boolean canTurn = false;
	public boolean multiblock = false;
	public float prevRotation = 0;

	@Override
	public void updateEntity()
	{
		if(offset[0]!=0||offset[1]!=0)
			return;
		if( 
				//				!(worldObj.getBlock(xCoord-(facing<=3?2:0), yCoord+2, zCoord-(facing<=3?0:2)).isReplaceable(worldObj, xCoord-(facing<=3?2:0), yCoord+2, zCoord-(facing<=3?0:2)))
				//				|| !(worldObj.getBlock(xCoord+(facing<=3?2:0), yCoord+2, zCoord+(facing<=3?0:2)).isReplaceable(worldObj, xCoord+(facing<=3?2:0), yCoord+2, zCoord+(facing<=3?0:2)))
				//				|| !(worldObj.getBlock(xCoord-(facing<=3?2:0), yCoord-2, zCoord-(facing<=3?0:2)).isReplaceable(worldObj, xCoord-(facing<=3?2:0), yCoord-2, zCoord-(facing<=3?0:2)))
				//				|| !(worldObj.getBlock(xCoord+(facing<=3?2:0), yCoord-2, zCoord+(facing<=3?0:2)).isReplaceable(worldObj, xCoord+(facing<=3?2:0), yCoord-2, zCoord+(facing<=3?0:2))))
				isBlocked())
		{
			canTurn=false;
			return;
		}
		else
			canTurn=getRotationVec().lengthVector()!=0;

		if(!multiblock /*&& worldObj.isRemote*/ && worldObj.getTotalWorldTime()%256==((xCoord^zCoord)&255))
		{
			rotationVec=null;
		}
		prevRotation = rotation;

		ForgeDirection fd = ForgeDirection.getOrientation(facing);
		if(worldObj.getTileEntity(xCoord-fd.offsetX,yCoord,zCoord-fd.offsetZ) instanceof TileEntityDynamo)
		{
			double power = getPower();
			int l=1;
			TileEntity tileEntity = worldObj.getTileEntity(xCoord+fd.offsetX*l,yCoord,zCoord+fd.offsetZ*l);
			while (l<3
					&& tileEntity instanceof TileEntityWatermill
					&& ((TileEntityWatermill)tileEntity).offset[0]==0
					&& ((TileEntityWatermill)tileEntity).offset[1]==0
					&& ((TileEntityWatermill)tileEntity).facing==facing
					&& !((TileEntityWatermill)tileEntity).isBlocked())
			{
				power += ((TileEntityWatermill)tileEntity).getPower();
				l++;
				tileEntity = worldObj.getTileEntity(xCoord+fd.offsetX*l,yCoord,zCoord+fd.offsetZ*l);
			}

			double perTick = 360f/1440 * (1/360f) * power/l;
			canTurn = perTick!=0;
			rotation += perTick;
			rotation %= 1;
			for(int l2=1; l2<l; l2++)
				tileEntity = worldObj.getTileEntity(xCoord+fd.offsetX*l2,yCoord,zCoord+fd.offsetZ*l2);
				if(tileEntity instanceof TileEntityWatermill)
				{
					((TileEntityWatermill)tileEntity).rotation = rotation;
					((TileEntityWatermill)tileEntity).canTurn = canTurn;
					((TileEntityWatermill)tileEntity).multiblock = true;
				}

			if(!worldObj.isRemote)
			{
				TileEntityDynamo dynamo = (TileEntityDynamo)worldObj.getTileEntity(xCoord-fd.offsetX,yCoord-fd.offsetY,zCoord-fd.offsetZ);
				if((facing==2||facing==3)&&dynamo.facing!=2&&dynamo.facing!=3)
					return;
				else if((facing==4||facing==5)&&dynamo.facing!=4&&dynamo.facing!=5)
					return;
				dynamo.inputRotation(Math.abs(power*.75), ForgeDirection.OPPOSITES[facing]);
			}
		}
		else if(!multiblock)
		{
			double perTick = 360f/1440 * (1/360f) * getPower();
			canTurn = perTick!=0;
			rotation += perTick;
			rotation %= 1;
		}
		if(multiblock)
			multiblock=false;
	}

	public boolean isBlocked()
	{
		for(ForgeDirection fdY : new ForgeDirection[]{ForgeDirection.UP,ForgeDirection.DOWN})
			for(ForgeDirection fdW : facing<=3?new ForgeDirection[]{ForgeDirection.EAST,ForgeDirection.WEST}: new ForgeDirection[]{ForgeDirection.SOUTH,ForgeDirection.NORTH})
			{
				Block b = worldObj.getBlock(xCoord+fdW.offsetX*2, yCoord+fdY.offsetY*2, zCoord+fdW.offsetZ*2);
				if(b.isSideSolid(worldObj, xCoord+fdW.offsetX*2,yCoord+fdY.offsetY*2,zCoord+fdW.offsetZ*2, fdW.getOpposite()))
					return true;
				if(b.isSideSolid(worldObj, xCoord+fdW.offsetX*2,yCoord+fdY.offsetY*2,zCoord+fdW.offsetZ*2, fdY.getOpposite()))
					return true;
			}
		return false;
	}

	public double getPower()
	{
		return facing<=3?-getRotationVec().xCoord:getRotationVec().zCoord;
	}
	public void resetRotationVec()
	{
		rotationVec=null;
	}
	public Vec3 getRotationVec()
	{
		if(rotationVec==null)
		{
			rotationVec = Vec3.createVectorHelper(0, 0, 0);
			rotationVec = Utils.addVectors(rotationVec, getHorizontalVec());
			rotationVec = Utils.addVectors(rotationVec, getVerticalVec());
//			worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), (int)((float)rotationVec.xCoord*10000f), (int)((float)rotationVec.zCoord*10000f));
		}
		return rotationVec;
	}

	Vec3 getHorizontalVec()
	{
		Vec3 dir = Vec3.createVectorHelper(0, 0, 0);
		dir = Utils.addVectors(dir, Utils.getFlowVector(worldObj, xCoord-(facing<=3?1:0), yCoord+3, zCoord-(facing<=3?0:1)));
		dir = Utils.addVectors(dir, Utils.getFlowVector(worldObj, xCoord, yCoord+3, zCoord));
		dir = Utils.addVectors(dir, Utils.getFlowVector(worldObj, xCoord+(facing<=3?1:0), yCoord+3, zCoord+(facing<=3?0:1)));

		dir = Utils.addVectors(dir, Utils.getFlowVector(worldObj, xCoord-(facing<=3?2:0), yCoord+2, zCoord-(facing<=3?0:2)));
		dir = Utils.addVectors(dir, Utils.getFlowVector(worldObj, xCoord+(facing<=3?2:0), yCoord+2, zCoord+(facing<=3?0:2)));

		dir = Utils.getFlowVector(worldObj, xCoord-(facing<=3?2:0), yCoord-2, zCoord-(facing<=3?0:2)).subtract(dir);
		dir = Utils.getFlowVector(worldObj, xCoord+(facing<=3?2:0), yCoord-2, zCoord+(facing<=3?0:2)).subtract(dir);

		dir = Utils.getFlowVector(worldObj, xCoord-(facing<=3?1:0), yCoord-3, zCoord-(facing<=3?0:1)).subtract(dir);
		dir = Utils.getFlowVector(worldObj, xCoord, yCoord-3, zCoord).subtract(dir);
		dir = Utils.getFlowVector(worldObj, xCoord+(facing<=3?1:0), yCoord-3, zCoord+(facing<=3?0:1)).subtract(dir);
		return dir;
	}
	Vec3 getVerticalVec()
	{
		Vec3 dir = Vec3.createVectorHelper(0, 0, 0);

		Vec3 dirNeg = Vec3.createVectorHelper(0, 0, 0);
		dirNeg = Utils.addVectors(dirNeg, Utils.getFlowVector(worldObj, xCoord-(facing<=3?2:0), yCoord+2, zCoord-(facing<=3?0:2)));
		dirNeg = Utils.addVectors(dirNeg, Utils.getFlowVector(worldObj, xCoord-(facing<=3?3:0), yCoord+1, zCoord-(facing<=3?0:3)));
		dirNeg = Utils.addVectors(dirNeg, Utils.getFlowVector(worldObj, xCoord-(facing<=3?3:0), yCoord+0, zCoord-(facing<=3?0:3)));
		dirNeg = Utils.addVectors(dirNeg, Utils.getFlowVector(worldObj, xCoord-(facing<=3?3:0), yCoord-1, zCoord-(facing<=3?0:3)));
		dirNeg = Utils.addVectors(dirNeg, Utils.getFlowVector(worldObj, xCoord-(facing<=3?2:0), yCoord-2, zCoord-(facing<=3?0:2)));
		Vec3 dirPos = Vec3.createVectorHelper(0, 0, 0);
		dirPos = Utils.addVectors(dirPos, Utils.getFlowVector(worldObj, xCoord+(facing<=3?2:0), yCoord+2, zCoord+(facing<=3?0:2)));
		dirPos = Utils.addVectors(dirPos, Utils.getFlowVector(worldObj, xCoord+(facing<=3?3:0), yCoord+1, zCoord+(facing<=3?0:3)));
		dirPos = Utils.addVectors(dirPos, Utils.getFlowVector(worldObj, xCoord+(facing<=3?3:0), yCoord+0, zCoord+(facing<=3?0:3)));
		dirPos = Utils.addVectors(dirPos, Utils.getFlowVector(worldObj, xCoord+(facing<=3?3:0), yCoord-1, zCoord+(facing<=3?0:3)));
		dirPos = Utils.addVectors(dirPos, Utils.getFlowVector(worldObj, xCoord+(facing<=3?2:0), yCoord-2, zCoord+(facing<=3?0:2)));

		if(facing<=3)
		{
			dir.xCoord += dirNeg.yCoord;
			dir.xCoord -= dirPos.yCoord;
		}
		else
		{
			dir.zCoord += dirNeg.yCoord;
			dir.zCoord -= dirPos.yCoord;
		}
		return dir;
	}

	public static boolean _Immovable()
	{
		return true;
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		rotationVec = Vec3.createVectorHelper(id/10000f, 0, arg/10000f);
		return true;
	}
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = nbt.getInteger("facing");
		prevRotation = nbt.getFloat("prevRotation");
		offset = nbt.getIntArray("offset");
		rotation = nbt.getFloat("rotation");

		if(offset==null||offset.length<2)
			offset=new int[]{0,0};
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing", facing);
		nbt.setFloat("prevRotation", prevRotation);
		nbt.setIntArray("offset", offset);
		nbt.setFloat("rotation", rotation);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(offset[0]==0&&offset[1]==0)
			return AxisAlignedBB.getBoundingBox(xCoord-(facing<=3?2:0),yCoord-2,zCoord-(facing<=3?0:2), xCoord+(facing<=3?3:0),yCoord+3,zCoord+(facing<=3?0:3));
		return AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord,yCoord,zCoord);
	}
	@Override
	public double getMaxRenderDistanceSquared()
	{
		return super.getMaxRenderDistanceSquared()*Config.getDouble("increasedTileRenderdistance");
//		if(Config.getBoolean("increasedTileRenderdistance"))
//			return super.getMaxRenderDistanceSquared()*1.5;
//		return super.getMaxRenderDistanceSquared();
	}
}