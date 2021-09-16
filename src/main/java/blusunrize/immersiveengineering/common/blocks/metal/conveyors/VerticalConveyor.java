/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.tool.conveyor.BasicConveyorType;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.IConveyorBlockEntity;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorBelt;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorType;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.client.render.conveyor.VerticalConveyorRender;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.mixin.accessors.ItemEntityAccess;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.List;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

/**
 * @author BluSunrize - 20.08.2016
 */
public class VerticalConveyor extends ConveyorBase
{
	public static final ResourceLocation NAME = new ResourceLocation(MODID, "vertical");
	public static ResourceLocation texture_on = new ResourceLocation("immersiveengineering:block/conveyor/vertical");
	public static ResourceLocation texture_off = new ResourceLocation("immersiveengineering:block/conveyor/vertical_off");
	public static IConveyorType<VerticalConveyor> TYPE = new BasicConveyorType<>(
			NAME, false, true, VerticalConveyor::new, () -> new VerticalConveyorRender(texture_on, texture_off)
	);

	public VerticalConveyor(BlockEntity tile)
	{
		super(tile);
	}

	@Override
	public IConveyorType<VerticalConveyor> getType()
	{
		return TYPE;
	}

	@Override
	public boolean changeConveyorDirection()
	{
		return false;
	}

	@Override
	public boolean setConveyorDirection(ConveyorDirection dir)
	{
		return false;
	}

	@Override
	public Direction[] sigTransportDirections()
	{
		return new Direction[]{Direction.UP, getFacing()};
	}

	@Override
	public Vec3 getDirection(Entity entity, boolean outputBlocked)
	{
		BlockPos posWall = getBlockEntity().getBlockPos().relative(getFacing());
		double d = .625+entity.getBbWidth();
		double distToWall = Math.abs((getFacing().getAxis()==Axis.Z?posWall.getZ(): posWall.getX())+.5-(getFacing().getAxis()==Axis.Z?entity.getZ(): entity.getX()));
		if(distToWall > d)
			return super.getDirection(entity, outputBlocked);

		double vBase = entity instanceof LivingEntity?1.5: 1.15;
		double distY = Math.abs(getBlockEntity().getBlockPos().offset(0, 1, 0).getY()+.5-entity.getY());
		double treshold = .9;
		boolean contact = distY < treshold;

		double vX = entity.getDeltaMovement().x;
		double vY = 0.1*vBase;
		double vZ = entity.getDeltaMovement().z;
		if(entity.getDeltaMovement().y < 0)
			vY += entity.getDeltaMovement().y*.9;

		if(!(entity instanceof Player))
		{
			vX = 0.05*getFacing().getStepX();
			vZ = 0.05*getFacing().getStepZ();
			if(getFacing()==Direction.WEST||getFacing()==Direction.EAST)
			{
				if(entity.getZ() > getBlockEntity().getBlockPos().getZ()+0.65D)
					vZ = -0.1D*vBase;
				else if(entity.getZ() < getBlockEntity().getBlockPos().getZ()+0.35D)
					vZ = 0.1D*vBase;
			}
			else if(getFacing()==Direction.NORTH||getFacing()==Direction.SOUTH)
			{
				if(entity.getX() > getBlockEntity().getBlockPos().getX()+0.65D)
					vX = -0.1D*vBase;
				else if(entity.getX() < getBlockEntity().getBlockPos().getX()+0.35D)
					vX = 0.1D*vBase;
			}
		}
		//Little boost at the top of a conveyor to help players and minecarts to get off
		BlockPos upForward = getBlockEntity().getBlockPos().offset(0, 1, 0);
		if(contact&&!(Utils.getExistingTileEntity(getBlockEntity().getLevel(), upForward) instanceof IConveyorBlockEntity))
			vY *= 2.25;
		return new Vec3(vX, vY, vZ);
	}

	private CapabilityReference<IItemHandler> inserter;

