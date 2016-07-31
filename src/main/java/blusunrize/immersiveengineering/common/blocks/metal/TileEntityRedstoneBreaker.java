package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.Vec3d;

public class TileEntityRedstoneBreaker extends TileEntityBreakerSwitch implements ITickable
{
	Connection primaryConnection;
	@Override
	public void update()
	{
		if (!worldObj.isRemote&&(worldObj.isBlockIndirectlyGettingPowered(getPos())>0)==active)
		{
			active = !active;
			ImmersiveNetHandler.INSTANCE.resetCachedIndirectConnections();
		}
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
	public float[] getBlockBounds()
	{
		if(facing==EnumFacing.DOWN)
			return new float[]{0,0,.125f, 1,1,.875f};
		if(facing==EnumFacing.UP)
			return new float[]{0,0,.125f, 1,1,.875f};
		return new float[]{0,.125f,0, 1,.875f,1};
	}

	@Override
	public Vec3d getRaytraceOffset(IImmersiveConnectable link)
	{
		if(sideAttached==0)
			return new Vec3d(facing==EnumFacing.WEST?1:facing==EnumFacing.EAST?0:.5, .5, facing==EnumFacing.NORTH?1:facing==EnumFacing.SOUTH?0:.5);
		return new Vec3d(.5,facing==EnumFacing.DOWN?1:0,.5);
	}
	@Override
	public Vec3d getConnectionOffset(Connection con)
	{
		int lowestDif=100;
		Connection lowestCon=null;
		for(Connection otherCon : ImmersiveNetHandler.INSTANCE.getConnections(worldObj, getPos()))
		{
			int xDif = (otherCon==null||otherCon.start==null||otherCon.end==null)?0: (otherCon.start.equals(getPos())&&otherCon.end!=null)? otherCon.end.getX()-getPos().getX(): (otherCon.end.equals(getPos())&& otherCon.start!=null)?otherCon.start.getX()-getPos().getX(): 0;
			int zDif = (otherCon==null||otherCon.start==null||otherCon.end==null)?0: (otherCon.start.equals(getPos())&&otherCon.end!=null)? otherCon.end.getZ()-getPos().getZ(): (otherCon.end.equals(getPos())&& otherCon.start!=null)?otherCon.start.getZ()-getPos().getZ(): 0;
			int dif = facing.getAxis()==Axis.X?zDif:xDif;
			if(lowestCon==null || dif<lowestDif)
			{
				lowestDif = dif;
				lowestCon = otherCon;
			}
			con.catenaryVertices=null;
		}
		if(facing.getAxis()==Axis.Y)
		{
			double h = facing==EnumFacing.DOWN?1.03125:-.03125;
			return new Vec3d(con.hasSameConnectors(lowestCon)?.125:.875,h,.5);
			//	return new Vec3(facing.getAxis()==Axis.X?.5:.125,h,facing.getAxis()==Axis.X?.125:.5);
			//	return new Vec3(facing.getAxis()==Axis.X?.5:.875,h,facing.getAxis()==Axis.X?.875:.5);
		}
		else
		{
			if(con.hasSameConnectors(lowestCon))
				return new Vec3d(facing==EnumFacing.WEST?1.03125:facing==EnumFacing.EAST?-.03125:.125, .5, facing==EnumFacing.NORTH?1.03125:facing==EnumFacing.SOUTH?-.03125:.125);
			return new Vec3d(facing==EnumFacing.WEST?1.03125:facing==EnumFacing.EAST?-.03125:.875, .5, facing==EnumFacing.NORTH?1.03125:facing==EnumFacing.SOUTH?-.03125:.875);
		}
	}
}