/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.ImmersiveConnectableTileEntity;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.EnergyConnector;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RazorWireTileEntity extends ImmersiveConnectableTileEntity implements IStateBasedDirectional, IAdvancedCollisionBounds,
		IOBJModelCallback<BlockState>, EnergyConnector
{
	public static TileEntityType<RazorWireTileEntity> TYPE;

	public RazorWireTileEntity()
	{
		super(TYPE);
	}

	@Override
	public EnumProperty<Direction> getFacingProperty()
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
	public boolean canHammerRotate(Direction side, float hitX, float hitY, float hitZ, LivingEntity entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return true;
	}

	@Override
	public void onEntityCollision(World world, Entity entity)
	{
		if(entity instanceof LivingEntity)
		{
			Vec3d motion = entity.getMotion();
			entity.setMotion(motion.getX()/5, motion.getY(), motion.getZ()/5);
			applyDamage((LivingEntity)entity);
		}
	}

	public static void applyDamage(LivingEntity entity)
	{
		int protection = (!entity.getItemStackFromSlot(EquipmentSlotType.FEET).isEmpty()?1: 0)+(!entity.getItemStackFromSlot(EquipmentSlotType.LEGS).isEmpty()?1: 0);
		float dmg = protection==2?.5f: protection==1?1: 1.5f;
		entity.attackEntityFrom(IEDamageSources.razorWire, dmg);
	}

	@Override
	public float[] getBlockBounds()
	{
		return null;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		boolean wallL = renderWall(true);
		boolean wallR = renderWall(false);
		if((!isOnGround()&&!isStacked())||!(wallL||wallR))
			return Collections.singletonList(null);
		List<AxisAlignedBB> list = new ArrayList<>(wallL&&wallR?2: 1);
		if(wallL)
			list.add(new AxisAlignedBB(getFacing()==Direction.SOUTH?.8125: 0, 0, getFacing()==Direction.WEST?.8125: 0, getFacing()==Direction.NORTH?.1875: 1, 1, getFacing()==Direction.EAST?.1875: 1).offset(getPos()));
		if(wallR)
			list.add(new AxisAlignedBB(getFacing()==Direction.NORTH?.8125: 0, 0, getFacing()==Direction.EAST?.8125: 0, getFacing()==Direction.SOUTH?.1875: 1, 1, getFacing()==Direction.WEST?.1875: 1).offset(getPos()));
		return list;
	}

	private boolean renderWall(boolean left)
	{
		Direction dir = left?getFacing().rotateY(): getFacing().rotateYCCW();
		BlockPos neighbourPos = getPos().offset(dir, -1);
		if(!world.isBlockLoaded(neighbourPos))
			return true;
		if(world.getTileEntity(neighbourPos) instanceof RazorWireTileEntity)
			return false;
		BlockState neighbour = world.getBlockState(neighbourPos);
		return !Block.doesSideFillSquare(neighbour.getShape(world, neighbourPos), dir);
	}

	private boolean isOnGround()
	{
		BlockPos down = getPos().down();
		return Block.doesSideFillSquare(world.getBlockState(down).getShape(world, down), Direction.UP);
	}

	private boolean isStacked()
	{
		BlockPos down = getPos().down();
		TileEntity te = world.getTileEntity(down);
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
	public Vec3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		BlockPos other = con.getOtherEnd(here).getPosition();
		int xDif = other.getX()-pos.getX();
		int yDif = other.getY()-pos.getY();
		int zDif = other.getZ()-pos.getZ();
		boolean wallL = renderWall(true);
		boolean wallR = renderWall(false);
		if(!isOnGround()||!(wallL||wallR))
		{
			if(yDif > 0)
				return new Vec3d(getFacing().getXOffset()!=0?.5: xDif < 0?.40625: .59375, .9375, getFacing().getZOffset()!=0?.5: zDif < 0?.40625: .59375);
			else
			{
				boolean right = getFacing().rotateY().getAxisDirection().getOffset()==Math.copySign(1, getFacing().getXOffset()!=0?zDif: xDif);
				int faceX = getFacing().getXOffset();
				int faceZ = getFacing().getZOffset();
				return new Vec3d(faceX!=0?.5+(right?0: faceX*.1875): (xDif < 0?0: 1), .046875, faceZ!=0?.5+(right?0: faceZ*.1875): (zDif < 0?0: 1));
			}
		}
		else
		{
			boolean wallN = getFacing()==Direction.NORTH||getFacing()==Direction.EAST?wallL: wallR;
			return new Vec3d(getFacing().getXOffset()!=0?.5: xDif < 0&&wallN?.125: .875, .9375, getFacing().getZOffset()!=0?.5: zDif < 0&&wallN?.125: .875);
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
		Direction dir = getFacing().rotateY();
		if(dir.getAxisDirection()==AxisDirection.NEGATIVE)
			dir = dir.getOpposite();
		for(int i = 1; i <= maxReach; i++)
		{
			BlockPos posP = getPos().offset(dir, i);
			if(connectP&&world.isBlockLoaded(posP)&&world.getTileEntity(posP) instanceof RazorWireTileEntity)
				widthP++;
			else
				connectP = false;
			BlockPos posN = getPos().offset(dir, -i);
			if(connectN&&world.isBlockLoaded(posN)&&world.getTileEntity(posN) instanceof RazorWireTileEntity)
				widthN++;
			else
				connectN = false;
		}
		AxisAlignedBB aabb = new AxisAlignedBB(getPos().add(getFacing().getAxis()==Axis.Z?-widthN: 0, 0, getFacing().getAxis()==Axis.X?-widthN: 0), getPos().add(getFacing().getAxis()==Axis.Z?1+widthP: 1, 1, getFacing().getAxis()==Axis.X?1+widthP: 1));
		List<LivingEntity> entities = world.getEntitiesWithinAABB(LivingEntity.class, aabb);
		for(LivingEntity ent : entities)
			ent.attackEntityFrom(IEDamageSources.razorShock, 2);
	}
}