	@Override
	public void onEntityCollision(@Nonnull Entity entity)
	{
		collisionTracker.onEntityCollided(entity);
		if(!isActive()||!entity.isAlive())
			return;
		if(entity instanceof Player&&entity.isShiftKeyDown())
			return;

		BlockPos posWall = getBlockEntity().getBlockPos().relative(getFacing());
		double d = .625+entity.getBbWidth();
		double distToWall = Math.abs((getFacing().getAxis()==Axis.Z?posWall.getZ(): posWall.getX())+.5-(getFacing().getAxis()==Axis.Z?entity.getZ(): entity.getX()));
		if(distToWall > d)
		{
			super.onEntityCollision(entity);
			return;
		}

		boolean outputBlocked = isOutputBlocked();
		double distY = Math.abs(getBlockEntity().getBlockPos().offset(0, 1, 0).getY()+.5-entity.getY());
		double treshold = .9;
		boolean contact = distY < treshold;

		entity.setOnGround(false);
		if(entity.fallDistance < 3)
			entity.fallDistance = 0;
		else
			entity.fallDistance *= .9;
		Vec3 vec = getDirection(entity, outputBlocked);
		boolean hasBeenHandled = !ConveyorHandler.markEntityAsHandled(entity);
		if(outputBlocked&&entity.getY() >= getBlockEntity().getBlockPos().getY()+0.25)
		{
			double my;
			if(entity.getY() < entity.yOld)
				my = entity.yOld-entity.getY();
			else
				my = entity.getDeltaMovement().y;
			if(hasBeenHandled)
				vec = new Vec3(vec.x, my, vec.z);
			else
				vec = new Vec3(0, my, 0);
		}
		entity.setDeltaMovement(vec);

		if(!contact)
			ConveyorHandler.applyMagnetSuppression(entity, (IConveyorBlockEntity)getBlockEntity());
		else
		{
			BlockPos posTop = getBlockEntity().getBlockPos().offset(0, 1, 0);
			if(!((getBlockEntity().getLevel().getBlockEntity(posTop) instanceof IConveyorBlockEntity)||(getBlockEntity().getLevel().isEmptyBlock(posTop)&&(getBlockEntity().getLevel().getBlockEntity(posTop.relative(getFacing())) instanceof IConveyorBlockEntity))))
				ConveyorHandler.revertMagnetSuppression(entity, (IConveyorBlockEntity)getBlockEntity());
		}

		if(entity instanceof ItemEntity item)
		{
			if(!contact)
			{
				if(item.getAge() > item.lifespan-60*20)
					((ItemEntityAccess)item).setAge(item.lifespan-60*20);
			}
			else
			{
				BlockEntity inventoryTile;
				inventoryTile = getBlockEntity().getLevel().getBlockEntity(getBlockEntity().getBlockPos().offset(0, 1, 0));
				if(!getBlockEntity().getLevel().isClientSide)
				{
					if(inventoryTile!=null&&!(inventoryTile instanceof IConveyorBlockEntity))
					{
						ItemStack stack = item.getItem();
						if(!stack.isEmpty())
						{
							if(inserter==null)
								inserter = CapabilityReference.forNeighbor(getBlockEntity(), CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);
							ItemStack ret = Utils.insertStackIntoInventory(inserter, stack, false);
							if(ret.isEmpty())
								entity.discard();
							else if(ret.getCount() < stack.getCount())
								item.setItem(ret);
						}
					}
				}
			}
		}

		if(isCovered()&&entity instanceof ItemEntity)
			((ItemEntity)entity).setPickUpDelay(10);
	}

	@Override
	public BlockPos getOutputInventory()
	{
		return getBlockEntity().getBlockPos().above();
	}

	@Override
	public List<BlockPos> getNextConveyorCandidates()
	{
		BlockPos pos = getBlockEntity().getBlockPos();
		return ImmutableList.of(
				pos.above(),
				pos.above().relative(getFacing())
		);
	}

	private static final CachedShapesWithTransform<Boolean, Direction> SHAPES =
			CachedShapesWithTransform.createDirectional(VerticalConveyor::getShapes);

	@Override
	public VoxelShape getSelectionShape()
	{
		return getCollisionShape();
	}

	private static List<AABB> getShapes(Boolean bottomBelt)
	{
		List<AABB> list = Lists.newArrayList(new AABB(0, 0, 0, 1, 1, .125f));
		if(bottomBelt)
			list.add(conveyorBounds.bounds());
		return list;
	}

	@Override
	public VoxelShape getCollisionShape()
	{
		return SHAPES.get(Pair.of(renderBottomBelt(getBlockEntity(), getFacing()), getFacing()));
	}

	public static boolean isInwardConveyor(BlockEntity tile, Direction f)
	{
		BlockEntity te = tile.getLevel().getBlockEntity(tile.getBlockPos().relative(f));
		if(te instanceof IConveyorBlockEntity)
		{
			IConveyorBelt sub = ((IConveyorBlockEntity)te).getConveyorInstance();
			if(sub!=null)
				for(Direction f2 : sub.sigTransportDirections())
					if(f==f2.getOpposite())
						return true;
		}
		te = tile.getLevel().getBlockEntity(tile.getBlockPos().offset(0, -1, 0).relative(f));
		if(te instanceof IConveyorBlockEntity)
		{
			IConveyorBelt sub = ((IConveyorBlockEntity)te).getConveyorInstance();
			if(sub!=null)
			{
				int b = 0;
				for(Direction f2 : sub.sigTransportDirections())
				{
					if(f==f2.getOpposite())
						b++;
					else if(Direction.UP==f2)
						b++;
					if(b==2)
						return true;
				}
			}
		}
		return false;
	}

	public static boolean renderBottomBelt(BlockEntity tile, Direction facing)
	{
		BlockEntity te = tile.getLevel().getBlockEntity(tile.getBlockPos().offset(0, -1, 0));
		if(te instanceof IConveyorBlockEntity&&((IConveyorBlockEntity)te).getConveyorInstance()!=null)
			for(Direction f : ((IConveyorBlockEntity)te).getConveyorInstance().sigTransportDirections())
				if(f==Direction.UP)
					return false;
		for(Direction f : DirectionUtils.BY_HORIZONTAL_INDEX)
			if(f!=facing&&isInwardConveyor(tile, f))
				return true;
		return false;
	}
}
