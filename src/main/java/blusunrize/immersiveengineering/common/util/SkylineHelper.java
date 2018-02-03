/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

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
import org.apache.commons.lang3.tuple.Triple;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static blusunrize.immersiveengineering.api.ApiUtils.getConnectionCatenary;

public class SkylineHelper
{
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


		/* Reasoning for the formula for pos (below): pos should be the point on the catenary (horizontally) closest to the player position
		A conn start, B conn across, C player pos
		A+tB
		C
		C-A=:D
		D**2=(Cx-Ax-tBx)**2+(Cz-Az-tBz)**2=(Dx-tBx)**2+(Dz-tBz)**2
		=Dx**2-2tDxBx+t**2Bx**2+Dz**2-2tDzBz+t**2Bz**2
		=t**2(Bx**2+Bz**2)-(2DxBx+2DzBz)t+Dz**2+Dx**2

		D**2'=(2Bx**2+2Bz**2)*t-2DxBx+2DzBz=0
		t=(DxBx+DzBz)/(Bx^2+Bz^2)
		 */
		Vec3d pos = player.getPositionEyes(0);
		Vec3d delta = pos.subtract(vStart);
		Vec3d across = new Vec3d(vEnd.x-vStart.x, 0, vEnd.z-vStart.z);
		double t = (delta.x*across.x+delta.z*across.z)/(across.x*across.x+across.z*across.z);
		pos = connection.getVecAt(t, vStart, across, across.lengthVector());
		int tInt = (int)(t*16);

		Vec3d[] steps = getConnectionCatenary(connection, vStart, vEnd);
		EntitySkylineHook hook = new EntitySkylineHook(player.world, pos.x,pos.y,pos.z, connection, cc0, steps, tInt+1);
		float speed = 1;
		if(!player.getActiveItemStack().isEmpty()&&player.getActiveItemStack().getItem() instanceof ItemSkyhook)
			speed = ((ItemSkyhook)player.getActiveItemStack().getItem()).getSkylineSpeed(player.getActiveItemStack());
		Vec3d moveVec = getSubMovementVector(steps[tInt], steps[tInt+1], speed);
		hook.motionX = moveVec.x;//*speed;
		hook.motionY = moveVec.y;//*speed;
		hook.motionZ = moveVec.z;//*speed;
		//		hook.motionX = (steps[0].x-cc1.posX)*.5f;
		//		hook.motionY = (steps[0].y-cc1.posY)*.5f;
		//		hook.motionZ = (steps[0].z-cc1.posZ)*.5f;

		//		for(Vec3 v : steps)
		//			living.world.spawnParticle("smoke", v.x,v.y,v.z, 0,0,0 );

		if(!player.world.isRemote)
			player.world.spawnEntity(hook);
		ItemSkyhook.existingHooks.put(player.getName(), hook);
		player.startRiding(hook);
		return hook;
	}

	public static Vec3d getSubMovementVector(Vec3d start, Vec3d target, float speed)
	{
		Vec3d movementVec = new Vec3d(target.x-start.x, target.y-start.y, target.z-start.z);
		int lPixel = (int)Math.max(1, (movementVec.lengthVector()/(.125*speed)));
		return new Vec3d(movementVec.x/lPixel, movementVec.y/lPixel, movementVec.z/lPixel);
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
					if (hitbox.contains(v)&&!w.isAirBlock(new BlockPos(v)))
						return true;
				}
		return false;
	}

	public static Connection getTargetConnection(World world, EntityPlayer player, Connection ignored)
	{
		double py = player.posY + player.getEyeHeight();
		BlockPos head = new BlockPos(player.posX, py, player.posZ);
		Connection ret = null;
		for (int i = 0;i<2;i++)
		{
			Map<BlockPos, Set<Triple<Connection, Vec3d, Vec3d>>> inDim = (i == 0 ? ImmersiveNetHandler.INSTANCE.blockInWire : ImmersiveNetHandler.INSTANCE.blockNearWire)
					.lookup(player.dimension);
			if (inDim != null && inDim.containsKey(head))
			{
				for (Triple<Connection, Vec3d, Vec3d> connectionVec3dVec3dTriple : inDim.get(head))
				{
					Connection c = connectionVec3dVec3dTriple.getLeft();
					if (ignored==null||!c.hasSameConnectors(ignored))
					{
						ret = c;
						break;
					}
				}
			}
		}
		if (ret!=null)
		{
			Vec3d across = new Vec3d(ret.end).subtract(new Vec3d(ret.start));
			if (across.dotProduct(player.getLookVec())<0)
			{
				ret = ImmersiveNetHandler.INSTANCE.getReverseConnection(world.provider.getDimension(), ret);
			}
		}
		return ret;
	}
}
