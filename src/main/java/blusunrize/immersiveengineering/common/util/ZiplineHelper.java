package blusunrize.immersiveengineering.common.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.entities.EntityZiplineHook;
import blusunrize.immersiveengineering.common.items.ItemSkyHook;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ZiplineHelper
{


	@SideOnly(Side.CLIENT)
	public static Set<Connection> grabableConnections = new HashSet();

	public static Connection getTargetConnection(World world, int x, int y, int z, EntityLivingBase living, Connection invalidCon)
	{
		if(!(world.getTileEntity(x,y,z) instanceof IImmersiveConnectable))
			return null;

		List<Connection> outputs = ImmersiveNetHandler.INSTANCE.getConnections(world, new ChunkCoordinates(x,y,z));
		if(outputs.size()>0)
		{
			Vec3 vec = living.getLookVec();
			vec = vec.normalize();
			Connection line = null;
			for(Connection c : outputs)
				if(c!=null && !c.equals(invalidCon))
				{
					if(line==null)
						line = c;
					else
					{
						Vec3 lineVec = Vec3.createVectorHelper(line.end.posX-line.start.posX, line.end.posY-line.start.posY, line.end.posZ-line.start.posZ).normalize();
						Vec3 conVec = Vec3.createVectorHelper(c.end.posX-c.start.posX, c.end.posY-c.start.posY, c.end.posZ-c.start.posZ).normalize();
						if(conVec.distanceTo(vec)<lineVec.distanceTo(vec))
							line = c;
					}
				}
			return line;
		}
		return null;
	}

	public static EntityZiplineHook spawnHook(EntityLivingBase living, TileEntity start, Connection connection)
	{
		ChunkCoordinates cc0 = connection.end==Utils.toCC(start)?connection.start:connection.end;
		ChunkCoordinates cc1 = connection.end==Utils.toCC(start)?connection.end:connection.start;
		IImmersiveConnectable iicStart = Utils.toIIC(cc1, living.worldObj);
		IImmersiveConnectable iicEnd = Utils.toIIC(cc0, living.worldObj);
		Vec3 vStart = Vec3.createVectorHelper(cc1.posX,cc1.posY,cc1.posZ);
		Vec3 vEnd = Vec3.createVectorHelper(cc0.posX,cc0.posY,cc0.posZ);

		if(iicStart!=null)
			vStart = Utils.addVectors(vStart, iicStart.getConnectionOffset(connection));
		if(iicEnd!=null)
			vEnd = Utils.addVectors(vEnd, iicEnd.getConnectionOffset(connection));
		
		Vec3[] steps = getConnectionCatenary(connection,vStart,vEnd);

		double dx = (steps[0].xCoord-vStart.xCoord);
		double dy = (steps[0].yCoord-vStart.yCoord);
		double dz = (steps[0].zCoord-vStart.zCoord);
		double d = Math.sqrt(dx*dx+dz*dz+dy*dy);

		Vec3 moveVec = Vec3.createVectorHelper(dx/d,dy/d,dz/d);


		EntityZiplineHook hook = new EntityZiplineHook(living.worldObj, vStart.xCoord,vStart.yCoord,vStart.zCoord, connection, cc0, steps);

		hook.motionX = moveVec.xCoord*.5f;
		hook.motionY = moveVec.yCoord*.5f;
		hook.motionZ = moveVec.zCoord*.5f;
		//		hook.motionX = (steps[0].xCoord-cc1.posX)*.5f;
		//		hook.motionY = (steps[0].yCoord-cc1.posY)*.5f;
		//		hook.motionZ = (steps[0].zCoord-cc1.posZ)*.5f;

//		for(Vec3 v : steps)
//			living.worldObj.spawnParticle("smoke", v.xCoord,v.yCoord,v.zCoord, 0,0,0 );

				if(!living.worldObj.isRemote)
					living.worldObj.spawnEntityInWorld(hook);
				ItemSkyHook.existingHooks.put(living.getCommandSenderName(), hook);
				living.mountEntity(hook);
		return hook;
	}

	public static Vec3[] getConnectionCatenary(Connection connection, Vec3 start, Vec3 end)
	{
		boolean vertical = connection.end.posX==connection.start.posX && connection.end.posZ==connection.start.posZ;

		if(vertical)
			return new Vec3[]{Vec3.createVectorHelper(end.xCoord, end.yCoord, end.zCoord)};

		double dx = (end.xCoord)-(start.xCoord);
		double dy = (end.yCoord)-(start.yCoord);
		double dz = (end.zCoord)-(start.zCoord);
		double dw = Math.sqrt(dx*dx + dz*dz);
		double k = Math.sqrt(dx*dx + dy*dy + dz*dz) * 1.005;
		double l = 0;
		int limiter = 0;
		while(!vertical && true && limiter<300)
		{
			limiter++;
			l += 0.01;
			if (Math.sinh(l)/l >= Math.sqrt(k*k - dy*dy)/dw)
				break;
		}
		double a = dw/2/l;
		double p = (0+dw-a*Math.log((k+dy)/(k-dy)))*0.5;
		double q = (dy+0-k*Math.cosh(l)/Math.sinh(l))*0.5;

		int vertices = 16;
		Vec3[] vex = new Vec3[vertices];

		for(int i=0; i<vertices; i++)
		{
			float n1 = (i+1)/(float)vertices;
			double x1 = 0 + dx * n1;
			double z1 = 0 + dz * n1;
			double y1 = a * Math.cosh((( Math.sqrt(x1*x1+z1*z1) )-p)/a)+q;
			vex[i] = Vec3.createVectorHelper(start.xCoord+x1, start.yCoord+y1, start.zCoord+z1);
		}
		return vex;
	}
}
