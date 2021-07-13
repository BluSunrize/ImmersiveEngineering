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
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.items.CoresampleItem;
import blusunrize.immersiveengineering.common.items.CoresampleItem.VeinSampleData;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.temp.IETickableBlockEntity;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
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

	public SampleDrillTileEntity()
	{
		super(IETileTypes.SAMPLE_DRILL.get());
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
				&&!world.isAirBlock(getPos().add(0, -1, 0));

		if(canRun&&energyStorage.extractEnergy(consumption, false)==consumption)
		{
			process++;
			if(process >= totalTime)
			{
				MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(world, getPos());
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
		ItemNBTHelper.putLong(stack, "timestamp", world.getGameTime());
		CoresampleItem.setDimension(stack, world.getDimensionKey());
		CoresampleItem.setCoords(stack, getPos());
		CoresampleItem.setMineralInfo(stack, info, getPos());
		return stack;
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		energyStorage.writeToNBT(nbt);
		nbt.putInt("dummy", dummy);
		nbt.putInt("process", process);
		nbt.putBoolean("isRunning", isRunning);
		if(!sample.isEmpty())
			nbt.put("sample", sample.write(new CompoundNBT()));
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		energyStorage.readFromNBT(nbt);
		dummy = nbt.getInt("dummy");
		process = nbt.getInt("process");
		isRunning = nbt.getBoolean("isRunning");
		if(nbt.contains("sample", NBT.TAG_COMPOUND))
			sample = ItemStack.read(nbt.getCompound("sample"));
		else
			sample = ItemStack.EMPTY;
	}

	@OnlyIn(Dist.CLIENT)
	private AxisAlignedBB renderAABB;

	@OnlyIn(Dist.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(renderAABB==null)
			if(dummy==0)
				renderAABB = new AxisAlignedBB(getPos(), getPos().add(1, 3, 1));
			else
				renderAABB = new AxisAlignedBB(getPos(), getPos());
		return renderAABB;
	}


	@Nonnull
	@Override
	public FluxStorage getFluxStorage()
	{
		if(dummy > 0)
		{
			TileEntity te = world.getTileEntity(getPos().add(0, -dummy, 0));
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
		BlockPos masterPos = getPos().down(dummy);
		TileEntity te = Utils.getExistingTileEntity(world, masterPos);
		return this.getClass().isInstance(te)?(IGeneralMultiblock)te: null;
	}

	@Override
	public void placeDummies(BlockItemUseContext ctx, BlockState state)
	{
		state = state.with(IEProperties.MULTIBLOCKSLAVE, true);
		for(int i = 1; i <= 2; i++)
		{
			world.setBlockState(pos.add(0, i, 0), state);
			((SampleDrillTileEntity)world.getTileEntity(pos.add(0, i, 0))).dummy = i;
		}
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		for(int i = 0; i <= 2; i++)
			if(world.getTileEntity(getPos().add(0, -dummy, 0).add(0, i, 0)) instanceof SampleDrillTileEntity)
				world.removeBlock(getPos().add(0, -dummy, 0).add(0, i, 0), false);
	}

	@Override
	public boolean interact(Direction side, PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(dummy!=0)
		{
			TileEntity te = world.getTileEntity(getPos().add(0, -dummy, 0));
			if(te instanceof SampleDrillTileEntity)
				return ((SampleDrillTileEntity)te).interact(side, player, hand, heldItem, hitX, hitY, hitZ);
		}

		if(!this.sample.isEmpty())
		{
			if(!world.isRemote)
			{
				player.entityDropItem(this.sample.copy(), .5f);
				this.sample = ItemStack.EMPTY;
				markDirty();
				this.markContainingBlockForUpdate(null);
			}
			return true;
		}
		else if(this.process <= 0)
		{
			if(!world.isRemote&&energyStorage.getEnergyStored() >= IEServerConfig.MACHINES.coredrill_consumption.get())
			{
				this.process = 1;
				markDirty();
				this.markContainingBlockForUpdate(null);
			}
			return true;
		}
		return false;
	}

	@Override
	public BlockPos getModelOffset(BlockState state, @Nullable Vector3i size)
	{
		return new BlockPos(0, dummy, 0);
	}
}