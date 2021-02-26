/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;


import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.Connection.CatenaryData;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.common.entities.SkylineHookEntity;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.CubeCoordinateIterator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

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
			GlobalWireNetwork global = GlobalWireNetwork.getNetwork(player.world);
			ConnectionPoint cpA = connection.getEndA();
			ConnectionPoint cpB = connection.getEndB();
			IImmersiveConnectable iicB = global.getExistingConnector(cpB);
			IImmersiveConnectable iicA = global.getExistingConnector(cpA);
			Vector3d vStart = Vector3d.copy(cpA.getPosition());
			Vector3d vEnd = Vector3d.copy(cpB.getPosition());

			if(iicB!=null)
				vStart = Utils.addVectors(vStart, iicB.getConnectionOffset(connection, cpB));
			if(iicA!=null)
				vEnd = Utils.addVectors(vEnd, iicA.getConnectionOffset(connection, cpA));

			Vector3d pos = player.getEyePosition(0);
			Vector3d across = new Vector3d(vEnd.x-vStart.x, vEnd.y-vStart.y, vEnd.z-vStart.z);
			double linePos = Utils.getCoeffForMinDistance(pos, vStart, across);
			connection.generateCatenaryData(player.world);
			CatenaryData catData = connection.getCatenaryData();

			Vector3d playerMovement = new Vector3d(player.getMotion().x, player.getMotion().y,
					player.getMotion().z);
			double slopeAtPos = connection.getSlope(linePos, cpA);
			Vector3d extendedWire;
			if(catData.isVertical())
				extendedWire = new Vector3d(0, catData.getHorLength(), 0);
			else
				extendedWire = new Vector3d(catData.getDeltaX(), slopeAtPos*catData.getHorLength(), catData.getDeltaZ());
			extendedWire = extendedWire.normalize();

			double totalSpeed = playerMovement.dotProduct(extendedWire);
			double horSpeed = totalSpeed/Math.sqrt(1+slopeAtPos*slopeAtPos);
			SkylineHookEntity hook = new SkylineHookEntity(player.world, connection, cpA, linePos, hand, horSpeed, limitSpeed);
			IELogger.logger.info("Speed keeping: Player {}, wire {}, Pos: {}", playerMovement, extendedWire,
					hook.getPositionVec());
			if(hook.isValidPosition(hook.getPosX(), hook.getPosY(), hook.getPosZ(), player))
			{
				double vertSpeed = Math.sqrt(totalSpeed*totalSpeed-horSpeed*horSpeed);
				double speedDiff = player.getMotion().y-vertSpeed;
				if(speedDiff < 0)
				{
					player.onLivingFall(fallDistanceFromSpeed(speedDiff), 1.2F);
					player.fallDistance = 0;
				}

				player.world.addEntity(hook);
				player.getCapability(SKYHOOK_USER_DATA, Direction.UP).ifPresent(data -> {
					data.startRiding();
					data.hook = hook;
				});
				player.startRiding(hook);
				IELogger.logger.debug("Started riding");
			}
			else
			{
				IELogger.logger.debug("Invalid pos");
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
		w.getCollisionShapes(entityIn, aabb).forEach(list::add);
		return list;
	}

	//Mostly taken from ICollisionReader, added the ignored parameter
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
		final BlockPos.Mutable currPos = new BlockPos.Mutable();
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

				while(true)
				{
					if(!it.hasNext())
						return false;

					int currX = it.getX();
					int currY = it.getY();
					int currZ = it.getZ();
					int numBounderies = it.numBoundariesTouched();
					if(numBounderies!=3)
					{
						int chunkX = currX >> 4;
						int chunkZ = currZ >> 4;
						IBlockReader iblockreader = w.getBlockReader(chunkX, chunkZ);
						if(iblockreader!=null)
						{
							currPos.setPos(currX, currY, currZ);
							BlockState blockstate = iblockreader.getBlockState(currPos);
							if((numBounderies!=1||blockstate.isCollisionShapeLargerThanFullBlock())&&
									(numBounderies!=2||blockstate.getBlock()==Blocks.MOVING_PISTON)&&
									!ignored.contains(currPos)
							)
							{
								VoxelShape blockShape = blockstate.getCollisionShape(w, currPos, selectionCtx);
								VoxelShape blockShapeWithOffset = blockShape.withOffset(currX, currY, currZ);
								if(VoxelShapes.compare(searchShape, blockShapeWithOffset, IBooleanFunction.AND))
								{
									add.accept(blockShapeWithOffset);
									break;
								}
							}
						}
					}
				}

				return true;
			}
		}, false)
				.forEach(outList::add);
	}
}
