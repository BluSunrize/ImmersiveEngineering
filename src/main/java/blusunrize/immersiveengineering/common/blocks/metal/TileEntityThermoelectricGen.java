package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidBlock;
import blusunrize.immersiveengineering.api.energy.ThermoelectricHandler;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyReceiver;

public class TileEntityThermoelectricGen extends TileEntityIEBase implements IEnergyConnection
{
	@Override
	public void updateEntity()
	{
		if(!worldObj.isRemote)
			for(ForgeDirection fd : new ForgeDirection[]{ForgeDirection.UP,ForgeDirection.SOUTH,ForgeDirection.EAST})
				if(!worldObj.isAirBlock(xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ) && !worldObj.isAirBlock(xCoord+fd.getOpposite().offsetX, yCoord+fd.getOpposite().offsetY, zCoord+fd.getOpposite().offsetZ))
				{
					int temp0 = getTemperature(xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ);
					int temp1 = getTemperature(xCoord+fd.getOpposite().offsetX, yCoord+fd.getOpposite().offsetY, zCoord+fd.getOpposite().offsetZ);
					if(temp0>-1&&temp1>-1)
					{
						int diff = Math.abs(temp0-temp1);
						int energy = (int) (Math.sqrt(diff)/2 *Config.getDouble("thermoelectric_output"));
						outputEnergy(energy);
					}
				}
	}

	public void outputEnergy(int amount)
	{
		for(ForgeDirection fd : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity te = worldObj.getTileEntity(xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ);
			if(te instanceof IEnergyReceiver)
			{
				amount -= ((IEnergyReceiver) te).receiveEnergy(fd.getOpposite(), amount, false);
			}
		}
	}


	int getTemperature(int x, int y, int z)
	{
		Fluid f = getFluid(x,y,z);
		if(f!=null)
			return f.getTemperature(worldObj, x, y, z);
		return ThermoelectricHandler.getTemperature(worldObj.getBlock(x,y,z), worldObj.getBlockMetadata(x,y,z));
	}
	Fluid getFluid(int x, int y, int z)
	{
		Fluid f = FluidRegistry.lookupFluidForBlock(worldObj.getBlock(x, y, z));
		if(f==null && worldObj.getBlock(x, y, z) instanceof BlockDynamicLiquid && worldObj.getBlockMetadata(x, y, z)==0)
			if(worldObj.getBlock(x, y, z).getMaterial().equals(Material.water))
				f = FluidRegistry.WATER;
			else if(worldObj.getBlock(x, y, z).getMaterial().equals(Material.lava))
				f = FluidRegistry.LAVA;
		if(worldObj.getBlock(x, y, z) instanceof IFluidBlock && !((IFluidBlock)worldObj.getBlock(x, y, z)).canDrain(worldObj, x, y, z))
			return null;
		if(worldObj.getBlock(x, y, z) instanceof BlockStaticLiquid && worldObj.getBlockMetadata(x, y, z)!=0)
			return null;
		if(f==null)
			return null;
		return f;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return true;
	}
}