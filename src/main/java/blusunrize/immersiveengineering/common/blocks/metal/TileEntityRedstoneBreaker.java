package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.util.Utils;

public class TileEntityRedstoneBreaker extends TileEntityBreakerSwitch
{
	Connection primaryConnection;
	@Override
	protected boolean canTakeHV()
	{
		return true;
	}

	@Override
	public boolean allowEnergyToPass(Connection con)
	{
		return !worldObj.isBlockIndirectlyGettingPowered(xCoord,yCoord,zCoord);
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
		int lowestDif=100;
		Connection lowestCon=null;
		for(Connection otherCon : ImmersiveNetHandler.INSTANCE.getConnections(worldObj, new ChunkCoordinates(xCoord,yCoord,zCoord)))
		{
			int xDif = (otherCon==null||otherCon.start==null||otherCon.end==null)?0: (otherCon.start.equals(Utils.toCC(this))&&otherCon.end!=null)? otherCon.end.posX-xCoord: (otherCon.end.equals(Utils.toCC(this))&& otherCon.start!=null)?otherCon.start.posX-xCoord: 0;
			int zDif = (otherCon==null||otherCon.start==null||otherCon.end==null)?0: (otherCon.start.equals(Utils.toCC(this))&&otherCon.end!=null)? otherCon.end.posZ-zCoord: (otherCon.end.equals(Utils.toCC(this))&& otherCon.start!=null)?otherCon.start.posZ-zCoord: 0;
			int dif = facing>3?zDif:xDif;
			if(lowestCon==null || dif<lowestDif)
			{
				lowestDif = dif;
				lowestCon = otherCon;
			}
			con.catenaryVertices=null;
		}
		if(sideAttached==0)
		{
			if(con.hasSameConnectors(lowestCon))
				return Vec3.createVectorHelper(facing==4?1.03125:facing==5?-.03125:.125, .5, facing==2?1.03125:facing==3?-.03125:.125);
			return Vec3.createVectorHelper(facing==4?1.03125:facing==5?-.03125:.875, .5, facing==2?1.03125:facing==3?-.03125:.875);
		}
		else
		{
			double h = sideAttached==1?1.03125:-.03125;
			if(con.hasSameConnectors(lowestCon))
				return Vec3.createVectorHelper(facing>3?.5:.125,h,facing>3?.125:.5);
			return Vec3.createVectorHelper(facing>3?.5:.875,h,facing>3?.875:.5);
		}
	}
}