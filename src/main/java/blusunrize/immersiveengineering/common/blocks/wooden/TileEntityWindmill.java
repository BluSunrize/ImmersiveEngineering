package blusunrize.immersiveengineering.common.blocks.wooden;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDynamo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityWindmill extends TileEntityIEBase
{
	public int facing = 2;
	public float prevRotation=0;
	public float rotation=0;
	public float turnSpeed=0;

	public boolean canTurn = false;

	@Override
	public void updateEntity()
	{
		if(worldObj.getTotalWorldTime()%128==((xCoord^zCoord)&127))
			canTurn = checkArea();
		if(!canTurn)
			return;

		double mod = .00005;
		if(!worldObj.isRaining())
			mod *= .75;
		if(!worldObj.isThundering())
			mod *= .66;
		if(yCoord>200)
			mod *= 2;
		else if(yCoord>150)
			mod *= 1.5;
		else if(yCoord>100)
			mod *= 1.25;
		else if(yCoord<70)
			mod *= .33;
		mod*=getSpeedModifier();
		
		
		prevRotation = (float) (turnSpeed*mod);
		rotation += turnSpeed*mod;
		rotation %= 1;

		if(!worldObj.isRemote)
		{
			ForgeDirection fd = ForgeDirection.getOrientation(facing);
			TileEntity tileEntity = worldObj.getTileEntity(xCoord-fd.offsetX,yCoord-fd.offsetY,zCoord-fd.offsetZ);
			if(tileEntity instanceof TileEntityDynamo)
			{
				TileEntityDynamo dynamo = (TileEntityDynamo)tileEntity;
				if((facing==2||facing==3)&&dynamo.facing!=2&&dynamo.facing!=3)
					return;
				else if((facing==4||facing==5)&&dynamo.facing!=4&&dynamo.facing!=5)
					return;
				double power = turnSpeed*mod * 400;
				dynamo.inputRotation(Math.abs(power), ForgeDirection.OPPOSITES[facing]);
			}
		}
	}
	protected float getSpeedModifier()
	{
		return .5f;
	}

	public boolean checkArea()
	{
		turnSpeed=0;
		for(int hh=-6;hh<=6;hh++)
		{
			int r=Math.abs(hh)==6?1: Math.abs(hh)==5?3: Math.abs(hh)==4?4: Math.abs(hh)>1?5: 6;
			for(int ww=-r;ww<=r;ww++)
				if((hh!=0||ww!=0)&&!worldObj.isAirBlock(xCoord+(facing<=3?ww:0), yCoord+hh, zCoord+(facing<=3?0:ww)))
					return false;
		}

		for(int hh=-6;hh<=6;hh++)
		{
			int blocked = 0;
			int r=Math.abs(hh)==6?1: Math.abs(hh)==5?3: Math.abs(hh)==4?4: Math.abs(hh)>1?5: 6;
			for(int ww=-r;ww<=r;ww++)
			{
				for(int dd=1;dd<8;dd++)
				{
					int xx = xCoord+(facing<=3?ww:0)+(facing==4?-dd: facing==5?dd: 0);
					int yy = yCoord+hh;
					int zz = zCoord+(facing<=3?0:ww)+(facing==2?-dd: facing==3?dd: 0);
					if(worldObj.isAirBlock(xx,yy,zz))
						turnSpeed ++;
					else if(worldObj.getTileEntity(xx,yy,zz) instanceof TileEntityWindmill)
					{
						blocked+=20;
						turnSpeed++;
						turnSpeed-=180;
					}
					else
						blocked++;
				}
			}
			if(blocked>100)
				return false;
			else if(blocked>50)
				return true;
		}

		return true;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = nbt.getInteger("facing");
		//prevRotation = nbt.getFloat("prevRotation");
		rotation = nbt.getFloat("rotation");
		turnSpeed = nbt.getFloat("turnSpeed");
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing", facing);
		//nbt.setFloat("prevRotation", prevRotation);
		nbt.setFloat("rotation", rotation);
		nbt.setFloat("turnSpeed", turnSpeed);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		return AxisAlignedBB.getBoundingBox(xCoord-(facing<=3?6:0),yCoord-6,zCoord-(facing<=3?0:6), xCoord+(facing<=3?7:0),yCoord+7,zCoord+(facing<=3?0:7));
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