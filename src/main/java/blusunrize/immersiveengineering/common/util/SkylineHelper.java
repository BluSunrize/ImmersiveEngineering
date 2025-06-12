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
import blusunrize.immersiveengineering.api.wires.utils.WireUtils;
import blusunrize.immersiveengineering.common.entities.SkyhookUserData;
import blusunrize.immersiveengineering.common.entities.SkylineHookEntity;
import blusunrize.immersiveengineering.common.register.IEDataAttachments;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

public class SkylineHelper
{
	private static final double LN_0_98 = Math.log(.98);

	public static void spawnHook(LivingEntity player, Connection connection, InteractionHand hand,
								 boolean limitSpeed, float slopeModifier)
	{
		if(!player.level().isClientSide)
		{
			GlobalWireNetwork global = GlobalWireNetwork.getNetwork(player.level());
			ConnectionPoint cpA = connection.getEndA();
			ConnectionPoint cpB = connection.getEndB();
			IImmersiveConnectable iicB = global.getExistingConnector(cpB);
			IImmersiveConnectable iicA = global.getExistingConnector(cpA);
			Vec3 vStart = Vec3.atLowerCornerOf(cpA.position());
			Vec3 vEnd = Vec3.atLowerCornerOf(cpB.position());

			if(iicB!=null)
				vStart = vStart.add(iicB.getConnectionOffset(cpB, cpA, connection.type));
			if(iicA!=null)
				vEnd = vEnd.add(iicA.getConnectionOffset(cpA, cpB, connection.type));

			Vec3 pos = player.getEyePosition(0);
			Vec3 across = new Vec3(vEnd.x-vStart.x, vEnd.y-vStart.y, vEnd.z-vStart.z);
			double linePos = WireUtils.getCoeffForMinDistance(pos, vStart, across);
			CatenaryData catData = connection.getCatenaryData();

			Vec3 playerMovement = new Vec3(player.getDeltaMovement().x, player.getDeltaMovement().y,
					player.getDeltaMovement().z);
			double slopeAtPos = connection.getSlope(linePos, cpA);
			Vec3 extendedWire;
			if(catData.isVertical())
				extendedWire = new Vec3(0, catData.horLength(), 0);
			else
				extendedWire = new Vec3(catData.getDeltaX(), slopeAtPos*catData.horLength(), catData.getDeltaZ());
			extendedWire = extendedWire.normalize();

			double totalSpeed = playerMovement.dot(extendedWire);
			double horSpeed = totalSpeed/(Math.sqrt(1+slopeAtPos*slopeAtPos)*slopeModifier);
			SkylineHookEntity hook = new SkylineHookEntity(player.level(), connection, cpA, linePos, hand, horSpeed, limitSpeed, slopeModifier);
			IELogger.logger.info("Speed keeping: Player {}, wire {}, Pos: {}", playerMovement, extendedWire,
					hook.position());
			if(hook.isValidPosition(hook.getX(), hook.getY(), hook.getZ(), player))
			{
				double vertSpeed = Math.sqrt(totalSpeed*totalSpeed-horSpeed*horSpeed);
				double speedDiff = player.getDeltaMovement().y-vertSpeed;
				if(speedDiff < 0)
				{
					// todo: Add an upgrade to nullify this
					float fallDamageMod = 0.4f;
					player.causeFallDamage(fallDistanceFromSpeed(speedDiff), fallDamageMod, player.damageSources().fall());
					player.fallDistance = 0;
				}

				player.level().addFreshEntity(hook);
				SkyhookUserData data = player.getData(IEDataAttachments.SKYHOOK_USER.get());
				data.startRiding();
				data.hook = hook;
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

	public static List<VoxelShape> getCollisionBoxes(@Nullable Entity entityIn, AABB aabb, Level w,
													 Collection<BlockPos> ignored)
	{
		List<VoxelShape> list = Lists.newArrayList();
		getBlockCollisionBoxes(entityIn, aabb, list, w, ignored);
		w.getBlockCollisions(entityIn, aabb).forEach(list::add);
		return list;
	}

	//Mostly taken from ICollisionReader, added the ignored parameter
	public static void getBlockCollisionBoxes(@Nullable Entity entityIn, AABB aabb, @Nonnull List<VoxelShape> outList,
											  Level w, Collection<BlockPos> ignored)
	{
		int minX = Mth.floor(aabb.minX-1.0E-7D)-1;
		int maxX = Mth.floor(aabb.maxX+1.0E-7D)+1;
		int minY = Mth.floor(aabb.minY-1.0E-7D)-1;
		int maxY = Mth.floor(aabb.maxY+1.0E-7D)+1;
		int minZ = Mth.floor(aabb.minZ-1.0E-7D)-1;
		int maxZ = Mth.floor(aabb.maxZ+1.0E-7D)+1;
		final CollisionContext selectionCtx = entityIn==null?CollisionContext.empty(): CollisionContext.of(entityIn);
		final Cursor3D it = new Cursor3D(minX, minY, minZ, maxX, maxY, maxZ);
		final BlockPos.MutableBlockPos currPos = new BlockPos.MutableBlockPos();
		final VoxelShape searchShape = Shapes.create(aabb);
		StreamSupport.stream(new AbstractSpliterator<VoxelShape>(Long.MAX_VALUE, Spliterator.NONNULL|Spliterator.IMMUTABLE)
				{
					boolean isEntityNull = entityIn==null;

					public boolean tryAdvance(Consumer<? super VoxelShape> add)
					{
						if(!this.isEntityNull)
						{
							assert (entityIn!=null);
							this.isEntityNull = true;
							VoxelShape worldBorder = w.getWorldBorder().getCollisionShape();
							boolean veryOutside = Shapes.joinIsNotEmpty(worldBorder, Shapes.create(entityIn.getBoundingBox().deflate(1.0E-7D)), BooleanOp.AND);
							boolean nearlyOutside = Shapes.joinIsNotEmpty(worldBorder, Shapes.create(entityIn.getBoundingBox().inflate(1.0E-7D)), BooleanOp.AND);
							if(!veryOutside&&nearlyOutside)
							{
								add.accept(worldBorder);
								return true;
							}
						}

						while(true)
						{
							if(!it.advance())
								return false;

							int currX = it.nextX();
							int currY = it.nextY();
							int currZ = it.nextZ();
							int numBounderies = it.getNextType();
							if(numBounderies!=3)
							{
								int chunkX = currX>>4;
								int chunkZ = currZ>>4;
								BlockGetter iblockreader = w.getChunkForCollisions(chunkX, chunkZ);
								if(iblockreader!=null)
								{
									currPos.set(currX, currY, currZ);
									BlockState blockstate = iblockreader.getBlockState(currPos);
									if((numBounderies!=1||blockstate.hasLargeCollisionShape())&&
											(numBounderies!=2||blockstate.getBlock()==Blocks.MOVING_PISTON)&&
											!ignored.contains(currPos)
									)
									{
										VoxelShape blockShape = blockstate.getCollisionShape(w, currPos, selectionCtx);
										VoxelShape blockShapeWithOffset = blockShape.move(currX, currY, currZ);
										if(Shapes.joinIsNotEmpty(searchShape, blockShapeWithOffset, BooleanOp.AND))
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
