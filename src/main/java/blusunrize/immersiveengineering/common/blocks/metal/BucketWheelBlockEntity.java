/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.ticking.IEClientTickableBE;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.network.MessageBlockEntitySync;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;

public class BucketWheelBlockEntity extends MultiblockPartBlockEntity<BucketWheelBlockEntity> implements IBlockBounds, IEClientTickableBE
{
	public float rotation = 0;
	public final NonNullList<ItemStack> digStacks = NonNullList.withSize(8, ItemStack.EMPTY);
	public boolean active = false;

	public BucketWheelBlockEntity(BlockEntityType<BucketWheelBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(IEMultiblocks.BUCKET_WHEEL, type, false, pos, state);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		float nbtRot = nbt.getFloat("rotation");
		rotation = (Math.abs(nbtRot-rotation) > 5*IEServerConfig.MACHINES.excavator_speed.get())?nbtRot: rotation; // avoid stuttering due to packet delays
		ContainerHelper.loadAllItems(nbt, digStacks);
		active = nbt.getBoolean("active");
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putFloat("rotation", rotation);
		ContainerHelper.saveAllItems(nbt, digStacks);
		nbt.putBoolean("active", active);
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side)
	{
		return new IFluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resources)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, Direction side)
	{
		return false;
	}

	@Override
	public void tickClient()
	{
		if(active)
		{
			rotation += IEServerConfig.MACHINES.excavator_speed.get();
			rotation %= 360;
		}
	}

	@Override
	public void tickServer()
	{
		tickClient();
		if(active&&level.getGameTime()%20==0)
		{
			CompoundTag nbt = new CompoundTag();
			nbt.putFloat("rotation", rotation);
			MessageBlockEntitySync sync = new MessageBlockEntitySync(this, nbt);
			ImmersiveEngineering.packetHandler.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(worldPosition)), sync);
		}
	}

	public void spawnParticles(ItemStack stack)
	{
		Level w = getLevelNonnull();
		if(w instanceof ServerLevel&&IEServerConfig.MACHINES.excavator_particles.get())
		{
			Direction facing = getFacing();
			Axis axis = facing.getAxis();
			int sign = (getIsMirrored()^facing.getAxisDirection()==AxisDirection.NEGATIVE)?1: -1;
			final double x = getBlockPos().getX()+.5;
			final double y = getBlockPos().getY()+2.5;
			final double z = getBlockPos().getZ()+.5;
			double fixPosOffset = .5*sign;
			double fixVelOffset = .075*sign;
			for(int i = 0; i < 16; i++)
			{
				double mX = (getLevelNonnull().random.nextDouble()-.5)*.01;
				double mY = getLevelNonnull().random.nextDouble()*-0.05D;
				double mZ = (getLevelNonnull().random.nextDouble()-.5)*.01;
				double rndPosOffset = .2*(getLevelNonnull().random.nextDouble()-.5);

				if(facing.getAxis()==Axis.X)
					mX += fixVelOffset;
				else
					mZ += fixVelOffset;

				((ServerLevel)w).sendParticles(
						new ItemParticleOption(ParticleTypes.ITEM, stack),
						x+axis.choose(fixPosOffset, 0, rndPosOffset), y, z+axis.choose(rndPosOffset, 0, fixPosOffset),
						0,
						mX, mY, mZ, 1
				);
			}
		}
	}

	@Override
	public void receiveMessageFromServer(CompoundTag message)
	{
		synchronized(digStacks)
		{
			if(message.contains("fill", NBT.TAG_INT))
				this.digStacks.set(message.getInt("fill"), ItemStack.of(message.getCompound("fillStack")));
			if(message.contains("empty", NBT.TAG_INT))
			{
				int toRemove = message.getInt("empty");
				this.digStacks.set(toRemove, ItemStack.EMPTY);
			}
			if(message.contains("rotation", NBT.TAG_INT))
			{
				int packetRotation = message.getInt("rotation");
				if(Math.abs(packetRotation-rotation) > 5*IEServerConfig.MACHINES.excavator_speed.get())
					rotation = packetRotation;
			}
		}
	}

	@Override
	public boolean triggerEvent(int id, int arg)
	{
		if(id==0)
			this.active = (arg==1);
		return true;
	}

	private AABB renderAABB;

	@Override
	public AABB getRenderBoundingBox()
	{
		if(renderAABB==null)
			renderAABB = new AABB(getBlockPos().offset(-(getFacing().getAxis()==Axis.Z?3: 0), -3, -(getFacing().getAxis()==Axis.X?3: 0)), getBlockPos().offset((getFacing().getAxis()==Axis.Z?4: 1), 4, (getFacing().getAxis()==Axis.X?4: 1)));
		return renderAABB;
	}

	private static final CachedShapesWithTransform<BlockPos, Direction> SHAPES = CachedShapesWithTransform.createDirectional(BucketWheelBlockEntity::getBoxes);

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return SHAPES.get(posInMultiblock, getFacing());
	}

	private static List<AABB> getBoxes(BlockPos posInMultiblock)
	{
		final AABB ret;
		if(ImmutableSet.of(
				new BlockPos(3, 0, 0),
				new BlockPos(2, 1, 0),
				new BlockPos(4, 1, 0)
		).contains(posInMultiblock))
			ret = new AABB(0, .25f, 0, 1, 1, 1);
		else if(ImmutableSet.of(
				new BlockPos(3, 6, 0),
				new BlockPos(2, 5, 0),
				new BlockPos(4, 5, 0)
		).contains(posInMultiblock))
			ret = new AABB(0, 0, 0, 1, .75f, 1);
		else if(new BlockPos(0, 3, 0).equals(posInMultiblock))
			ret = new AABB(.25f, 0, 0, 1, 1, 1);
		else if(new BlockPos(6, 3, 0).equals(posInMultiblock))
			ret = new AABB(0, 0, 0, .75f, 1, 1);
		else if(ImmutableSet.of(
				new BlockPos(1, 2, 0),
				new BlockPos(1, 4, 0)
		).contains(posInMultiblock))
			ret = new AABB(.25f, 0, 0, 1, 1, 1);
		else if(ImmutableSet.of(
				new BlockPos(5, 2, 0),
				new BlockPos(5, 4, 0)
		).contains(posInMultiblock))
			ret = new AABB(0, 0, 0, .75f, 1, 1);
		else
			ret = new AABB(0, 0, 0, 1, 1, 1);
		return ImmutableList.of(ret);
	}

	public void adjustStructureFacingAndMirrored(Direction targetFacing, boolean targetMirrored)
	{
		if(this==master()&&targetFacing.getAxis()!=Direction.Axis.Y&&(getFacing()!=targetFacing||getIsMirrored()!=targetMirrored))
		{
			boolean changePos = (getFacing()!=targetFacing)^(getIsMirrored()^targetMirrored);
			for(int h = -3; h <= 3; h++)
				for(int w = -3; w <= 3; w++)
				{
					if((Math.abs(h)==3&&w!=0)||(Math.abs(w)==3&&h!=0))
						continue;
					BlockEntity te = level.getBlockEntity(getBlockPos().offset(0, h, 0).relative(getFacing(), w));
					if(te instanceof BucketWheelBlockEntity)
					{
						BucketWheelBlockEntity bucketTE = (BucketWheelBlockEntity)te;
						bucketTE.setFacing(targetFacing);
						bucketTE.setMirrored(targetMirrored);
						if(changePos)
							bucketTE.posInMultiblock = new BlockPos(6-bucketTE.posInMultiblock.getX(), bucketTE.posInMultiblock.getY(), bucketTE.posInMultiblock.getZ());
						te.setChanged();
						bucketTE.markContainingBlockForUpdate(null);
						level.blockEvent(te.getBlockPos(), te.getBlockState().getBlock(), 255, 0);
					}
				}
		}
	}
}