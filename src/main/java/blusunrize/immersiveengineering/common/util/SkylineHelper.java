package blusunrize.immersiveengineering.common.util;


import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.entities.EntitySkylineHook;
import blusunrize.immersiveengineering.common.items.ItemSkyhook;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Set;

public class SkylineHelper
{
	public static Connection getTargetConnection(World world, BlockPos pos, EntityLivingBase living, Connection invalidCon)
	{
		if(!(world.getTileEntity(pos) instanceof IImmersiveConnectable))
			return null;

		Set<Connection> outputs = ImmersiveNetHandler.INSTANCE.getConnections(world, pos);
		if(outputs!=null && outputs.size()>0)
		{
			Vec3d vec = living.getLookVec();
			vec = vec.normalize();
			Connection line = null;
			for(Connection c : outputs)
				if(c!=null && !c.hasSameConnectors(invalidCon))				{
					if(line==null)
						line = c;
					else
					{
						Vec3d lineVec = new Vec3d(line.end.getX()-line.start.getX(), line.end.getY()-line.start.getY(), line.end.getZ()-line.start.getZ()).normalize();
						Vec3d conVec = new Vec3d(c.end.getX()-c.start.getX(), c.end.getY()-c.start.getY(), c.end.getZ()-c.start.getZ()).normalize();
						if(conVec.distanceTo(vec)<lineVec.distanceTo(vec))
							line = c;
					}
				}
			return line;
		}
		return null;
	}

	public static EntitySkylineHook spawnHook(EntityPlayer player, TileEntity start, Connection connection)
	{
		BlockPos cc0 = connection.end==Utils.toCC(start)?connection.start:connection.end;
		BlockPos cc1 = connection.end==Utils.toCC(start)?connection.end:connection.start;
		IImmersiveConnectable iicStart = ApiUtils.toIIC(cc1, player.world);
		IImmersiveConnectable iicEnd = ApiUtils.toIIC(cc0, player.world);
		Vec3d vStart = new Vec3d(cc1);
		Vec3d vEnd = new Vec3d(cc0);

		if(iicStart!=null)
			vStart = Utils.addVectors(vStart, iicStart.getConnectionOffset(connection));
		if(iicEnd!=null)
			vEnd = Utils.addVectors(vEnd, iicEnd.getConnectionOffset(connection));

		Vec3d[] steps = getConnectionCatenary(connection,vStart,vEnd);

		double dx = (steps[0].xCoord-vStart.xCoord);
		double dy = (steps[0].yCoord-vStart.yCoord);
		double dz = (steps[0].zCoord-vStart.zCoord);
		double d = 1;//connection.length;
		//						Math.sqrt(dx*dx+dz*dz+dy*dy);

		//		Vec3 moveVec = Vec3.createVectorHelper(dx,dy,dz);
//		Vec3 moveVec = Vec3.createVectorHelper(dx/d,dy/d,dz/d);

		EntitySkylineHook hook = new EntitySkylineHook(player.world, vStart.xCoord,vStart.yCoord,vStart.zCoord, connection, cc0, steps);
		float speed = 1;
		if(!player.getActiveItemStack().isEmpty()&&player.getActiveItemStack().getItem() instanceof ItemSkyhook)
			speed = ((ItemSkyhook)player.getActiveItemStack().getItem()).getSkylineSpeed(player.getActiveItemStack());
		Vec3d moveVec = getSubMovementVector(vStart, steps[0], speed);
		hook.motionX = moveVec.xCoord;//*speed;
		hook.motionY = moveVec.yCoord;//*speed;
		hook.motionZ = moveVec.zCoord;//*speed;
		//		hook.motionX = (steps[0].xCoord-cc1.posX)*.5f;
		//		hook.motionY = (steps[0].yCoord-cc1.posY)*.5f;
		//		hook.motionZ = (steps[0].zCoord-cc1.posZ)*.5f;

		//		for(Vec3 v : steps)
		//			living.world.spawnParticle("smoke", v.xCoord,v.yCoord,v.zCoord, 0,0,0 );

		if(!player.world.isRemote)
			player.world.spawnEntity(hook);
		ItemSkyhook.existingHooks.put(player.getName(), hook);
		player.startRiding(hook);
		return hook;
	}

	public static Vec3d[] getConnectionCatenary(Connection connection, Vec3d start, Vec3d end)
	{
		boolean vertical = connection.end.getX()==connection.start.getX() && connection.end.getZ()==connection.start.getZ();

		if(vertical)
			return new Vec3d[]{new Vec3d(end.xCoord, end.yCoord, end.zCoord)};

		double dx = (end.xCoord)-(start.xCoord);
		double dy = (end.yCoord)-(start.yCoord);
		double dz = (end.zCoord)-(start.zCoord);
		double dw = Math.sqrt(dx*dx + dz*dz);
		double k = Math.sqrt(dx*dx + dy*dy + dz*dz) * connection.cableType.getSlack();
		double l = 0;
		int limiter = 0;
		while(!vertical && limiter<300)
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
		Vec3d[] vex = new Vec3d[vertices];

		for(int i=0; i<vertices; i++)
		{
			float n1 = (i+1)/(float)vertices;
			double x1 = 0 + dx * n1;
			double z1 = 0 + dz * n1;
			double y1 = a * Math.cosh((( Math.sqrt(x1*x1+z1*z1) )-p)/a)+q;
			vex[i] = new Vec3d(start.xCoord+x1, start.yCoord+y1, start.zCoord+z1);
		}
		return vex;
	}

	public static Vec3d getSubMovementVector(Vec3d start, Vec3d target, float speed)
	{
		Vec3d movementVec = new Vec3d(target.xCoord-start.xCoord, target.yCoord-start.yCoord, target.zCoord-start.zCoord);
		int lPixel = (int)Math.max(1, (movementVec.lengthVector()/(.125*speed)));
		return new Vec3d(movementVec.xCoord/lPixel, movementVec.yCoord/lPixel, movementVec.zCoord/lPixel);
	}
	public static boolean isInBlock(EntityPlayer player, World w)
	{
		BlockPos init = player.getPosition();
		AxisAlignedBB hitbox = player.getEntityBoundingBox();
		hitbox = new AxisAlignedBB(hitbox.minX-1, hitbox.minY-1, hitbox.minZ-1, hitbox.maxX, hitbox.maxY, hitbox.maxZ);
		
		for (int xOff = 0;xOff<2;xOff++)
			for (int yOff = 0;yOff<3;yOff++)
				for (int zOff = 0;zOff<2;zOff++)
				{
					Vec3d v = new Vec3d(init.getX()+xOff, init.getY()+yOff, init.getZ()+zOff);
					if (hitbox.isVecInside(v)&&!w.isAirBlock(new BlockPos(v)))
						return true;
				}
		return false;
	}
}
