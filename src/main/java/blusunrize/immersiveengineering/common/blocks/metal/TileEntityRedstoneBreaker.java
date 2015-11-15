package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.util.Vec3;
import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.util.Utils;

public class TileEntityRedstoneBreaker extends TileEntityBreakerSwitch
{
	@Override
	protected boolean canTakeHV()
	{
		return true;
	}
	
	@Override
	public Vec3 getRaytraceOffset(IImmersiveConnectable link)
	{
		if(sideAttached==0)
			return Vec3.createVectorHelper(facing==4?1:facing==5?0:.5, .5, facing==2?1:facing==3?0:.5);
		return Vec3.createVectorHelper(.5,sideAttached==1?1:0,.5);
	}
	@Override
	public Vec3 getConnectionOffset(Connection con)
	{
		if(sideAttached==0)
		{
			int xDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.posX-xCoord: (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.posX-xCoord: 0;
			int zDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.posZ-zCoord: (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.posZ-zCoord: 0;

			return Vec3.createVectorHelper(facing==4?1.03125:facing==5?-.03125:xDif<0?.125:.875, .5, facing==2?1.03125:facing==3?-.03125:zDif<0?.125:.875);
		}
		else
		{
			int xDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.posX-xCoord: (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.posX-xCoord: 0;
			int zDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.posZ-zCoord: (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.posZ-zCoord: 0;
			double h = sideAttached==1?1.03125:-.03125;
			if(facing>3)
				return Vec3.createVectorHelper(.5,h,zDif>0?.875:.125);
			else
				return Vec3.createVectorHelper(xDif>0?.875:.125,h,.5);
		}
	}
}