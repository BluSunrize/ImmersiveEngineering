/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.utils.shapes.CachedVoxelShapes;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.EnergyConnector;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ICollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.generic.ImmersiveConnectableTileEntity;
import blusunrize.immersiveengineering.common.register.IETileTypes;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RazorWireTileEntity extends ImmersiveConnectableTileEntity implements IStateBasedDirectional, ICollisionBounds,
		IOBJModelCallback<BlockState>, EnergyConnector, ISelectionBounds
{
	public RazorWireTileEntity(BlockPos pos, BlockState state)
	{
		super(IETileTypes.RAZOR_WIRE.get(), pos, state);
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return true;
	}

	@Override
	public void onEntityCollision(Level world, Entity entity)
	{
		if(entity instanceof LivingEntity)
		{
			Vec3 motion = entity.getDeltaMovement();
			entity.setDeltaMovement(motion.x()/5, motion.y(), motion.z()/5);
			applyDamage((LivingEntity)entity);
		}
	}

	public static void applyDamage(LivingEntity entity)
	{
		int protection = (!entity.getItemBySlot(EquipmentSlot.FEET).isEmpty()?1: 0)+(!entity.getItemBySlot(EquipmentSlot.LEGS).isEmpty()?1: 0);
		float dmg = protection==2?.5f: protection==1?1: 1.5f;
		entity.hurt(IEDamageSources.razorWire, dmg);
	}

	@Override
	public VoxelShape getSelectionShape(@Nullable CollisionContext ctx)
	{
		return Shapes.block();
	}

	private static final CachedVoxelShapes<BoundingBoxKey> SHAPES = new CachedVoxelShapes<>(RazorWireTileEntity::getShape);

	@Override
	public VoxelShape getCollisionShape(CollisionContext ctx)
	{
		return SHAPES.get(new BoundingBoxKey(this));
	}

	private static List<AABB> getShape(BoundingBoxKey key)
	{
		if((!key.onGround&&!key.stacked)||!(key.wallL||key.wallR))
			return ImmutableList.of();
		List<AABB> list = new ArrayList<>(key.wallL&&key.wallR?2: 1);
		if(key.wallL)
			list.add(new AABB(
					key.facing==Direction.SOUTH?.8125: 0, 0, key.facing==Direction.WEST?.8125: 0,
					key.facing==Direction.NORTH?.1875: 1, 1, key.facing==Direction.EAST?.1875: 1));
		if(key.wallR)
			list.add(new AABB(
					key.facing==Direction.NORTH?.8125: 0, 0, key.facing==Direction.EAST?.8125: 0,
					key.facing==Direction.SOUTH?.1875: 1, 1, key.facing==Direction.WEST?.1875: 1));
		return list;
	}

	private boolean renderWall(boolean left)
	{
		Direction dir = left?getFacing().getClockWise(): getFacing().getCounterClockWise();
		BlockPos neighbourPos = getBlockPos().relative(dir, -1);
		if(!level.hasChunkAt(neighbourPos))
			return true;
		if(level.getBlockEntity(neighbourPos) instanceof RazorWireTileEntity)
			return false;
		BlockState neighbour = level.getBlockState(neighbourPos);
		return !Block.isFaceFull(neighbour.getShape(level, neighbourPos), dir);
	}

	private static class BoundingBoxKey
	{
		public final boolean wallL;
		public final boolean wallR;
		public final boolean onGround;
		public final boolean stacked;
		public final Direction facing;

		public BoundingBoxKey(RazorWireTileEntity te)
		{
			this.facing = te.getFacing();
			this.wallL = te.renderWall(true);
			this.wallR = te.renderWall(false);
			this.onGround = te.isOnGround();
			this.stacked = te.isStacked();
		}

		@Override
		public boolean equals(Object o)
		{
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			BoundingBoxKey that = (BoundingBoxKey)o;
			return wallL==that.wallL&&
					wallR==that.wallR&&
					onGround==that.onGround&&
					stacked==that.stacked&&
					facing==that.facing;
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(wallL, wallR, onGround, stacked, facing);
		}
	}

	private boolean isOnGround()
	{
		BlockPos down = getBlockPos().below();
		return Block.isFaceFull(level.getBlockState(down).getShape(level, down), Direction.UP);
	}

	private boolean isStacked()
	{
		BlockPos down = getBlockPos().below();
		BlockEntity te = level.getBlockEntity(down);
		if(te instanceof RazorWireTileEntity)
			return ((RazorWireTileEntity)te).isOnGround();
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldRenderGroup(BlockState object, String group)
	{
		if(group==null)
			return false;
		boolean stack = isStacked();
		if(!stack&&!isOnGround())
			return !group.startsWith("wood");
		if(group.startsWith("wood")&&!(group.endsWith("inverted")==stack))
			return false;
		if(group.startsWith("wood_left"))
			return renderWall(true);
		else if("wire_left".equals(group)||"barbs_left".equals(group))
			return !renderWall(true);
		else if(group.startsWith("wood_right"))
			return renderWall(false);
		else if("wire_right".equals(group)||"barbs_right".equals(group))
			return !renderWall(false);
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public String getCacheKey(BlockState object)
	{
		boolean stack = isStacked();
		if(!stack&&!isOnGround())
			return "default";
		return (renderWall(true)?"L": " ")+(renderWall(false)?"R": " ")+(stack?"_stack": "");
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		return WireType.LV_CATEGORY.equals(cableType.getCategory());//TODO only allow one connection!
	}

	@Override
	public Vec3 getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		BlockPos other = con.getOtherEnd(here).getPosition();
		int xDif = other.getX()-worldPosition.getX();
		int yDif = other.getY()-worldPosition.getY();
		int zDif = other.getZ()-worldPosition.getZ();
		boolean wallL = renderWall(true);
		boolean wallR = renderWall(false);
		if(!isOnGround()||!(wallL||wallR))
		{
			if(yDif > 0)
				return new Vec3(getFacing().getStepX()!=0?.5: xDif < 0?.40625: .59375, .9375, getFacing().getStepZ()!=0?.5: zDif < 0?.40625: .59375);
			else
			{
				boolean right = getFacing().getClockWise().getAxisDirection().getStep()==Math.copySign(1, getFacing().getStepX()!=0?zDif: xDif);
				int faceX = getFacing().getStepX();
				int faceZ = getFacing().getStepZ();
				return new Vec3(faceX!=0?.5+(right?0: faceX*.1875): (xDif < 0?0: 1), .046875, faceZ!=0?.5+(right?0: faceZ*.1875): (zDif < 0?0: 1));
			}
		}
		else
		{
			boolean wallN = getFacing()==Direction.NORTH||getFacing()==Direction.EAST?wallL: wallR;
			return new Vec3(getFacing().getStepX()!=0?.5: xDif < 0&&wallN?.125: .875, .9375, getFacing().getStepZ()!=0?.5: zDif < 0&&wallN?.125: .875);
		}
	}

	@Override
	public boolean isSource(ConnectionPoint cp)
	{
		return false;
	}

	@Override
	public boolean isSink(ConnectionPoint cp)
	{
		return true;
	}

	@Override
	public int getRequestedEnergy()
	{
		return 64;
	}

	@Override
	public void insertEnergy(int amount)
	{
		int maxReach = amount/8;
		int widthP = 0;
		boolean connectP = true;
		int widthN = 0;
		boolean connectN = true;
		Direction dir = getFacing().getClockWise();
		if(dir.getAxisDirection()==AxisDirection.NEGATIVE)
			dir = dir.getOpposite();
		for(int i = 1; i <= maxReach; i++)
		{
			BlockPos posP = getBlockPos().relative(dir, i);
			if(connectP&&level.hasChunkAt(posP)&&level.getBlockEntity(posP) instanceof RazorWireTileEntity)
				widthP++;
			else
				connectP = false;
			BlockPos posN = getBlockPos().relative(dir, -i);
			if(connectN&&level.hasChunkAt(posN)&&level.getBlockEntity(posN) instanceof RazorWireTileEntity)
				widthN++;
			else
				connectN = false;
		}
		AABB aabb = new AABB(getBlockPos().offset(getFacing().getAxis()==Axis.Z?-widthN: 0, 0, getFacing().getAxis()==Axis.X?-widthN: 0), getBlockPos().offset(getFacing().getAxis()==Axis.Z?1+widthP: 1, 1, getFacing().getAxis()==Axis.X?1+widthP: 1));
		List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, aabb);
		for(LivingEntity ent : entities)
			ent.hurt(IEDamageSources.razorShock, 2);
	}
}