package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.WireType;
import blusunrize.immersiveengineering.common.blocks.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.common.util.Utils;

public class TileEntityBreakerSwitch extends TileEntityImmersiveConnectable
{
	public int sideAttached=0;
	public int facing=2;
	public int wires = 0;
	public boolean active=false;
	
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
	}
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = nbt.getInteger("facing");
		sideAttached = nbt.getInteger("sideAttached");
		wires = nbt.getInteger("wires");
		active = nbt.getBoolean("active");
	}

	@Override
	public Vec3 getRaytraceOffset()
	{
		//		ForgeDirection fd = ForgeDirection.getOrientation(facing).getOpposite();
		//		return Vec3.createVectorHelper(.5+.5*fd.offsetX, .5+.5*fd.offsetY, .5+.5*fd.offsetZ);
		return Vec3.createVectorHelper(.5,.5,.5);
	}
	@Override
	public Vec3 getConnectionOffset(Connection con)
	{
		double r = con.cableType.getRenderDiameter()/2;
		if(sideAttached==0)
		{
			double h = .25+r;
			if(con!=null && con.start!=null && con.end!=null)
				if((con.start.equals(Utils.toCC(this)) && con.end!=null && con.end.posY>yCoord) || (con.end.equals(Utils.toCC(this)) && con.start!=null && con.start.posY>yCoord))
					h=.8125-r;
			return Vec3.createVectorHelper(facing==4?.0625-r:facing==5?.9375+r:.5, h, facing==2?.0625-r:facing==3?.9375+r:.5);
		}
		else
		{
			int xDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.posX-xCoord: (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.posX-xCoord: 0;
			int zDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.posZ-zCoord: (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.posZ-zCoord: 0;
			double h = sideAttached==1?.0625-r: .9375+r;
			if((facing==4&&xDif>0) || (facing==5&&xDif<0) || (facing==2&&zDif>0) || (facing==3&&zDif<0))
				return Vec3.createVectorHelper(facing==4?.6875:facing==5?.3125:.5, h, facing==2?.6875:facing==3?.3125:.5);
			return Vec3.createVectorHelper(facing==4?.25:facing==5?.75:.5, h, facing==2?.25:facing==3?.75:.5);
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
		this.active = id==1;
		this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		return true;
	}

}
