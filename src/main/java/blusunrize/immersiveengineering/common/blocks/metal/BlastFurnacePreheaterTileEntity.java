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
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IETileTypes;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlastFurnacePreheaterTileEntity extends IEBaseTileEntity implements IIEInternalFluxHandler,
		IStateBasedDirectional, IHasDummyBlocks, IModelOffsetProvider
{
	public boolean active;
	public int dummy = 0;
	public FluxStorage energyStorage = new FluxStorage(8000);
	public float angle = 0;
	public long lastRenderTick = -1;

	public BlastFurnacePreheaterTileEntity(BlockPos pos, BlockState state)
	{
		super(IETileTypes.BLASTFURNACE_PREHEATER.get(), pos, state);
	}

	public int doSpeedup()
	{
		int consumed = IEServerConfig.MACHINES.preheater_consumption.get();
		if(this.energyStorage.extractEnergy(consumed, true)==consumed)
		{
			if(!active)
			{
				active = true;
				this.markContainingBlockForUpdate(null);
			}
			this.energyStorage.extractEnergy(consumed, false);
			return 1;
		}
		else if(active)
		{
			active = false;
			this.markContainingBlockForUpdate(null);
		}
		return 0;
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
			((BlastFurnacePreheaterTileEntity)level.getBlockEntity(worldPosition.offset(0, i, 0))).dummy = i;
			((BlastFurnacePreheaterTileEntity)level.getBlockEntity(worldPosition.offset(0, i, 0))).setFacing(this.getFacing());
		}
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		for(int i = 0; i <= 2; i++)
			if(level.getBlockEntity(getBlockPos().offset(0, -dummy, 0).offset(0, i, 0)) instanceof BlastFurnacePreheaterTileEntity)
				level.removeBlock(getBlockPos().offset(0, -dummy, 0).offset(0, i, 0), false);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		dummy = nbt.getInt("dummy");
		energyStorage.readFromNBT(nbt);
		active = nbt.getBoolean("active");
		if(descPacket)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		nbt.putInt("dummy", dummy);
		nbt.putBoolean("active", active);
		energyStorage.writeToNBT(nbt);
	}

	@Nonnull
	@Override
	public FluxStorage getFluxStorage()
	{
		if(dummy > 0)
		{
			BlockEntity te = level.getBlockEntity(getBlockPos().offset(0, -dummy, 0));
			if(te instanceof BlastFurnacePreheaterTileEntity)
				return ((BlastFurnacePreheaterTileEntity)te).getFluxStorage();
		}
		return energyStorage;
	}

	@Nonnull
	@Override
	public IOSideConfig getEnergySideConfig(Direction facing)
	{
		return dummy==2&&facing==Direction.UP?IOSideConfig.INPUT: IOSideConfig.NONE;
	}

	IEForgeEnergyWrapper wrapper = new IEForgeEnergyWrapper(this, Direction.UP);

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing)
	{
		if(dummy==2&&facing==Direction.UP)
			return wrapper;
		return null;
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
	public void afterRotation(Direction oldDir, Direction newDir)
	{
		for(int i = 0; i <= 2; i++)
		{
			BlockEntity te = level.getBlockEntity(getBlockPos().offset(0, -dummy+i, 0));
			if(te instanceof BlastFurnacePreheaterTileEntity)
			{
				((BlastFurnacePreheaterTileEntity)te).setFacing(newDir);
				te.setChanged();
				((BlastFurnacePreheaterTileEntity)te).markContainingBlockForUpdate(null);
			}
		}
	}

	@Override
	public BlockPos getModelOffset(BlockState state, @Nullable Vec3i size)
	{
		return new BlockPos(0, dummy, 0);
	}
}