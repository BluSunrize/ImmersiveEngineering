/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler.IExternalHeatable;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IActiveState;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.ResettableCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class FurnaceHeaterBlockEntity extends IEBaseBlockEntity implements IEServerTickableBE, IActiveState,
		IStateBasedDirectional, IHammerInteraction
{
	public MutableEnergyStorage energyStorage = new MutableEnergyStorage(32000, Math.max(256,
			Math.max(IEServerConfig.MACHINES.heater_consumption.get(), IEServerConfig.MACHINES.heater_speedupConsumption.get())));
	private final ResettableCapability<IEnergyStorage> energyCap = registerEnergyInput(energyStorage);
	private final Collection<CapabilityReference<IExternalHeatable>> heatables = CapabilityReference.forAllNeighbors(
			this, ExternalHeaterHandler.CAPABILITY
	).values();

	public FurnaceHeaterBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.FURNACE_HEATER.get(), pos, state);
	}

	@Override
	public void tickServer()
	{
		boolean activeBeforeTick = getIsActive();
		boolean redstonePower = isRSPowered();
		boolean newActive = activeBeforeTick;
		if(activeBeforeTick&&!redstonePower)
			newActive = false;
		if(energyStorage.getEnergyStored() > 3200||activeBeforeTick)
			for(CapabilityReference<IExternalHeatable> capRef : heatables)
			{
				IExternalHeatable heatable = capRef.getNullable();
				if(heatable!=null)
				{
					int consumed = heatable.doHeatTick(energyStorage.getEnergyStored(), redstonePower);
					if(consumed > 0)
					{
						this.energyStorage.extractEnergy(consumed, false);
						newActive = true;
					}
				}
			}
		if(newActive!=activeBeforeTick)
		{
			setActive(newActive);
			this.setChanged();
		}
	}

	@Override
	public boolean triggerEvent(int id, int arg)
	{
		this.markContainingBlockForUpdate(null);
		return true;
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		EnergyHelper.deserializeFrom(energyStorage, nbt);
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		EnergyHelper.serializeTo(energyStorage, nbt);
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		if(cap==CapabilityEnergy.ENERGY&&(side==null||side==getFacing()))
			return energyCap.cast();
		return super.getCapability(cap, side);
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_ALL;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.PISTON_LIKE;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return placer.isShiftKeyDown();
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean hammerUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		this.setFacing(side);
		return true;
	}
}
