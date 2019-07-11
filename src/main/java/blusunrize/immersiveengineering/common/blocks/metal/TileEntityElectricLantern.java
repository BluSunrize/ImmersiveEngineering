/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.energy.wires.Connection;
import blusunrize.immersiveengineering.api.energy.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.energy.wires.localhandlers.EnergyTransferHandler.EnergyConnector;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.LightType;

import javax.annotation.Nonnull;

public class TileEntityElectricLantern extends TileEntityImmersiveConnectable implements ISpawnInterdiction, ITickable,
		IDirectionalTile, IHammerInteraction, IBlockBounds, IActiveState, ILightValue, EnergyConnector
{
	public static TileEntityType<TileEntityElectricLantern> TYPE;

	public int energyStorage = 0;
	private int energyDraw = IEConfig.Machines.lantern_energyDraw;
	private int maximumStorage = IEConfig.Machines.lantern_maximumStorage;
	public boolean active = false;
	private boolean interdictionList = false;
	private boolean flipped = false;

	public TileEntityElectricLantern()
	{
		super(TYPE);
	}

	@Override
	public void tick()
	{
		if(world.isRemote)
			return;
		if(!interdictionList&&IEConfig.Machines.lantern_spawnPrevent)
		{
			synchronized(EventHandler.interdictionTiles)
			{
				if(!EventHandler.interdictionTiles.contains(this))
					EventHandler.interdictionTiles.add(this);
			}
			interdictionList = true;
		}
		boolean b = active;
		if(energyStorage >= energyDraw)
		{
			energyStorage -= energyDraw;
			if(!active)
				active = true;
		}
		else if(active)
			active = false;

		if(active!=b)
		{
			this.markContainingBlockForUpdate(null);
			world.checkLightFor(LightType.BLOCK, getPos());
			world.addBlockEvent(getPos(), getBlockState().getBlock(), 1, 0);
		}
	}

	@Override
	public double getInterdictionRangeSquared()
	{
		return active?1024: 0;
	}

	@Override
	public void remove()
	{
		synchronized(EventHandler.interdictionTiles)
		{
			EventHandler.interdictionTiles.remove(this);
		}
		super.remove();
	}

	@Override
	public void onChunkUnloaded()
	{
		synchronized(EventHandler.interdictionTiles)
		{
			EventHandler.interdictionTiles.remove(this);
		}
		super.onChunkUnloaded();
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		active = nbt.getBoolean("active");
		energyStorage = nbt.getInt("energyStorage");
		flipped = nbt.getBoolean("flipped");
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putBoolean("active", active);
		nbt.putInt("energyStorage", energyStorage);
		nbt.putBoolean("flipped", flipped);
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==1)
		{
			this.markContainingBlockForUpdate(null);
			world.checkLightFor(LightType.BLOCK, getPos());
			return true;
		}
		return super.receiveClientEvent(id, arg);
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		return WireType.LV_CATEGORY.equals(cableType.getCategory());
	}

	@Override
	public Vec3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		BlockPos other = con.getOtherEnd(here).getPosition();
		int xDif = other.getX()-pos.getX();
		int zDif = other.getZ()-pos.getZ();
		if(Math.abs(xDif) >= Math.abs(zDif))
			return new Vec3d(xDif < 0?.25: xDif > 0?.75: .5, flipped?.9375: .0625, .5);
		return new Vec3d(.5, flipped?.9375: .0625, zDif < 0?.25: zDif > 0?.75: .5);
	}

	@Override
	public float[] getBlockBounds()
	{
		return new float[]{.1875f, 0, .1875f, .8125f, 1, .8125f};
	}

	@Override
	public boolean getIsActive()
	{
		return active;
	}

	@Override
	public int getLightValue()
	{
		return active?15: 0;
	}


	@Override
	public Direction getFacing()
	{
		return flipped?Direction.UP: Direction.NORTH;
	}

	@Override
	public void setFacing(Direction facing)
	{
	}

	@Override
	public int getFacingLimitation()
	{
		return -1;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(Direction side, float hitX, float hitY, float hitZ, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}

	@Override
	public boolean hammerUseSide(Direction side, PlayerEntity player, float hitX, float hitY, float hitZ)
	{
		flipped = !flipped;
		markContainingBlockForUpdate(null);
		world.addBlockEvent(getPos(), getBlockState().getBlock(), active?1: 0, 0);
		return true;
	}

	@Override
	public boolean isSource(ConnectionPoint cp)
	{
		return false;
	}

	@Override
	public boolean isSink(ConnectionPoint cp)
	{
		return true;
	}

	@Override
	public int getRequestedEnergy()
	{
		return maximumStorage-energyStorage;
	}

	@Override
	public void insertEnergy(int amount)
	{
		energyStorage += amount;
	}
}