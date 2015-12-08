package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorStructural;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;

public class TileEntityBalloon extends TileEntityConnectorStructural
{
	public int style = 0;
	public byte colour0 = 15;
	public byte colour1 = 15;

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt,descPacket);
		style = nbt.getInteger("style");
		colour0 = nbt.getByte("colour0");
		colour1 = nbt.getByte("colour1");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt,descPacket);
		nbt.setInteger("style",style);
		nbt.setByte("colour0",colour0);
		nbt.setByte("colour1",colour1);
	}
	
	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==0)
		{
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			return true;
		}
		return super.receiveClientEvent(id, arg);
	}
	
	@Override
	public Vec3 getRaytraceOffset(IImmersiveConnectable link)
	{
		int xDif = ((TileEntity)link).xCoord-xCoord;
		int zDif = ((TileEntity)link).zCoord-zCoord;
		int yDif = ((TileEntity)link).yCoord-yCoord;
		if(yDif<0)
		{
			double dist = Math.sqrt(xDif*xDif + zDif*zDif);
			if(dist/Math.abs(yDif)<2.5)
				return Vec3.createVectorHelper(.5,.09375,.5);
		}
		if(Math.abs(zDif)>Math.abs(xDif))
			return Vec3.createVectorHelper(.5,.09375,zDif>0?.8125:.1875);
		else
			return Vec3.createVectorHelper(xDif>0?.8125:.1875,.09375,.5);
	}
	@Override
	public Vec3 getConnectionOffset(Connection con)
	{
		int xDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.posX-xCoord: (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.posX-xCoord: 0;
		int zDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.posZ-zCoord: (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.posZ-zCoord: 0;
		int yDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.posY-yCoord: (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.posY-yCoord: 0;
		if(yDif<0)
		{
			double dist = Math.sqrt(xDif*xDif + zDif*zDif);
			if(dist/Math.abs(yDif)<2.5)
				return Vec3.createVectorHelper(.5,.09375,.5);
		}
		if(Math.abs(zDif)>Math.abs(xDif))
			return Vec3.createVectorHelper(.5,.09375,zDif>0?.78125:.21875);
		else
			return Vec3.createVectorHelper(xDif>0?.78125:.21875,.09375,.5);
	}
}