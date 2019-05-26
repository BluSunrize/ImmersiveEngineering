/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.energy.ThermoelectricHandler;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.INeighbourChangeTile;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxConnector;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityThermoelectricGen extends TileEntityIEBase implements ITickable, INeighbourChangeTile, IIEInternalFluxConnector
{
	public static TileEntityType<TileEntityThermoelectricGen> TYPE;

	private int energyOutput = -1;

	public TileEntityThermoelectricGen()
	{
		super(TYPE);
	}

	@Override
	public void tick()
	{
		if(world.getGameTime()%1024==((getPos().getX()^getPos().getZ())&1023))
			recalculateEnergyOutput();
		if(!world.isRemote&&this.energyOutput > 0)
			outputEnergy(this.energyOutput);
	}

	public void outputEnergy(int amount)
	{
		for(EnumFacing fd : EnumFacing.VALUES)
		{
			TileEntity te = Utils.getExistingTileEntity(world, getPos().offset(fd));
			amount -= EnergyHelper.insertFlux(te, fd.getOpposite(), amount, false);
		}
	}

	@Override
	public void onNeighborBlockChange(BlockPos pos)
	{
		recalculateEnergyOutput();
	}

	private void recalculateEnergyOutput()
	{
		int energy = 0;
		for(EnumFacing fd : new EnumFacing[]{EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.WEST})
			if(!world.isAirBlock(getPos().offset(fd))&&!world.isAirBlock(getPos().offset(fd.getOpposite())))
			{
				int temp0 = getTemperature(getPos().offset(fd));
				int temp1 = getTemperature(getPos().offset(fd.getOpposite()));
				if(temp0 > -1&&temp1 > -1)
				{
					int diff = Math.abs(temp0-temp1);
					energy += (int)(Math.sqrt(diff)/2*IEConfig.Machines.thermoelectric_output);
				}
			}
		this.energyOutput = energy==0?-1: energy;
	}

	private int getTemperature(BlockPos pos)
	{
		//TODO
		//Fluid f = getFluid(pos);
		//if(f!=null)
		//	return f.getTemperature(world, pos);
		IBlockState state = world.getBlockState(pos);
		return ThermoelectricHandler.getTemperature(state.getBlock());
	}

	@Nullable
	Fluid getFluid(BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		IFluidState fState = state.getFluidState();
		return fState.getFluid();
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		this.energyOutput = nbt.getInt("enegyOutput");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInt("enegyOutput", this.energyOutput);
	}


	@Nonnull
	@Override
	public SideConfig getEnergySideConfig(@Nullable EnumFacing facing)
	{
		return SideConfig.OUTPUT;
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from)
	{
		return true;
	}

	IEForgeEnergyWrapper wrapper = new IEForgeEnergyWrapper(this, null);

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing)
	{
		return wrapper;
	}
}