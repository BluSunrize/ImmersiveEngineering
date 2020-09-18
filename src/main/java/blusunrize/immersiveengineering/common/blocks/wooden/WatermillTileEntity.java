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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public class WatermillTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IStateBasedDirectional, IHasDummyBlocks, IHasObjProperty
{
	public int[] offset = {0, 0};
	public float rotation = 0;
	private Vec3d rotationVec = null;
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
		if(offset[0]!=0||offset[1]!=0||world==null)
			return;
		if(isBlocked())
		{
			canTurn = false;
			return;
		}
		else
			canTurn = multiblock||getRotationVec().length()!=0;

		if(world.getGameTime()%64==((getPos().getX()^getPos().getZ())&63))
		{
			rotationVec = null;
		}
		prevRotation = rotation;

		TileEntity acc = Utils.getExistingTileEntity(world, getPos().offset(getFacing().getOpposite()));
		if(!multiblock&&acc instanceof IRotationAcceptor)
		{
			double power = getPower();
			int l = 1;
			TileEntity tileEntity = Utils.getExistingTileEntity(world, getPos().offset(getFacing(), l));
			while(l < 3
					&&canUse(tileEntity))
			{
				power += ((WatermillTileEntity)tileEntity).getPower();
				l++;
				tileEntity = Utils.getExistingTileEntity(world, getPos().offset(getFacing(), l));
			}

			perTick = 1f/1440*power/l;
			canTurn = perTick!=0;
			rotation += perTick;
			rotation %= 1;
			for(int l2 = 1; l2 < l; l2++)
			{
				tileEntity = world.getTileEntity(getPos().offset(getFacing(), l2));
				if(tileEntity instanceof WatermillTileEntity)
				{
					((WatermillTileEntity)tileEntity).rotation = rotation;
					((WatermillTileEntity)tileEntity).canTurn = canTurn;
					((WatermillTileEntity)tileEntity).perTick = perTick;
					((WatermillTileEntity)tileEntity).multiblock = true;
				}
			}

			if(!world.isRemote)
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

	private boolean canUse(@Nullable TileEntity tileEntity)
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
		if(world==null)
			return true;
		for(Direction fdY : new Direction[]{Direction.UP, Direction.DOWN})
			for(Direction fdW : getFacing().getAxis()==Axis.Z?new Direction[]{Direction.EAST, Direction.WEST}: new Direction[]{Direction.SOUTH, Direction.NORTH})
			{
				BlockPos pos = getPos().offset(fdW, 2).offset(fdY, 2);
				BlockState state = world.getBlockState(pos);
				if(Block.doesSideFillSquare(state.getShape(world, pos), fdW.getOpposite()))
					return true;
				if(Block.doesSideFillSquare(state.getShape(world, pos), fdY.getOpposite()))
					return true;
			}
		return false;
	}

	public double getPower()
	{
		return getFacing().getAxis()==Axis.Z?-getRotationVec().x: getRotationVec().z;
	}

	public Vec3d getRotationVec()
	{
		if(rotationVec==null)
		{
			rotationVec = new Vec3d(0, 0, 0);
			Vec3d dirHoz = getHorizontalVec();
			Vec3d dirVer = getVerticalVec();
			rotationVec = Utils.addVectors(rotationVec, dirHoz);
			rotationVec = Utils.addVectors(rotationVec, dirVer);
			//			world.addBlockEvent(xCoord, yCoord, zCoord, getBlockState(), (int)((float)rotationVec.xCoord*10000f), (int)((float)rotationVec.zCoord*10000f));
		}
		return rotationVec;
	}

	private Vec3d getHorizontalVec()
	{
		Vec3d dir = new Vec3d(0, 0, 0);
		boolean faceZ = getFacing().ordinal() <= 3;
		dir = Utils.addVectors(dir, Utils.getFlowVector(world, getPos().add(-(faceZ?1: 0), +3, -(faceZ?0: 1))));
		dir = Utils.addVectors(dir, Utils.getFlowVector(world, getPos().add(0, +3, 0)));
		dir = Utils.addVectors(dir, Utils.getFlowVector(world, getPos().add(+(faceZ?1: 0), +3, +(faceZ?0: 1))));

		dir = Utils.addVectors(dir, Utils.getFlowVector(world, getPos().add(-(faceZ?2: 0), +2, -(faceZ?0: 2))));
		dir = Utils.addVectors(dir, Utils.getFlowVector(world, getPos().add(+(faceZ?2: 0), +2, +(faceZ?0: 2))));

		dir = dir.subtract(Utils.getFlowVector(world, getPos().add(-(faceZ?2: 0), -2, -(faceZ?0: 2))));
		dir = dir.subtract(Utils.getFlowVector(world, getPos().add(+(faceZ?2: 0), -2, +(faceZ?0: 2))));
		dir = dir.subtract(Utils.getFlowVector(world, getPos().add(-(faceZ?1: 0), -3, -(faceZ?0: 1))));
		dir = dir.subtract(Utils.getFlowVector(world, getPos().add(0, -3, 0)));
		dir = dir.subtract(Utils.getFlowVector(world, getPos().add(+(faceZ?1: 0), -3, +(faceZ?0: 1))));

		return dir;
	}

	private Vec3d getVerticalVec()
	{
		Vec3d dir = new Vec3d(0, 0, 0);

		Vec3d dirNeg = new Vec3d(0, 0, 0);
		dirNeg = Utils.addVectors(dirNeg, Utils.getFlowVector(world, getPos().add(-(getFacing().getAxis()==Axis.Z?2: 0), 2, -(getFacing().getAxis()==Axis.Z?0: 2))));
		dirNeg = Utils.addVectors(dirNeg, Utils.getFlowVector(world, getPos().add(-(getFacing().getAxis()==Axis.Z?3: 0), 1, -(getFacing().getAxis()==Axis.Z?0: 3))));
		dirNeg = Utils.addVectors(dirNeg, Utils.getFlowVector(world, getPos().add(-(getFacing().getAxis()==Axis.Z?3: 0), 0, -(getFacing().getAxis()==Axis.Z?0: 3))));
		dirNeg = Utils.addVectors(dirNeg, Utils.getFlowVector(world, getPos().add(-(getFacing().getAxis()==Axis.Z?3: 0), -1, -(getFacing().getAxis()==Axis.Z?0: 3))));
		dirNeg = Utils.addVectors(dirNeg, Utils.getFlowVector(world, getPos().add(-(getFacing().getAxis()==Axis.Z?2: 0), -2, -(getFacing().getAxis()==Axis.Z?0: 2))));
		Vec3d dirPos = new Vec3d(0, 0, 0);
		dirPos = Utils.addVectors(dirPos, Utils.getFlowVector(world, getPos().add((getFacing().getAxis()==Axis.Z?2: 0), 2, (getFacing().getAxis()==Axis.Z?0: 2))));
		dirPos = Utils.addVectors(dirPos, Utils.getFlowVector(world, getPos().add((getFacing().getAxis()==Axis.Z?3: 0), 1, (getFacing().getAxis()==Axis.Z?0: 3))));
		dirPos = Utils.addVectors(dirPos, Utils.getFlowVector(world, getPos().add((getFacing().getAxis()==Axis.Z?3: 0), 0, (getFacing().getAxis()==Axis.Z?0: 3))));
		dirPos = Utils.addVectors(dirPos, Utils.getFlowVector(world, getPos().add((getFacing().getAxis()==Axis.Z?3: 0), -1, (getFacing().getAxis()==Axis.Z?0: 3))));
		dirPos = Utils.addVectors(dirPos, Utils.getFlowVector(world, getPos().add((getFacing().getAxis()==Axis.Z?2: 0), -2, (getFacing().getAxis()==Axis.Z?0: 2))));
		if(getFacing().getAxis()==Axis.Z)
			dir = dir.add(dirNeg.y-dirPos.y, 0, 0);
		else
			dir = dir.add(0, 0, dirNeg.y-dirPos.y);
		return dir;
	}

	public static boolean _Immovable()
	{
		return true;
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		rotationVec = new Vec3d(id/10000f, 0, arg/10000f);
		return true;
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		prevRotation = nbt.getFloat("prevRotation");
		offset = nbt.getIntArray("offset");
		rotation = nbt.getFloat("rotation");

		if(offset==null||offset.length < 2)
			offset = new int[]{0, 0};
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.putFloat("prevRotation", prevRotation);
		nbt.putIntArray("offset", offset);
		nbt.putFloat("rotation", rotation);
	}

	@OnlyIn(Dist.CLIENT)
	private AxisAlignedBB renderAABB;

	@OnlyIn(Dist.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(renderAABB==null)
			if(offset[0]==0&&offset[1]==0)
				renderAABB = new AxisAlignedBB(getPos().getX()-(getFacing().getAxis()==Axis.Z?2: 0), getPos().getY()-2, getPos().getZ()-(getFacing().getAxis()==Axis.Z?0: 2), getPos().getX()+(getFacing().getAxis()==Axis.Z?3: 0), getPos().getY()+3, getPos().getZ()+(getFacing().getAxis()==Axis.Z?0: 3));
			else
				renderAABB = new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX()+1, getPos().getY()+1, getPos().getZ()+1);
		return renderAABB;
	}

	@Override
	public EnumProperty<Direction> getFacingProperty()
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
	public boolean canHammerRotate(Direction side, Vec3d hit, LivingEntity entity)
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
		BlockPos masterPos = getPos().add(getFacing().getAxis()==Axis.Z?-offset[0]: 0, -offset[1], getFacing().getAxis()==Axis.Z?0: -offset[0]);
		TileEntity te = Utils.getExistingTileEntity(world, masterPos);
		return this.getClass().isInstance(te)?(IGeneralMultiblock)te: null;
	}

	@Override
	public void placeDummies(BlockItemUseContext ctx, BlockState state)
	{
		state = state.with(IEProperties.MULTIBLOCKSLAVE, true);
		for(int hh = -2; hh <= 2; hh++)
			for(int ww = -2; ww <= 2; ww++)
				if(((hh > -2&&hh < 2)||(ww > -2&&ww < 2))&&(hh!=0||ww!=0))
				{
					BlockPos pos2 = pos.add(getFacing().getAxis()==Axis.Z?ww: 0, hh, getFacing().getAxis()==Axis.Z?0: ww);
					world.setBlockState(pos2, state);
					WatermillTileEntity dummy = (WatermillTileEntity)world.getTileEntity(pos2);
					dummy.setFacing(getFacing());
					dummy.offset = new int[]{ww, hh};
				}
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		if(!formed)
			return;
		BlockPos initPos = pos.add(getFacing().getAxis()==Axis.Z?-offset[0]: 0, -offset[1], getFacing().getAxis()==Axis.X?-offset[0]: 0);
		for(int hh = -2; hh <= 2; hh++)
			for(int ww = -2; ww <= 2; ww++)
				if((hh > -2&&hh < 2)||(ww > -2&&ww < 2))
				{
					BlockPos pos2 = initPos.add(getFacing().getAxis()==Axis.Z?ww: 0, hh, getFacing().getAxis()==Axis.X?ww: 0);
					TileEntity te = world.getTileEntity(pos2);
					if(te instanceof WatermillTileEntity)
					{
						((WatermillTileEntity)te).formed = false;
						world.removeBlock(pos2, false);
					}
				}
	}

	@Override
	public VisibilityList compileDisplayList(BlockState state)
	{
		return VisibilityList.hideAll();
	}
}
