/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;


import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.Connection.CatenaryData;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.common.entities.SkylineHookEntity;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import static blusunrize.immersiveengineering.api.CapabilitySkyhookData.SKYHOOK_USER_DATA;

public class SkylineHelper
{
	private static final double LN_0_98 = Math.log(.98);

	public static void spawnHook(LivingEntity player, Connection connection, Hand hand,
								 boolean limitSpeed)
	{

		if(!player.world.isRemote)
		{
			ConnectionPoint cpA = connection.getEndA();
			ConnectionPoint cpB = connection.getEndB();
			IImmersiveConnectable iicB = ApiUtils.toIIC(cpB, player.world);
			IImmersiveConnectable iicA = ApiUtils.toIIC(cpA, player.world);
			Vec3d vStart = new Vec3d(cpB.getPosition());
			Vec3d vEnd = new Vec3d(cpA.getPosition());

			if(iicB!=null)
				vStart = Utils.addVectors(vStart, iicB.getConnectionOffset(connection, cpB));
			if(iicA!=null)
				vEnd = Utils.addVectors(vEnd, iicA.getConnectionOffset(connection, cpA));

			Vec3d pos = player.getEyePosition(0);
			Vec3d across = new Vec3d(vEnd.x-vStart.x, vEnd.y-vStart.y, vEnd.z-vStart.z);
			double linePos = Utils.getCoeffForMinDistance(pos, vStart, across);
			connection.generateCatenaryData(player.world);
			CatenaryData catData = connection.catData;

			Vec3d playerMovement = new Vec3d(player.getMotion().x, player.getMotion().y,
					player.getMotion().z);
			double slopeAtPos = connection.getSlope(linePos, cpA);
			Vec3d extendedWire;
			if(catData.isVertical())
				extendedWire = new Vec3d(0, catData.getHorLength(), 0);
			else
				extendedWire = new Vec3d(catData.getDeltaX(), slopeAtPos*catData.getHorLength(), catData.getDeltaZ());
			extendedWire = extendedWire.normalize();

			double totalSpeed = playerMovement.dotProduct(extendedWire);
			double horSpeed = totalSpeed/Math.sqrt(1+slopeAtPos*slopeAtPos);
			SkylineHookEntity hook = new SkylineHookEntity(player.world, connection, cpA, linePos, hand, horSpeed, limitSpeed);
			IELogger.logger.info("Speed keeping: Player {}, wire {}, Pos: {}", playerMovement, extendedWire,
					hook.getPositionVector());
			if(hook.isValidPosition(hook.posX, hook.posY, hook.posZ, player))
			{
				double vertSpeed = Math.sqrt(totalSpeed*totalSpeed-horSpeed*horSpeed);
				double speedDiff = player.getMotion().y-vertSpeed;
				if(speedDiff < 0)
				{
					player.fall(fallDistanceFromSpeed(speedDiff), 1.2F);
					player.fallDistance = 0;
				}

				player.world.addEntity(hook);
				player.getCapability(SKYHOOK_USER_DATA, Direction.UP).ifPresent(data -> {
					data.startRiding();
					data.hook = hook;
				});
				player.startRiding(hook);
			}
		}
	}

	public static float fallDistanceFromSpeed(double v)
	{
		double fallTime = Math.log(v/3.92+1)/LN_0_98;//In ticks
		return -(float)(196-3.92*fallTime-194.04*Math.pow(.98, fallTime-.5));
	}

	public static List<VoxelShape> getCollisionBoxes(@Nullable Entity entityIn, AxisAlignedBB aabb, World w,
													 Collection<BlockPos> ignored)
	{
		List<VoxelShape> list = Lists.newArrayList();
		getBlockCollisionBoxes(entityIn, aabb, list, w, ignored);
		w.getEmptyCollisionShapes(entityIn, aabb, ImmutableSet.of()).forEach(list::add);
		return list;
	}

	//Mostly taken from IWorldReader, added the ignored parameter
	public static void getBlockCollisionBoxes(@Nullable Entity entityIn, AxisAlignedBB aabb, @Nonnull List<VoxelShape> outList,
											  World w, Collection<BlockPos> ignored)
	{
		int minX = MathHelper.floor(aabb.minX-1.0E-7D)-1;
		int maxX = MathHelper.floor(aabb.maxX+1.0E-7D)+1;
		int minY = MathHelper.floor(aabb.minY-1.0E-7D)-1;
		int maxY = MathHelper.floor(aabb.maxY+1.0E-7D)+1;
		int minZ = MathHelper.floor(aabb.minZ-1.0E-7D)-1;
		int maxZ = MathHelper.floor(aabb.maxZ+1.0E-7D)+1;
		final ISelectionContext selectionCtx = entityIn==null?ISelectionContext.dummy(): ISelectionContext.forEntity(entityIn);
		final CubeCoordinateIterator it = new CubeCoordinateIterator(minX, minY, minZ, maxX, maxY, maxZ);
		final BlockPos.MutableBlockPos currPos = new BlockPos.MutableBlockPos();
		final VoxelShape searchShape = VoxelShapes.create(aabb);
		StreamSupport.stream(new AbstractSpliterator<VoxelShape>(Long.MAX_VALUE, Spliterator.NONNULL|Spliterator.IMMUTABLE)
		{
			boolean isEntityNull = entityIn==null;

			public boolean tryAdvance(Consumer<? super VoxelShape> add)
			{
				if(!this.isEntityNull)
				{
					assert (entityIn!=null);
					this.isEntityNull = true;
					VoxelShape worldBorder = w.getWorldBorder().getShape();
					boolean veryOutside = VoxelShapes.compare(worldBorder, VoxelShapes.create(entityIn.getBoundingBox().shrink(1.0E-7D)), IBooleanFunction.AND);
					boolean nearlyOutside = VoxelShapes.compare(worldBorder, VoxelShapes.create(entityIn.getBoundingBox().grow(1.0E-7D)), IBooleanFunction.AND);
					if(!veryOutside&&nearlyOutside)
					{
						add.accept(worldBorder);
						return true;
					}
				}

				VoxelShape voxelshape3;
				while(true)
				{
					if(!it.hasNext())
					{
						return false;
					}

					int currX = it.getX();
					int currY = it.getY();
					int currZ = it.getZ();
					int numBounderies = it.numBoundariesTouched();
					if(numBounderies!=3)
					{
						int chunkX = currX >> 4;
						int chunkZ = currZ >> 4;
						IChunk ichunk = w.getChunk(chunkX, chunkZ, w.getChunkStatus(), false);
						if(ichunk!=null)
						{
							currPos.setPos(currX, currY, currZ);
							BlockState blockstate = ichunk.getBlockState(currPos);
							if((numBounderies!=1||blockstate.isCollisionShapeLargerThanFullBlock())&&
									(numBounderies!=2||blockstate.getBlock()==Blocks.MOVING_PISTON))
							{
								VoxelShape blockShape = blockstate.getCollisionShape(w, currPos, selectionCtx);
								voxelshape3 = blockShape.withOffset((double)currX, (double)currY, (double)currZ);
								if(VoxelShapes.compare(searchShape, voxelshape3, IBooleanFunction.AND))
								{
									break;
								}
							}
						}
					}
				}

				add.accept(voxelshape3);
				return true;
			}
		}, false)
				.forEach(outList::add);
	}
}
