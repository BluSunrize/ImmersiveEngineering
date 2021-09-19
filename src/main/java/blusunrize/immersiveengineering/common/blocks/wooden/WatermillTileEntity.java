/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.energy.IRotationAcceptor;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasObjProperty;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public class WatermillTileEntity extends IEBaseTileEntity implements TickableBlockEntity, IStateBasedDirectional, IHasDummyBlocks, IHasObjProperty
{
	public int[] offset = {0, 0};
	public float rotation = 0;
	private Vec3 rotationVec = null;
	public boolean canTurn = false;
	public boolean multiblock = false;
	public float prevRotation = 0;
	private boolean formed = true;
	public double perTick;

	public WatermillTileEntity()
	{
		super(IETileTypes.WATERMILL.get());
	}

	@Override
	public void tick()
	{
		checkForNeedlessTicking();
		if(offset[0]!=0||offset[1]!=0||level==null)
			return;
		if(isBlocked())
		{
			canTurn = false;
			return;
		}
		else
			canTurn = multiblock||getRotationVec().length()!=0;

		if(level.getGameTime()%64==((getBlockPos().getX()^getBlockPos().getZ())&63))
		{
			rotationVec = null;
		}
		prevRotation = rotation;

		BlockEntity acc = Utils.getExistingTileEntity(level, getBlockPos().relative(getFacing().getOpposite()));
		if(!multiblock&&acc instanceof IRotationAcceptor)
		{
			double power = getPower();
			int l = 1;
			BlockEntity tileEntity = Utils.getExistingTileEntity(level, getBlockPos().relative(getFacing(), l));
			while(l < 3
					&&canUse(tileEntity))
			{
				power += ((WatermillTileEntity)tileEntity).getPower();
				l++;
				tileEntity = Utils.getExistingTileEntity(level, getBlockPos().relative(getFacing(), l));
			}

			perTick = 1f/1440*power/l;
			canTurn = perTick!=0;
			rotation += perTick;
			rotation %= 1;
			for(int l2 = 1; l2 < l; l2++)
			{
				tileEntity = level.getBlockEntity(getBlockPos().relative(getFacing(), l2));
				if(tileEntity instanceof WatermillTileEntity)
				{
					((WatermillTileEntity)tileEntity).rotation = rotation;
					((WatermillTileEntity)tileEntity).canTurn = canTurn;
					((WatermillTileEntity)tileEntity).perTick = perTick;
					((WatermillTileEntity)tileEntity).multiblock = true;
				}
			}

			if(!level.isClientSide)
			{
				IRotationAcceptor dynamo = (IRotationAcceptor)acc;
				//				if((facing.getAxis()==Axis.Z)&&dynamo.facing!=2&&dynamo.facing!=3)
				//					return;
				//				else if((facing.getAxis()==Axis.X)&&dynamo.facing!=4&&dynamo.facing!=5)
				//					return;
				dynamo.inputRotation(Math.abs(power*.75), getFacing().getOpposite());
			}
		}
		else if(!multiblock)
		{
			perTick = 1f/1440*getPower();
			canTurn = perTick!=0;
			rotation += perTick;
			rotation %= 1;
		}
		if(multiblock)
			multiblock = false;
	}

	private boolean canUse(@Nullable BlockEntity tileEntity)
	{
		return tileEntity instanceof WatermillTileEntity
				&&((WatermillTileEntity)tileEntity).offset[0]==0
				&&((WatermillTileEntity)tileEntity).offset[1]==0
				&&(((WatermillTileEntity)tileEntity).getFacing()==getFacing()||((WatermillTileEntity)tileEntity).getFacing()==getFacing().getOpposite())
				&&!((WatermillTileEntity)tileEntity).isBlocked()
				&&!((WatermillTileEntity)tileEntity).multiblock;
	}

	public boolean isBlocked()
	{
		if(level==null)
			return true;
		for(Direction fdY : new Direction[]{Direction.UP, Direction.DOWN})
			for(Direction fdW : getFacing().getAxis()==Axis.Z?new Direction[]{Direction.EAST, Direction.WEST}: new Direction[]{Direction.SOUTH, Direction.NORTH})
			{
				BlockPos pos = getBlockPos().relative(fdW, 2).relative(fdY, 2);
				BlockState state = level.getBlockState(pos);
				if(Block.isFaceFull(state.getShape(level, pos), fdW.getOpposite()))
					return true;
				if(Block.isFaceFull(state.getShape(level, pos), fdY.getOpposite()))
					return true;
			}
		return false;
	}

	public double getPower()
	{
		return getFacing().getAxis()==Axis.Z?-getRotationVec().x: getRotationVec().z;
	}

	public void resetRotationVec()
	{
		rotationVec = null;
	}

	public Vec3 getRotationVec()
	{
		if(rotationVec==null)
		{
			rotationVec = new Vec3(0, 0, 0);
			Vec3 dirHoz = getHorizontalVec();
			Vec3 dirVer = getVerticalVec();
			rotationVec = rotationVec.add(dirHoz);
			rotationVec = rotationVec.add(dirVer);
			//			world.addBlockEvent(xCoord, yCoord, zCoord, getBlockState(), (int)((float)rotationVec.xCoord*10000f), (int)((float)rotationVec.zCoord*10000f));
		}
		return rotationVec;
	}

	private Vec3 getHorizontalVec()
	{
		Vec3 dir = new Vec3(0, 0, 0);
		boolean faceZ = getFacing().ordinal() <= 3;
		dir = dir.add(Utils.getFlowVector(level, getBlockPos().offset(-(faceZ?1: 0), +3, -(faceZ?0: 1))));
		dir = dir.add(Utils.getFlowVector(level, getBlockPos().offset(0, +3, 0)));
		dir = dir.add(Utils.getFlowVector(level, getBlockPos().offset(+(faceZ?1: 0), +3, +(faceZ?0: 1))));

		dir = dir.add(Utils.getFlowVector(level, getBlockPos().offset(-(faceZ?2: 0), +2, -(faceZ?0: 2))));
		dir = dir.add(Utils.getFlowVector(level, getBlockPos().offset(+(faceZ?2: 0), +2, +(faceZ?0: 2))));

		dir = dir.subtract(Utils.getFlowVector(level, getBlockPos().offset(-(faceZ?2: 0), -2, -(faceZ?0: 2))));
		dir = dir.subtract(Utils.getFlowVector(level, getBlockPos().offset(+(faceZ?2: 0), -2, +(faceZ?0: 2))));
		dir = dir.subtract(Utils.getFlowVector(level, getBlockPos().offset(-(faceZ?1: 0), -3, -(faceZ?0: 1))));
		dir = dir.subtract(Utils.getFlowVector(level, getBlockPos().offset(0, -3, 0)));
		dir = dir.subtract(Utils.getFlowVector(level, getBlockPos().offset(+(faceZ?1: 0), -3, +(faceZ?0: 1))));

		return dir;
	}

	private Vec3 getVerticalVec()
	{
		Vec3 dir = new Vec3(0, 0, 0);

		Vec3 dirNeg = new Vec3(0, 0, 0);
		dirNeg = dirNeg.add(Utils.getFlowVector(level, getBlockPos().offset(-(getFacing().getAxis()==Axis.Z?2: 0), 2, -(getFacing().getAxis()==Axis.Z?0: 2))));
		dirNeg = dirNeg.add(Utils.getFlowVector(level, getBlockPos().offset(-(getFacing().getAxis()==Axis.Z?3: 0), 1, -(getFacing().getAxis()==Axis.Z?0: 3))));
		dirNeg = dirNeg.add(Utils.getFlowVector(level, getBlockPos().offset(-(getFacing().getAxis()==Axis.Z?3: 0), 0, -(getFacing().getAxis()==Axis.Z?0: 3))));
		dirNeg = dirNeg.add(Utils.getFlowVector(level, getBlockPos().offset(-(getFacing().getAxis()==Axis.Z?3: 0), -1, -(getFacing().getAxis()==Axis.Z?0: 3))));
		dirNeg = dirNeg.add(Utils.getFlowVector(level, getBlockPos().offset(-(getFacing().getAxis()==Axis.Z?2: 0), -2, -(getFacing().getAxis()==Axis.Z?0: 2))));
		Vec3 dirPos = new Vec3(0, 0, 0);
		dirPos = dirPos.add(Utils.getFlowVector(level, getBlockPos().offset((getFacing().getAxis()==Axis.Z?2: 0), 2, (getFacing().getAxis()==Axis.Z?0: 2))));
		dirPos = dirPos.add(Utils.getFlowVector(level, getBlockPos().offset((getFacing().getAxis()==Axis.Z?3: 0), 1, (getFacing().getAxis()==Axis.Z?0: 3))));
		dirPos = dirPos.add(Utils.getFlowVector(level, getBlockPos().offset((getFacing().getAxis()==Axis.Z?3: 0), 0, (getFacing().getAxis()==Axis.Z?0: 3))));
		dirPos = dirPos.add(Utils.getFlowVector(level, getBlockPos().offset((getFacing().getAxis()==Axis.Z?3: 0), -1, (getFacing().getAxis()==Axis.Z?0: 3))));
		dirPos = dirPos.add(Utils.getFlowVector(level, getBlockPos().offset((getFacing().getAxis()==Axis.Z?2: 0), -2, (getFacing().getAxis()==Axis.Z?0: 2))));
		if(getFacing().getAxis()==Axis.Z)
			dir = dir.add(dirNeg.y-dirPos.y, 0, 0);
		else
			dir = dir.add(0, 0, dirNeg.y-dirPos.y);
		return dir;
	}

	@Override
	public boolean triggerEvent(int id, int arg)
	{
		rotationVec = new Vec3(id/10000f, 0, arg/10000f);
		return true;
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		prevRotation = nbt.getFloat("prevRotation");
		offset = nbt.getIntArray("offset");
		rotation = nbt.getFloat("rotation");

		if(offset==null||offset.length < 2)
			offset = new int[]{0, 0};
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		nbt.putFloat("prevRotation", prevRotation);
		nbt.putIntArray("offset", offset);
		nbt.putFloat("rotation", rotation);
	}

	@OnlyIn(Dist.CLIENT)
	private AABB renderAABB;

	@OnlyIn(Dist.CLIENT)
	@Override
	public AABB getRenderBoundingBox()
	{
		if(renderAABB==null)
			if(offset[0]==0&&offset[1]==0)
				renderAABB = new AABB(getBlockPos().getX()-(getFacing().getAxis()==Axis.Z?2: 0), getBlockPos().getY()-2, getBlockPos().getZ()-(getFacing().getAxis()==Axis.Z?0: 2), getBlockPos().getX()+(getFacing().getAxis()==Axis.Z?3: 0), getBlockPos().getY()+3, getBlockPos().getZ()+(getFacing().getAxis()==Axis.Z?0: 3));
			else
				renderAABB = new AABB(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), getBlockPos().getX()+1, getBlockPos().getY()+1, getBlockPos().getZ()+1);
		return renderAABB;
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL_PREFER_SIDE;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}

	@Override
	public boolean isDummy()
	{
		return offset[0]!=0||offset[1]!=0;
	}

	@Nullable
	@Override
	public IGeneralMultiblock master()
	{
		if(!isDummy())
			return this;
		BlockPos masterPos = getBlockPos().offset(getFacing().getAxis()==Axis.Z?-offset[0]: 0, -offset[1], getFacing().getAxis()==Axis.Z?0: -offset[0]);
		BlockEntity te = Utils.getExistingTileEntity(level, masterPos);
		return this.getClass().isInstance(te)?(IGeneralMultiblock)te: null;
	}

	@Override
	public void placeDummies(BlockPlaceContext ctx, BlockState state)
	{
		state = state.setValue(IEProperties.MULTIBLOCKSLAVE, true);
		for(int hh = -2; hh <= 2; hh++)
			for(int ww = -2; ww <= 2; ww++)
				if(((hh > -2&&hh < 2)||(ww > -2&&ww < 2))&&(hh!=0||ww!=0))
				{
					BlockPos pos2 = worldPosition.offset(getFacing().getAxis()==Axis.Z?ww: 0, hh, getFacing().getAxis()==Axis.Z?0: ww);
					level.setBlockAndUpdate(pos2, state);
					WatermillTileEntity dummy = (WatermillTileEntity)level.getBlockEntity(pos2);
					dummy.setFacing(getFacing());
					dummy.offset = new int[]{ww, hh};
				}
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		if(!formed)
			return;
		BlockPos initPos = pos.offset(getFacing().getAxis()==Axis.Z?-offset[0]: 0, -offset[1], getFacing().getAxis()==Axis.X?-offset[0]: 0);
		for(int hh = -2; hh <= 2; hh++)
			for(int ww = -2; ww <= 2; ww++)
				if((hh > -2&&hh < 2)||(ww > -2&&ww < 2))
				{
					BlockPos pos2 = initPos.offset(getFacing().getAxis()==Axis.Z?ww: 0, hh, getFacing().getAxis()==Axis.X?ww: 0);
					BlockEntity te = level.getBlockEntity(pos2);
					if(te instanceof WatermillTileEntity)
					{
						((WatermillTileEntity)te).formed = false;
						level.removeBlock(pos2, false);
					}
				}
	}

	@Override
	public VisibilityList compileDisplayList(BlockState state)
	{
		return VisibilityList.hideAll();
	}
}
