/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.excavator.MineralWorldInfo;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.items.CoresampleItem;
import blusunrize.immersiveengineering.common.items.CoresampleItem.VeinSampleData;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.register.IETileTypes;
import blusunrize.immersiveengineering.common.temp.IETickableBlockEntity;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class SampleDrillTileEntity extends IEBaseTileEntity implements IETickableBlockEntity, IIEInternalFluxHandler, IHasDummyBlocks,
		IPlayerInteraction, IModelOffsetProvider
{
	public FluxStorage energyStorage = new FluxStorage(8000);
	public int dummy = 0;
	public int process = 0;
	public boolean isRunning = false;
	@Nonnull
	public ItemStack sample = ItemStack.EMPTY;

	public SampleDrillTileEntity(BlockPos pos, BlockState state)
	{
		super(IETileTypes.SAMPLE_DRILL.get(), pos, state);
	}

	@Override
	public boolean canTickAny()
	{
		return dummy==0&&sample.isEmpty();
	}

	@Override
	public void tick()
	{
		checkForNeedlessTicking();
		IETickableBlockEntity.super.tick();
	}

	@Override
	public void tickClient()
	{
		if(isRunning)
			process++;
	}

	@Override
	public void tickServer()
	{
		final int consumption = IEServerConfig.MACHINES.coredrill_consumption.get();
		final int totalTime = IEServerConfig.MACHINES.coredrill_time.get();
		boolean canRun = process > 0
				&&process < totalTime
				&&energyStorage.getEnergyStored() >= consumption
				&&!isRSPowered()
				&&!level.isEmptyBlock(getBlockPos().offset(0, -1, 0));

		if(canRun&&energyStorage.extractEnergy(consumption, false)==consumption)
		{
			process++;
			if(process >= totalTime)
			{
				MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(level, getBlockPos());
				this.sample = createCoreSample(info);
				this.process = 0;
				canRun = false;
				this.markContainingBlockForUpdate(null);
			}
			this.markChunkDirty();
		}
		if(canRun!=isRunning)
		{
			isRunning = canRun;
			this.markChunkDirty();
			this.markContainingBlockForUpdate(null);
		}
	}

	public float getSampleProgress()
	{
		return process/(float)IEServerConfig.MACHINES.coredrill_time.get();
	}

	public boolean isSamplingFinished()
	{
		return process >= IEServerConfig.MACHINES.coredrill_time.get();
	}

	@Nullable
	public List<VeinSampleData> getVein()
	{
		if(sample.isEmpty())
			return null;
		else
			return CoresampleItem.getVeins(sample);
	}

	@Nonnull
	public ItemStack createCoreSample(@Nullable MineralWorldInfo info)
	{
		ItemStack stack = new ItemStack(Misc.coresample);
		ItemNBTHelper.putLong(stack, "timestamp", level.getGameTime());
		CoresampleItem.setDimension(stack, level.dimension());
		CoresampleItem.setCoords(stack, getBlockPos());
		CoresampleItem.setMineralInfo(stack, info, getBlockPos());
		return stack;
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		energyStorage.writeToNBT(nbt);
		nbt.putInt("dummy", dummy);
		nbt.putInt("process", process);
		nbt.putBoolean("isRunning", isRunning);
		if(!sample.isEmpty())
			nbt.put("sample", sample.save(new CompoundTag()));
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		energyStorage.readFromNBT(nbt);
		dummy = nbt.getInt("dummy");
		process = nbt.getInt("process");
		isRunning = nbt.getBoolean("isRunning");
		if(nbt.contains("sample", NBT.TAG_COMPOUND))
			sample = ItemStack.of(nbt.getCompound("sample"));
		else
			sample = ItemStack.EMPTY;
	}

	@OnlyIn(Dist.CLIENT)
	private AABB renderAABB;

	@OnlyIn(Dist.CLIENT)
	@Override
	public AABB getRenderBoundingBox()
	{
		if(renderAABB==null)
			if(dummy==0)
				renderAABB = new AABB(getBlockPos(), getBlockPos().offset(1, 3, 1));
			else
				renderAABB = new AABB(getBlockPos(), getBlockPos());
		return renderAABB;
	}


	@Nonnull
	@Override
	public FluxStorage getFluxStorage()
	{
		if(dummy > 0)
		{
			BlockEntity te = level.getBlockEntity(getBlockPos().offset(0, -dummy, 0));
			if(te instanceof SampleDrillTileEntity)
				return ((SampleDrillTileEntity)te).getFluxStorage();
		}
		return energyStorage;
	}

	@Nonnull
	@Override
	public IOSideConfig getEnergySideConfig(Direction facing)
	{
		return dummy==0&&facing!=null&&facing.getAxis()!=Axis.Y?IOSideConfig.INPUT: IOSideConfig.NONE;
	}

	IEForgeEnergyWrapper[] wrappers = {
			new IEForgeEnergyWrapper(this, Direction.NORTH),
			new IEForgeEnergyWrapper(this, Direction.SOUTH),
			new IEForgeEnergyWrapper(this, Direction.WEST),
			new IEForgeEnergyWrapper(this, Direction.EAST)
	};

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing)
	{
		if(dummy==0&&facing!=null&&facing.getAxis()!=Axis.Y)
			return wrappers[facing.ordinal()-2];
		return null;
	}

	@Override
	public boolean isDummy()
	{
		return dummy > 0;
	}

	@Nullable
	@Override
	public IGeneralMultiblock master()
	{
		if(!isDummy())
			return this;
		BlockPos masterPos = getBlockPos().below(dummy);
		BlockEntity te = Utils.getExistingTileEntity(level, masterPos);
		return this.getClass().isInstance(te)?(IGeneralMultiblock)te: null;
	}

	@Override
	public void placeDummies(BlockPlaceContext ctx, BlockState state)
	{
		state = state.setValue(IEProperties.MULTIBLOCKSLAVE, true);
		for(int i = 1; i <= 2; i++)
		{
			level.setBlockAndUpdate(worldPosition.offset(0, i, 0), state);
			((SampleDrillTileEntity)level.getBlockEntity(worldPosition.offset(0, i, 0))).dummy = i;
		}
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		for(int i = 0; i <= 2; i++)
			if(level.getBlockEntity(getBlockPos().offset(0, -dummy, 0).offset(0, i, 0)) instanceof SampleDrillTileEntity)
				level.removeBlock(getBlockPos().offset(0, -dummy, 0).offset(0, i, 0), false);
	}

	@Override
	public boolean interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(dummy!=0)
		{
			BlockEntity te = level.getBlockEntity(getBlockPos().offset(0, -dummy, 0));
			if(te instanceof SampleDrillTileEntity)
				return ((SampleDrillTileEntity)te).interact(side, player, hand, heldItem, hitX, hitY, hitZ);
		}

		if(!this.sample.isEmpty())
		{
			if(!level.isClientSide)
			{
				player.spawnAtLocation(this.sample.copy(), .5f);
				this.sample = ItemStack.EMPTY;
				setChanged();
				this.markContainingBlockForUpdate(null);
			}
			return true;
		}
		else if(this.process <= 0)
		{
			if(!level.isClientSide&&energyStorage.getEnergyStored() >= IEServerConfig.MACHINES.coredrill_consumption.get())
			{
				this.process = 1;
				setChanged();
				this.markContainingBlockForUpdate(null);
			}
			return true;
		}
		return false;
	}

	@Override
	public BlockPos getModelOffset(BlockState state, @Nullable Vec3i size)
	{
		return new BlockPos(0, dummy, 0);
	}
}