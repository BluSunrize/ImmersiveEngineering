package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidBlock;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import cofh.api.energy.IEnergyReceiver;

public class TileEntityThermoelectricGen extends TileEntityIEBase
{
	@Override
	public void updateEntity()
	{
		if(!worldObj.isRemote)
			for(ForgeDirection fd : new ForgeDirection[]{ForgeDirection.UP,ForgeDirection.SOUTH,ForgeDirection.EAST})
				if(!worldObj.isAirBlock(xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ) && !worldObj.isAirBlock(xCoord+fd.getOpposite().offsetX, yCoord+fd.getOpposite().offsetY, zCoord+fd.getOpposite().offsetZ))
				{
					Fluid f0 = getFluid(xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ);
					Fluid f1 = getFluid(xCoord+fd.getOpposite().offsetX, yCoord+fd.getOpposite().offsetY, zCoord+fd.getOpposite().offsetZ);
					int temp0 = f0!=null?f0.getTemperature(worldObj, xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ): worldObj.getBlock(xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ).getMaterial()==Material.ice?273: -1;
					int temp1 = f1!=null?f1.getTemperature(worldObj, xCoord+fd.getOpposite().offsetX, yCoord+fd.getOpposite().offsetY, zCoord+fd.getOpposite().offsetZ): worldObj.getBlock(xCoord+fd.getOpposite().offsetX, yCoord+fd.getOpposite().offsetY, zCoord+fd.getOpposite().offsetZ).getMaterial()==Material.ice?273: -1;
					if(temp0!=-1&&temp1!=-1)
					{
						int diff = Math.abs(temp0-temp1);
						int energy = (int) (Math.sqrt(diff)/2);
						outputEnergy(energy);
					}
				}

	}

	public void outputEnergy(int amount)
	{
		for(ForgeDirection fd : ForgeDirection.VALID_DIRECTIONS)
			if(worldObj.getTileEntity(xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ) instanceof IEnergyReceiver)
			{
				IEnergyReceiver ier = (IEnergyReceiver)worldObj.getTileEntity(xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ);
				amount -= ier.receiveEnergy(fd.getOpposite(), amount, false);
			}
	}

	static int roomTemp = 295;
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
	public void readCustomNBT(NBTTagCompound nbt)
	{
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt)
	{
	}

}
