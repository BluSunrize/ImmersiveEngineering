/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler.IExternalHeatable;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IActiveState;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

import javax.annotation.Nonnull;

public class FurnaceHeaterTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IIEInternalFluxHandler, IActiveState, IDirectionalTile
{
	public static TileEntityType<FurnaceHeaterTileEntity> TYPE;
	public FluxStorage energyStorage = new FluxStorage(32000, Math.max(256,
			Math.max(IEConfig.MACHINES.heater_consumption.get(), IEConfig.MACHINES.heater_speedupConsumption.get())));
	//public int[] sockets = new int[6];
	public boolean active = false;
	public Direction facing = Direction.NORTH;

	public FurnaceHeaterTileEntity()
	{
		super(TYPE);
	}

	@Override
	public void tick()
	{
		if(!world.isRemote)
		{
			boolean a = active;
			boolean redstonePower = world.getRedstonePowerFromNeighbors(getPos()) > 0;
			if(active&&!redstonePower)
				active = false;
			if(energyStorage.getEnergyStored() > 3200||a)
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
						if(!active)
							active = true;
					}
				}
			if(active!=a)
			{
				this.markDirty();
				this.markContainingBlockForUpdate(null);
				world.addBlockEvent(getPos(), this.getBlockState().getBlock(), 1, active?1: 0);
			}
		}
	}

	@Override
	public boolean getIsActive()
	{
		return active||world.getRedstonePowerFromNeighbors(getPos()) > 0;
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==1)
			this.active = arg==1;
		this.markContainingBlockForUpdate(null);
		return true;
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		energyStorage.readFromNBT(nbt);
		facing = Direction.byIndex(nbt.getInt("facing"));
		//		sockets = nbt.getIntArray("sockets");
		//		if(sockets.length<6)
		//			sockets = new int[0];
		active = nbt.getBoolean("active");
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		energyStorage.writeToNBT(nbt);
		nbt.putInt("facing", facing.ordinal());
		//		nbt.putIntArray("sockets", sockets);
		nbt.putBoolean("active", active);
	}

	@Nonnull
	@Override
	public FluxStorage getFluxStorage()
	{
		return energyStorage;
	}

	@Nonnull
	@Override
	public SideConfig getEnergySideConfig(Direction facing)
	{
		return facing==this.facing?SideConfig.INPUT: SideConfig.NONE;
	}

	IEForgeEnergyWrapper wrapper = new IEForgeEnergyWrapper(this, facing);

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing)
	{
		if(facing==this.facing)
		{
			if(wrapper.side!=this.facing)
				wrapper = new IEForgeEnergyWrapper(this, this.facing);
			return wrapper;
		}
		return null;
	}

	@Override
	public Direction getFacing()
	{
		return this.facing;
	}

	@Override
	public void setFacing(Direction facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 1;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return placer.isSneaking();
	}

	@Override
	public boolean canHammerRotate(Direction side, float hitX, float hitY, float hitZ, LivingEntity entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return true;
	}
}