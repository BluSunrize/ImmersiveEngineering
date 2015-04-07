package blusunrize.immersiveengineering.common.blocks.wooden;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
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

	@Override
	public void updateEntity()
	{
		if(offset[0]!=0||offset[1]!=0)
			return;
		if( !(worldObj.getBlock(xCoord-(facing<=3?2:0), yCoord+2, zCoord-(facing<=3?0:2)).isReplaceable(worldObj, xCoord-(facing<=3?2:0), yCoord+2, zCoord-(facing<=3?0:2)))
				|| !(worldObj.getBlock(xCoord+(facing<=3?2:0), yCoord+2, zCoord+(facing<=3?0:2)).isReplaceable(worldObj, xCoord+(facing<=3?2:0), yCoord+2, zCoord+(facing<=3?0:2)))
				|| !(worldObj.getBlock(xCoord-(facing<=3?2:0), yCoord-2, zCoord-(facing<=3?0:2)).isReplaceable(worldObj, xCoord-(facing<=3?2:0), yCoord-2, zCoord-(facing<=3?0:2)))
				|| !(worldObj.getBlock(xCoord+(facing<=3?2:0), yCoord-2, zCoord+(facing<=3?0:2)).isReplaceable(worldObj, xCoord+(facing<=3?2:0), yCoord-2, zCoord+(facing<=3?0:2))))
			return;

		Vec3 dir = Vec3.createVectorHelper(0, 0, 0);
		dir = Utils.addVectors(dir, getHorizontalVec());
		dir = Utils.addVectors(dir, getVerticalVec());

		double power = facing<=3?-dir.xCoord:dir.zCoord;
		double perTick = 360f/1440 * (1/360f) * power;
		rotation += perTick;
		rotation %= 1;
		if(!worldObj.isRemote)
		{
			ForgeDirection fd = ForgeDirection.getOrientation(facing);
			if(worldObj.getTileEntity(xCoord-fd.offsetX,yCoord-fd.offsetY,zCoord-fd.offsetZ) instanceof TileEntityDynamo)
			{
				TileEntityDynamo dynamo = (TileEntityDynamo)worldObj.getTileEntity(xCoord-fd.offsetX,yCoord-fd.offsetY,zCoord-fd.offsetZ);
				if((facing==2||facing==3)&&dynamo.facing!=2&&dynamo.facing!=3)
					return;
				else if((facing==4||facing==5)&&dynamo.facing!=4&&dynamo.facing!=5)
					return;
				dynamo.inputRotation(Math.abs(power));
			}
		}
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
	public void readCustomNBT(NBTTagCompound nbt)
	{
		facing = nbt.getInteger("facing");
		offset = nbt.getIntArray("offset");
		rotation = nbt.getFloat("rotation");

		if(offset==null||offset.length<2)
			offset=new int[]{0,0};
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt)
	{
		nbt.setInteger("facing", facing);
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
}