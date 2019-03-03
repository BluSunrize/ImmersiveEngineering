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
import blusunrize.immersiveengineering.api.energy.wires.Connection;
import blusunrize.immersiveengineering.api.energy.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.localhandlers.EnergyTransferHandler;
import blusunrize.immersiveengineering.api.energy.wires.localhandlers.EnergyTransferHandler.EnergyConnector;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import java.util.Collection;


public class TileEntityConnectorLV extends TileEntityImmersiveConnectable implements IDirectionalTile,
		IIEInternalFluxHandler, IBlockBounds, EnergyConnector, ITickable
{
	public EnumFacing facing = EnumFacing.DOWN;
	public int currentTickToMachine = 0;
	public int currentTickToNet = 0;
	public static int[] connectorInputValues = Config.IEConfig.Machines.wireConnectorInput;
	private FluxStorage storageToNet = new FluxStorage(getMaxInput(), getMaxInput(), getMaxInput());
	private FluxStorage storageToMachine = new FluxStorage(getMaxInput(), getMaxInput(), getMaxInput());

	@Override
	public void update()
	{
		if(!world.isRemote)
		{
			int maxOut = Math.min(storageToMachine.getEnergyStored(), getMaxOutput()-currentTickToMachine);
			if(maxOut > 0)
			{
				TileEntity target = Utils.getExistingTileEntity(world, pos.offset(facing));
				if(target!=null)
				{
					int inserted = EnergyHelper.insertFlux(target, facing.getOpposite(), maxOut, false);
					storageToMachine.extractEnergy(inserted, false);
				}
			}
			currentTickToMachine = 0;
			currentTickToNet = 0;
		}
	}

	@Override
	public EnumFacing getFacing()
	{
		return this.facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 0;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return true;
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
	protected boolean canTakeLV()
	{
		return true;
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing.ordinal());
		NBTTagCompound toNet = new NBTTagCompound();
		storageToNet.writeToNBT(toNet);
		nbt.setTag("toNet", toNet);
		NBTTagCompound toMachine = new NBTTagCompound();
		storageToMachine.writeToNBT(toMachine);
		nbt.setTag("toMachine", toMachine);
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = EnumFacing.byIndex(nbt.getInteger("facing"));
		NBTTagCompound toMachine = nbt.getCompoundTag("toMachine");
		storageToMachine.readFromNBT(toMachine);
		NBTTagCompound toNet = nbt.getCompoundTag("toNet");
		storageToNet.readFromNBT(toNet);
	}

	@Override
	public Vec3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		EnumFacing side = facing.getOpposite();
		double conRadius = con.type.getRenderDiameter()/2;
		return new Vec3d(.5-conRadius*side.getXOffset(), .5-conRadius*side.getYOffset(), .5-conRadius*side.getZOffset());
	}

	IEForgeEnergyWrapper energyWrapper;

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing)
	{
		if(facing!=this.facing||isRelay())
			return null;
		if(energyWrapper==null||energyWrapper.side!=this.facing)
			energyWrapper = new IEForgeEnergyWrapper(this, this.facing);
		return energyWrapper;
	}

	@Override
	public FluxStorage getFluxStorage()
	{
		return storageToNet;
	}

	@Override
	public SideConfig getEnergySideConfig(EnumFacing facing)
	{
		return (!isRelay()&&facing==this.facing)?SideConfig.INPUT: SideConfig.NONE;
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from)
	{
		if(isRelay())
			return false;
		return from==facing;
	}

	@Override
	public int receiveEnergy(EnumFacing from, int energy, boolean simulate)
	{
		if(world.isRemote||isRelay())
			return 0;
		energy = Math.min(getMaxInput()-currentTickToNet, energy);
		if(energy <= 0)
			return 0;

		int accepted = Math.min(Math.min(getMaxOutput(), getMaxInput()), energy);
		accepted = Math.min(getMaxOutput()-storageToNet.getEnergyStored(), accepted);
		if(accepted <= 0)
			return 0;

		if(!simulate)
		{
			storageToNet.modifyEnergyStored(accepted);
			currentTickToNet += accepted;
			markDirty();
		}

		return accepted;
	}

	@Override
	public int getEnergyStored(EnumFacing from)
	{
		if(isRelay())
			return 0;
		return storageToNet.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from)
	{
		if(isRelay())
			return 0;
		return getMaxInput();
	}

	@Override
	public int extractEnergy(EnumFacing from, int energy, boolean simulate)
	{
		return 0;
	}

	public int getMaxInput()
	{
		return connectorInputValues[0];
	}

	public int getMaxOutput()
	{
		return connectorInputValues[0];
	}

	@Override
	public float[] getBlockBounds()
	{
		float length = this instanceof TileEntityRelayHV?.875f: this instanceof TileEntityConnectorHV?.75f: this instanceof TileEntityConnectorMV?.5625f: .5f;
		float wMin = this instanceof TileEntityConnectorStructural?.25f: .3125f;
		float wMax = this instanceof TileEntityConnectorStructural?.75f: .6875f;
		switch(facing.getOpposite())
		{
			case UP:
				return new float[]{wMin, 0, wMin, wMax, length, wMax};
			case DOWN:
				return new float[]{wMin, 1-length, wMin, wMax, 1, wMax};
			case SOUTH:
				return new float[]{wMin, wMin, 0, wMax, wMax, length};
			case NORTH:
				return new float[]{wMin, wMin, 1-length, wMax, wMax, 1};
			case EAST:
				return new float[]{0, wMin, wMin, length, wMax, wMax};
			case WEST:
				return new float[]{1-length, wMin, wMin, 1, wMax, wMax};
		}
		return new float[]{0, 0, 0, 1, 1, 1};
	}

	@Override
	public boolean isSource(ConnectionPoint cp)
	{
		return !isRelay();
	}

	@Override
	public boolean isSink(ConnectionPoint cp)
	{
		return !isRelay();
	}

	@Override
	public int getAvailableEnergy()
	{
		return storageToNet.getEnergyStored();
	}

	@Override
	public int getRequestedEnergy()
	{
		return storageToMachine.getMaxEnergyStored()-storageToMachine.getEnergyStored();
	}

	@Override
	public void insertEnergy(int amount)
	{
		storageToMachine.receiveEnergy(amount, false);
	}

	@Override
	public void extractEnergy(int amount)
	{
		storageToNet.extractEnergy(amount, false);
	}

	@Override
	public Collection<ResourceLocation> getRequestedHandlers()
	{
		return ImmutableList.of(EnergyTransferHandler.ID);
	}
}