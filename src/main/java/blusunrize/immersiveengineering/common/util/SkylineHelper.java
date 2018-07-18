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
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.entities.EntitySkylineHook;
import blusunrize.immersiveengineering.common.items.ItemSkyhook;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import static blusunrize.immersiveengineering.api.ApiUtils.getConnectionCatenary;
import static blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection.vertices;

public class SkylineHelper
{
	public static EntitySkylineHook spawnHook(EntityPlayer player, TileEntity start, Connection connection)
	{
		BlockPos cc0 = connection.end==Utils.toCC(start)?connection.start: connection.end;
		BlockPos cc1 = connection.end==Utils.toCC(start)?connection.end: connection.start;
		IImmersiveConnectable iicStart = ApiUtils.toIIC(cc1, player.world);
		IImmersiveConnectable iicEnd = ApiUtils.toIIC(cc0, player.world);
		Vec3d vStart = new Vec3d(cc1);
		Vec3d vEnd = new Vec3d(cc0);

		if(iicStart!=null)
			vStart = Utils.addVectors(vStart, iicStart.getConnectionOffset(connection));
		if(iicEnd!=null)
			vEnd = Utils.addVectors(vEnd, iicEnd.getConnectionOffset(connection));

		Vec3d pos = player.getPositionEyes(0);
		Vec3d across = new Vec3d(vEnd.x-vStart.x, vEnd.y-vStart.y, vEnd.z-vStart.z);
		double t = Utils.getCoeffForMinDistance(pos, vStart, across);
		connection.getSubVertices(player.world);
		pos = connection.getVecAt(t, vStart, across, Math.sqrt(across.x*across.x+across.z*across.z));
		Vec3d[] steps = getConnectionCatenary(connection, vStart, vEnd);
		int tInt = MathHelper.clamp((int)(t*vertices), 0, vertices-1);

		EntitySkylineHook hook = new EntitySkylineHook(player.world, pos.x, pos.y, pos.z, connection, cc0, steps, tInt+1);
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

		for(int xOff = 0; xOff < 2; xOff++)
			for(int yOff = 0; yOff < 3; yOff++)
				for(int zOff = 0; zOff < 2; zOff++)
				{
					Vec3d v = new Vec3d(init.getX()+xOff, init.getY()+yOff, init.getZ()+zOff);
					if(hitbox.contains(v)&&!w.isAirBlock(new BlockPos(v)))
						return true;
				}
		return false;
	}
}
