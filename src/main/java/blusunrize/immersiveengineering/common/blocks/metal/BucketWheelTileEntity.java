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
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;

public class BucketWheelTileEntity extends MultiblockPartTileEntity<BucketWheelTileEntity> implements IBlockBounds
{
	public float rotation = 0;
	public final NonNullList<ItemStack> digStacks = NonNullList.withSize(8, ItemStack.EMPTY);
	public boolean active = false;
	public ItemStack particleStack = ItemStack.EMPTY;

	public BucketWheelTileEntity()
	{
		super(IEMultiblocks.BUCKET_WHEEL, IETileTypes.BUCKET_WHEEL.get(), false);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		float nbtRot = nbt.getFloat("rotation");
		rotation = (Math.abs(nbtRot-rotation) > 5*IEServerConfig.MACHINES.excavator_speed.get())?nbtRot: rotation; // avoid stuttering due to packet delays
		ContainerHelper.loadAllItems(nbt, digStacks);
		active = nbt.getBoolean("active");
		particleStack = nbt.contains("particleStack", NBT.TAG_COMPOUND)?ItemStack.of(nbt.getCompound("particleStack")): ItemStack.EMPTY;
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putFloat("rotation", rotation);
		ContainerHelper.saveAllItems(nbt, digStacks);
		nbt.putBoolean("active", active);
		if(!particleStack.isEmpty())
			nbt.put("particleStack", particleStack.save(new CompoundTag()));
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
	public void tick()
	{
		checkForNeedlessTicking();
		if(!formed||!new BlockPos(3, 3, 0).equals(posInMultiblock))
			return;

		if(active)
		{
			rotation += IEServerConfig.MACHINES.excavator_speed.get();
			rotation %= 360;
		}

		if(level.isClientSide)
		{
			if(!particleStack.isEmpty())
			{
				//TODO this can be done from the server now
				ImmersiveEngineering.proxy.spawnBucketWheelFX(this, particleStack);
				particleStack = ItemStack.EMPTY;
			}
		}
		else if(active&&level.getGameTime()%20==0)
		{
			CompoundTag nbt = new CompoundTag();
			nbt.putFloat("rotation", rotation);
			MessageTileSync sync = new MessageTileSync(this, nbt);
			ImmersiveEngineering.packetHandler.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(worldPosition)), sync);
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
				particleStack = digStacks.get(toRemove);
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

	@OnlyIn(Dist.CLIENT)
	private AABB renderAABB;

	@Override
	@OnlyIn(Dist.CLIENT)
	public AABB getRenderBoundingBox()
	{
		if(renderAABB==null)
//			if(pos==24)
			renderAABB = new AABB(getBlockPos().offset(-(getFacing().getAxis()==Axis.Z?3: 0), -3, -(getFacing().getAxis()==Axis.X?3: 0)), getBlockPos().offset((getFacing().getAxis()==Axis.Z?4: 1), 4, (getFacing().getAxis()==Axis.X?4: 1)));
//			else
//				renderAABB = new AxisAlignedBB(getPos(), getPos());
		return renderAABB;
	}

	private static CachedShapesWithTransform<BlockPos, Direction> SHAPES = CachedShapesWithTransform.createDirectional(BucketWheelTileEntity::getBoxes);

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
					if(te instanceof BucketWheelTileEntity)
					{
						BucketWheelTileEntity bucketTE = (BucketWheelTileEntity)te;
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