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
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler.IExternalHeatable;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IActiveState;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.temp.IETickableBlockEntity;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FurnaceHeaterTileEntity extends IEBaseTileEntity implements IETickableBlockEntity, IIEInternalFluxHandler, IActiveState,
		IStateBasedDirectional, IHammerInteraction
{
	public FluxStorage energyStorage = new FluxStorage(32000, Math.max(256,
			Math.max(IEServerConfig.MACHINES.heater_consumption.get(), IEServerConfig.MACHINES.heater_speedupConsumption.get())));

	public FurnaceHeaterTileEntity()
	{
		super(IETileTypes.FURNACE_HEATER.get());
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
			for(Direction fd : DirectionUtils.VALUES)
			{
				BlockEntity tileEntity = Utils.getExistingTileEntity(level, getBlockPos().relative(fd));
				int consumed = 0;
				if(tileEntity!=null)
					if(tileEntity instanceof IExternalHeatable)
						consumed = ((IExternalHeatable)tileEntity).doHeatTick(energyStorage.getEnergyStored(), redstonePower);
					else
						consumed = heatTile(tileEntity, redstonePower);
				if(consumed > 0)
				{
					this.energyStorage.extractEnergy(consumed, false);
					newActive = true;
				}
			}
		if(newActive!=activeBeforeTick)
		{
			setActive(newActive);
			this.setChanged();
		}
	}

	private <T extends BlockEntity> int heatTile(T furnace, boolean redstonePower) {
		ExternalHeaterHandler.HeatableAdapter<? super T> adapter = ExternalHeaterHandler.getHeatableAdapter(furnace);
		if(adapter!=null)
			return adapter.doHeatTick(furnace, energyStorage.getEnergyStored(), redstonePower);
		else
			return 0;
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
		energyStorage.readFromNBT(nbt);
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		energyStorage.writeToNBT(nbt);
	}

	@Nonnull
	@Override
	public FluxStorage getFluxStorage()
	{
		return energyStorage;
	}

	@Nonnull
	@Override
	public IOSideConfig getEnergySideConfig(Direction facing)
	{
		return facing==this.getFacing()?IOSideConfig.INPUT: IOSideConfig.NONE;
	}

	@Nullable
	IEForgeEnergyWrapper wrapper;

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing)
	{
		if(facing==this.getFacing())
		{
			if(wrapper==null||wrapper.side!=this.getFacing())
				wrapper = new IEForgeEnergyWrapper(this, this.getFacing());
			return wrapper;
		}
		return null;
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
	public boolean canRotate(Direction axis)
	{
		return true;
	}

	@Override
	public boolean hammerUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		this.setFacing(side);
		return true;
	}
}