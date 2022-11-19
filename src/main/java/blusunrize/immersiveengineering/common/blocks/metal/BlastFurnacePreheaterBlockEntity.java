/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundBE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.ticking.IEClientTickableBE;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlastFurnacePreheaterBlockEntity extends IEBaseBlockEntity implements IStateBasedDirectional,
		IHasDummyBlocks, IModelOffsetProvider, IEClientTickableBE, ISoundBE
{
	public static final float ANGLE_PER_TICK = (float)Math.toRadians(20);
	public boolean active;
	public int dummy = 0;
	public final MutableEnergyStorage energyStorage = new MutableEnergyStorage(8000);
	public float angle = 0;
	private final MultiblockCapability<IEnergyStorage> energyCap = MultiblockCapability.make(
			this, be -> be.energyCap, BlastFurnacePreheaterBlockEntity::master, registerEnergyInput(energyStorage)
	);

	public BlastFurnacePreheaterBlockEntity(BlockEntityType<BlastFurnacePreheaterBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
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
		else
			turnOff();
		return 0;
	}

	@Override
	public void tickClient()
	{
		if(active)
			angle = (angle+ANGLE_PER_TICK)%Mth.PI;
		ImmersiveEngineering.proxy.handleTileSound(IESounds.preheater, this, active, 0.5f, 1f);
	}

	public Void turnOff()
	{
		if(active)
		{
			active = false;
			this.markContainingBlockForUpdate(null);
		}
		return null;
	}

	@Override
	public boolean isDummy()
	{
		return dummy > 0;
	}

	@Nullable
	@Override
	public BlastFurnacePreheaterBlockEntity master()
	{
		BlockPos masterPos = getBlockPos().below(dummy);
		BlockEntity te = Utils.getExistingTileEntity(level, masterPos);
		return te instanceof BlastFurnacePreheaterBlockEntity heater?heater: null;
	}

	@Override
	public void placeDummies(BlockPlaceContext ctx, BlockState state)
	{
		state = state.setValue(IEProperties.MULTIBLOCKSLAVE, true);
		for(int i = 1; i <= 2; i++)
		{
			level.setBlockAndUpdate(worldPosition.offset(0, i, 0), state);
			((BlastFurnacePreheaterBlockEntity)level.getBlockEntity(worldPosition.offset(0, i, 0))).dummy = i;
			((BlastFurnacePreheaterBlockEntity)level.getBlockEntity(worldPosition.offset(0, i, 0))).setFacing(this.getFacing());
		}
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		for(int i = 0; i <= 2; i++)
			if(level.getBlockEntity(getBlockPos().offset(0, -dummy, 0).offset(0, i, 0)) instanceof BlastFurnacePreheaterBlockEntity)
				level.removeBlock(getBlockPos().offset(0, -dummy, 0).offset(0, i, 0), false);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		dummy = nbt.getInt("dummy");
		active = nbt.getBoolean("active");
		if(descPacket)
			this.markContainingBlockForUpdate(null);
		else
			EnergyHelper.deserializeFrom(energyStorage, nbt);
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		nbt.putInt("dummy", dummy);
		nbt.putBoolean("active", active);
		if(!descPacket)
			EnergyHelper.serializeTo(energyStorage, nbt);
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		if(cap==CapabilityEnergy.ENERGY&&(side==null||(dummy==2&&side==Direction.UP)))
			return energyCap.get().cast();
		return super.getCapability(cap, side);
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
	public void afterRotation(Direction oldDir, Direction newDir)
	{
		for(int i = 0; i <= 2; i++)
		{
			BlockEntity te = level.getBlockEntity(getBlockPos().offset(0, -dummy+i, 0));
			if(te instanceof BlastFurnacePreheaterBlockEntity dummy)
			{
				dummy.setFacing(newDir);
				dummy.setChanged();
				dummy.markContainingBlockForUpdate(null);
			}
		}
	}

	@Override
	public BlockPos getModelOffset(BlockState state, @Nullable Vec3i size)
	{
		return new BlockPos(0, dummy, 0);
	}

	@Override
	public boolean shouldPlaySound(String sound)
	{
		return active;
	}
}