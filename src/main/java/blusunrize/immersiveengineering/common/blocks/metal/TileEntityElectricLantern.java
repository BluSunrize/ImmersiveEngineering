/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.PropertyBoolInverted;
import blusunrize.immersiveengineering.api.energy.wires.Connection;
import blusunrize.immersiveengineering.api.energy.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.localhandlers.EnergyTransferHandler.EnergyConnector;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;

import javax.annotation.Nonnull;

public class TileEntityElectricLantern extends TileEntityImmersiveConnectable implements ISpawnInterdiction, ITickable,
		IDirectionalTile, IHammerInteraction, IBlockBounds, IActiveState, ILightValue, EnergyConnector
{
	public int energyStorage = 0;
	private int energyDraw = IEConfig.Machines.lantern_energyDraw;
	private int maximumStorage = IEConfig.Machines.lantern_maximumStorage;
	public boolean active = false;
	private boolean interdictionList = false;
	private boolean flipped = false;

	@Override
	public void update()
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
			world.checkLightFor(EnumSkyBlock.BLOCK, getPos());
			world.addBlockEvent(getPos(), getBlockState(), 1, 0);
		}
	}

	@Override
	public double getInterdictionRangeSquared()
	{
		return active?1024: 0;
	}

	@Override
	public void invalidate()
	{
		synchronized(EventHandler.interdictionTiles)
		{
			EventHandler.interdictionTiles.remove(this);
		}
		super.invalidate();
	}

	@Override
	public void onChunkUnload()
	{
		synchronized(EventHandler.interdictionTiles)
		{
			EventHandler.interdictionTiles.remove(this);
		}
		super.onChunkUnload();
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		active = nbt.getBoolean("active");
		energyStorage = nbt.getInt("energyStorage");
		flipped = nbt.getBoolean("flipped");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setBoolean("active", active);
		nbt.setInt("energyStorage", energyStorage);
		nbt.setBoolean("flipped", flipped);
	}

	@Override
	protected boolean canTakeLV()
	{
		return true;
	}

	@Override
	protected boolean isRelay()
	{
		return true;
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==1)
		{
			this.markContainingBlockForUpdate(null);
			world.checkLightFor(EnumSkyBlock.BLOCK, getPos());
			return true;
		}
		return super.receiveClientEvent(id, arg);
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
	public PropertyBoolInverted getBoolProperty(Class<? extends IUsesBooleanProperty> inf)
	{
		return IEProperties.BOOLEANS[0];
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
	public EnumFacing getFacing()
	{
		return flipped?EnumFacing.UP: EnumFacing.NORTH;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
	}

	@Override
	public int getFacingLimitation()
	{
		return -1;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return false;
	}

	@Override
	public boolean hammerUseSide(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ)
	{
		flipped = !flipped;
		markContainingBlockForUpdate(null);
		world.addBlockEvent(getPos(), getBlockState(), active?1: 0, 0);
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