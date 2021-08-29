/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.IConveyorBlockEntity;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorType;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.client.render.conveyor.SplitConveyorRender;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author BluSunrize - 20.08.2016
 */
public class SplitConveyor extends ConveyorBase<SplitConveyor>
{
	public static final ResourceLocation NAME = ImmersiveEngineering.rl("splitter");
	public static final ResourceLocation texture_on = ImmersiveEngineering.rl("block/conveyor/split");
	public static final ResourceLocation texture_off = ImmersiveEngineering.rl("block/conveyor/split_off");
	public static final IConveyorType<SplitConveyor> TYPE = new BasicConveyorType<>(
			NAME, false, true, SplitConveyor::new, () -> new SplitConveyorRender(texture_on, texture_off)
	);

	boolean nextOutputLeft = true;

	public SplitConveyor(BlockEntity tile)
	{
		super(tile);
	}

	@Override
	public IConveyorType<SplitConveyor> getType()
	{
		return TYPE;
	}

	@Override
	public ConveyorDirection getConveyorDirection()
	{
		return ConveyorDirection.HORIZONTAL;
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
	public void handleInsertion(ItemEntity entity, ConveyorDirection conDir, double distX, double distZ)
	{
		String nbtKey = "immersiveengineering:conveyorDir"+Integer.toHexString(getBlockEntity().getBlockPos().hashCode());
		if(entity.getPersistentData().contains(nbtKey, NBT.TAG_INT))
		{
			Direction redirect = Direction.values()[entity.getPersistentData().getInt(nbtKey)];
			BlockPos nextPos = getBlockEntity().getBlockPos().relative(redirect);
			double distNext = Math.abs((redirect.getAxis()==Axis.Z?nextPos.getZ(): nextPos.getX())+.5-(redirect.getAxis()==Axis.Z?entity.getZ(): entity.getX()));
			BlockEntity inventoryTile = getBlockEntity().getLevel().getBlockEntity(nextPos);
			if(distNext < .7&&inventoryTile!=null&&!(inventoryTile instanceof IConveyorBlockEntity))
			{
				ItemStack stack = entity.getItem();
				if(!stack.isEmpty())
				{
					CapabilityReference<IItemHandler> insert = CapabilityReference.forNeighbor(getBlockEntity(), CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, redirect);
					ItemStack ret = Utils.insertStackIntoInventory(insert, stack, false);
					if(ret.isEmpty())
						entity.discard();
					else if(ret.getCount() < stack.getCount())
						entity.setItem(ret);
				}
			}
		}
	}

	@Override
	public void onEntityCollision(@Nonnull Entity entity)
	{
		if(!isActive())
			return;
		Direction redirect = null;
		if(entity.isAlive())
		{
			String nbtKey = "immersiveengineering:conveyorDir"+Integer.toHexString(getBlockEntity().getBlockPos().hashCode());
			if(entity.getPersistentData().contains(nbtKey, NBT.TAG_INT))
				redirect = Direction.values()[entity.getPersistentData().getInt(nbtKey)];
			else
			{
				redirect = getOutputFace();
				entity.getPersistentData().putInt(nbtKey, redirect.ordinal());
				BlockPos nextPos = getBlockEntity().getBlockPos().relative(this.getOutputFace().getOpposite());
				if(getBlockEntity().getLevel().hasChunkAt(nextPos))
				{
					BlockEntity nextTile = getBlockEntity().getLevel().getBlockEntity(nextPos);
					if(!(nextTile instanceof IConveyorBlockEntity))
						nextOutputLeft = !nextOutputLeft;
					else if(((IConveyorBlockEntity)nextTile).getFacing()!=this.getOutputFace())
						nextOutputLeft = !nextOutputLeft;
				}
			}
		}
		super.onEntityCollision(entity);
		if(redirect!=null)
		{
			String nbtKey = "immersiveengineering:conveyorDir"+Integer.toHexString(getBlockEntity().getBlockPos().hashCode());
			BlockPos nextPos = getBlockEntity().getBlockPos().relative(redirect);
			double distNext = Math.abs((redirect.getAxis()==Axis.Z?nextPos.getZ(): nextPos.getX())+.5-(redirect.getAxis()==Axis.Z?entity.getZ(): entity.getX()));
			double treshold = .4;
			boolean contact = distNext < treshold;
			if(contact)
				entity.getPersistentData().remove(nbtKey);
		}
	}

	@Override
	public Direction[] sigTransportDirections()
	{
		return new Direction[]{getFacing().getClockWise(), getFacing().getCounterClockWise()};
	}

	@Override
	public Vec3 getDirection(Entity entity, boolean outputBlocked)
	{
		Vec3 vec = super.getDirection(entity, outputBlocked);
		String nbtKey = "immersiveengineering:conveyorDir"+Integer.toHexString(getBlockEntity().getBlockPos().hashCode());
		if(!entity.getPersistentData().contains(nbtKey, NBT.TAG_INT))
			return vec;
		Direction redirect = Direction.from3DDataValue(entity.getPersistentData().getInt(nbtKey));
		BlockPos wallPos = getBlockEntity().getBlockPos().relative(getFacing());
		double distNext = Math.abs((getFacing().getAxis()==Axis.Z?wallPos.getZ(): wallPos.getX())+.5-(getFacing().getAxis()==Axis.Z?entity.getZ(): entity.getX()));
		if(distNext < 1.33)
		{
			double sideMove = Math.pow(1+distNext, 0.1)*.2;
			if(distNext < .8)
				vec = new Vec3(getFacing().getAxis()==Axis.X?0: vec.x, vec.y, getFacing().getAxis()==Axis.Z?0: vec.z);
			vec = vec.add(redirect.getStepX()*sideMove, 0, redirect.getStepZ()*sideMove);
		}
		return vec;
	}

	@Override
	public CompoundTag writeConveyorNBT()
	{
		CompoundTag nbt = super.writeConveyorNBT();
		nbt.putBoolean("nextLeft", nextOutputLeft);
		return nbt;
	}

	@Override
	public void readConveyorNBT(CompoundTag nbt)
	{
		super.readConveyorNBT(nbt);
		nextOutputLeft = nbt.getBoolean("nextLeft");
	}

	@Override
	public List<BlockPos> getNextConveyorCandidates()
	{
		BlockPos baseOutput = getBlockEntity().getBlockPos().relative(getOutputFace());
		return ImmutableList.of(
				baseOutput,
				baseOutput.below()
		);
	}

	@Override
	public boolean isOutputBlocked()
	{
		// Consider the belt blocked if at least one of the possible outputs is blocked
		Direction outputFace = getOutputFace();
		BlockPos here = getBlockEntity().getBlockPos();
		for(BlockPos outputPos : new BlockPos[]{
				here.relative(outputFace, 1),
				here.relative(outputFace, -1),
		})
		{
			BlockEntity tile = SafeChunkUtils.getSafeBE(getBlockEntity().getLevel(), outputPos);
			if(tile instanceof IConveyorBlockEntity&&((IConveyorBlockEntity)tile).getConveyorInstance().isBlocked())
				return true;
		}
		return false;
	}

	private Direction getOutputFace()
	{
		if(nextOutputLeft)
			return getFacing().getCounterClockWise();
		else
			return getFacing().getClockWise();
	}
}
