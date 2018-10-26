/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;


import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.CapabilitySkyhookData.SkyhookUserData;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.entities.EntitySkylineHook;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Objects;

import static blusunrize.immersiveengineering.api.CapabilitySkyhookData.SKYHOOK_USER_DATA;

public class SkylineHelper
{
	private static final double LN_0_98 = Math.log(.98);

	public static void spawnHook(EntityLivingBase player, TileEntity start, Connection connection, EnumHand hand,
								 boolean limitSpeed)
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
		double linePos = Utils.getCoeffForMinDistance(pos, vStart, across);
		connection.getSubVertices(player.world);

		Vec3d playerMovement = new Vec3d(player.motionX, player.motionY,
				player.motionZ);
		double slopeAtPos = connection.getSlopeAt(linePos);
		Vec3d extendedWire = new Vec3d(connection.across.x, slopeAtPos*connection.horizontalLength, connection.across.z);
		extendedWire = extendedWire.normalize();

		if(!player.world.isRemote)
		{
			double totalSpeed = playerMovement.dotProduct(extendedWire);
			double horSpeed = totalSpeed/Math.sqrt(1+slopeAtPos*slopeAtPos);
			EntitySkylineHook hook = new EntitySkylineHook(player.world, connection, linePos, hand, horSpeed, limitSpeed);
			IELogger.logger.info("Speed keeping: Player {}, wire {}, Pos: {}", playerMovement, extendedWire,
					hook.getPositionVector());
			if(hook.isValidPosition(hook.posX, hook.posY, hook.posZ, player))
			{
				double vertSpeed = Math.sqrt(totalSpeed*totalSpeed-horSpeed*horSpeed);
				double speedDiff = player.motionY-vertSpeed;
				if(speedDiff < 0)
				{
					player.fall(fallDistanceFromSpeed(speedDiff), 1.2F);
					player.fallDistance = 0;
				}

				player.world.spawnEntity(hook);
				SkyhookUserData data = Objects.requireNonNull(player.getCapability(SKYHOOK_USER_DATA, EnumFacing.UP));
				data.startRiding();
				data.hook = hook;
				player.startRiding(hook);
			}
		}
	}

	public static float fallDistanceFromSpeed(double v)
	{
		double fallTime = Math.log(v/3.92+1)/LN_0_98;//In ticks
		return -(float)(196-3.92*fallTime-194.04*Math.pow(.98, fallTime-.5));
	}

	public static Vec3d getSubMovementVector(Vec3d start, Vec3d target, float speed)
	{
		Vec3d movementVec = new Vec3d(target.x-start.x, target.y-start.y, target.z-start.z);
		int lPixel = (int)Math.max(1, (movementVec.length()/(.125*speed)));
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
