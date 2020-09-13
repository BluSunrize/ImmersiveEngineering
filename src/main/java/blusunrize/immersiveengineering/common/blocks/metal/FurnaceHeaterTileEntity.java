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
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IActiveState;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FurnaceHeaterTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IIEInternalFluxHandler, IActiveState,
		IStateBasedDirectional
{
	public FluxStorage energyStorage = new FluxStorage(32000, Math.max(256,
			Math.max(IEConfig.MACHINES.heater_consumption.get(), IEConfig.MACHINES.heater_speedupConsumption.get())));

	public FurnaceHeaterTileEntity()
	{
		super(IETileTypes.FURNACE_HEATER.get());
	}

	@Override
	public void tick()
	{
		if(!world.isRemote)
		{
			boolean activeBeforeTick = getIsActive();
			boolean redstonePower = isRSPowered();
			boolean newActive = activeBeforeTick;
			if(activeBeforeTick&&!redstonePower)
				newActive = false;
			if(energyStorage.getEnergyStored() > 3200||activeBeforeTick)
				for(Direction fd : Direction.VALUES)
				{
					TileEntity tileEntity = Utils.getExistingTileEntity(world, getPos().offset(fd));
					int consumed = 0;
					if(tileEntity!=null)
						if(tileEntity instanceof IExternalHeatable)
							consumed = ((IExternalHeatable)tileEntity).doHeatTick(energyStorage.getEnergyStored(), redstonePower);
						else
						{
							ExternalHeaterHandler.HeatableAdapter adapter = ExternalHeaterHandler.getHeatableAdapter(tileEntity.getClass());
							if(adapter!=null)
								consumed = adapter.doHeatTick(tileEntity, energyStorage.getEnergyStored(), redstonePower);
						}
					if(consumed > 0)
					{
						this.energyStorage.extractEnergy(consumed, false);
						newActive = true;
					}
				}
			if(newActive!=activeBeforeTick)
			{
				setActive(newActive);
				this.markDirty();
			}
		}
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		this.markContainingBlockForUpdate(null);
		return true;
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		energyStorage.readFromNBT(nbt);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
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
	public EnumProperty<Direction> getFacingProperty()
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
		return placer.isSneaking();
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3d hit, LivingEntity entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return true;
	}
}