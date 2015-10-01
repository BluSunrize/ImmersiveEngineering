package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.WireType;
import blusunrize.immersiveengineering.common.blocks.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.common.util.Utils;

public class TileEntityBreakerSwitch extends TileEntityImmersiveConnectable
{
	public int sideAttached=0;
	public int facing=2;
	public int wires = 0;
	public boolean active=false;
	public boolean inverted=false;

	@Override
	protected boolean canTakeLV()
	{
		return true;
	}
	@Override
	protected boolean canTakeMV()
	{
		return true;
	}
	@Override
	protected boolean canTakeHV()
	{
		return true;
	}

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public boolean allowEnergyToPass(Connection con)
	{
		return active;
	}

	@Override
	public boolean canConnectCable(WireType cableType, TargetingInfo target)
	{
		if(cableType!=null && !cableType.isEnergyWire())
			return false;
		if(wires>=2)
			return false;
		return limitType==null || cableType==limitType;
	}
	@Override
	public void connectCable(WireType cableType, TargetingInfo target)
	{
		if(this.limitType==null)
			this.limitType = cableType;
		wires++;
	}
	@Override
	public WireType getCableLimiter(TargetingInfo target)
	{
		return limitType;
	}
	@Override
	public void removeCable(Connection connection)
	{
		WireType type = connection!=null?connection.cableType:null;
		if(type==null)
			wires = 0;
		else
			wires--;
		if(wires<=0)
			limitType=null;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing);
		nbt.setInteger("sideAttached", sideAttached);
		nbt.setInteger("wires", wires);
		nbt.setBoolean("active", active);
		nbt.setBoolean("inverted", inverted);
	}
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = nbt.getInteger("facing");
		sideAttached = nbt.getInteger("sideAttached");
		wires = nbt.getInteger("wires");
		active = nbt.getBoolean("active");
		inverted = nbt.getBoolean("inverted");
	}

	@Override
	public Vec3 getRaytraceOffset(IImmersiveConnectable link)
	{
		return Vec3.createVectorHelper(.5,.5,.5);
	}
	@Override
	public Vec3 getConnectionOffset(Connection con)
	{
		if(sideAttached==0)
		{
			int xDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.posX-xCoord: (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.posX-xCoord: 0;
			int zDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.posZ-zCoord: (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.posZ-zCoord: 0;

			return Vec3.createVectorHelper(facing==4?.125:facing==5?.875:xDif<0?.25:.75, .5, facing==2?.125:facing==3?.875:zDif<0?.25:.75);
		}
		else
		{
			int xDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.posX-xCoord: (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.posX-xCoord: 0;
			int zDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.posZ-zCoord: (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.posZ-zCoord: 0;
			double h = sideAttached==1?.125: .875;
			if(facing>3)
				return Vec3.createVectorHelper(.5,h,zDif>0?.75:.25);
			else
				return Vec3.createVectorHelper(xDif>0?.75:.25,h,.5);
		}
	}

	public void toggle()
	{
		active = !active;
		ImmersiveNetHandler.INSTANCE.resetCachedIndirectConnections();
		worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), active?1:0, 0);
	}
	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(super.receiveClientEvent(id, arg))
			return true;
		this.active = id==1;
		this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		return true;
	}

}
