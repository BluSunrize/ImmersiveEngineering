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
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static blusunrize.immersiveengineering.api.CapabilitySkyhookData.SKYHOOK_USER_DATA;

public class SkylineHelper
{
	private static final double LN_0_98 = Math.log(.98);

	public static void spawnHook(EntityLivingBase player, TileEntity start, Connection connection, EnumHand hand,
								 boolean limitSpeed)
	{

		if(!player.world.isRemote)
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
			Vec3d extendedWire;
			if(connection.vertical)
				extendedWire = new Vec3d(0, connection.horizontalLength, 0);
			else
				extendedWire = new Vec3d(connection.across.x, slopeAtPos*connection.horizontalLength, connection.across.z);
			extendedWire = extendedWire.normalize();

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

	//Mostly taken from World
	public static List<AxisAlignedBB> getCollisionBoxes(@Nullable Entity entityIn, AxisAlignedBB aabb, World w,
														Collection<BlockPos> ignored)
	{
		List<AxisAlignedBB> list = Lists.<AxisAlignedBB>newArrayList();
		getBlockCollisionBoxes(entityIn, aabb, list, w, ignored);

		if(entityIn!=null)
		{
			List<Entity> entities = w.getEntitiesWithinAABBExcludingEntity(entityIn, aabb.grow(0.25D));

			for(Entity entity : entities)
			{
				if(!entityIn.isRidingSameEntity(entity))
				{
					AxisAlignedBB entityBB = entity.getCollisionBoundingBox();

					if(entityBB!=null&&entityBB.intersects(aabb))
					{
						list.add(entityBB);
					}

					entityBB = entityIn.getCollisionBox(entity);

					if(entityBB!=null&&entityBB.intersects(aabb))
					{
						list.add(entityBB);
					}
				}
			}
		}
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.GetCollisionBoxesEvent(w, entityIn, aabb, list));
		return list;
	}

	public static void getBlockCollisionBoxes(@Nullable Entity entityIn, AxisAlignedBB aabb, @Nonnull List<AxisAlignedBB> outList,
											  World w, Collection<BlockPos> ignored)
	{
		int minX = MathHelper.floor(aabb.minX)-1;
		int maxX = MathHelper.ceil(aabb.maxX)+1;
		int minY = MathHelper.floor(aabb.minY)-1;
		int maxY = MathHelper.ceil(aabb.maxY)+1;
		int minZ = MathHelper.floor(aabb.minZ)-1;
		int maxZ = MathHelper.ceil(aabb.maxZ)+1;
		WorldBorder worldborder = w.getWorldBorder();
		boolean outsideWorld = entityIn!=null&&entityIn.isOutsideBorder();
		boolean insideWorld = entityIn!=null&&w.isInsideWorldBorder(entityIn);
		IBlockState outsideState = Blocks.STONE.getDefaultState();
		BlockPos.PooledMutableBlockPos mutPos = BlockPos.PooledMutableBlockPos.retain();

		try
		{
			for(int x = minX; x < maxX; ++x)
			{
				for(int z = minZ; z < maxZ; ++z)
				{
					boolean xBorder = x==minX||x==maxX-1;
					boolean zBorder = z==minZ||z==maxZ-1;

					if((!xBorder||!zBorder)&&w.isBlockLoaded(mutPos.setPos(x, 64, z)))
					{
						for(int y = minY; y < maxY; ++y)
						{
							if(!xBorder&&!zBorder||y!=maxY-1)
							{
								if(entityIn!=null&&outsideWorld==insideWorld)
								{
									entityIn.setOutsideBorder(!insideWorld);
								}

								mutPos.setPos(x, y, z);
								if(!ignored.contains(mutPos))
								{
									IBlockState currState;

									if(!worldborder.contains(mutPos)&&insideWorld)
										currState = outsideState;
									else
										currState = w.getBlockState(mutPos);

									currState.addCollisionBoxToList(w, mutPos, aabb, outList, entityIn, false);
								}
							}
						}
					}
				}
			}
		} finally
		{
			mutPos.release();
		}
	}
}
