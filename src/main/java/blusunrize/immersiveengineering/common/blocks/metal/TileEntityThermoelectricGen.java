package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.energy.ThermoelectricHandler;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxConnection;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import cofh.api.energy.IEnergyConnection;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidBlock;

public class TileEntityThermoelectricGen extends TileEntityIEBase implements ITickable, IFluxConnection,IEnergyConnection
{
	@Override
	public void update()
	{
		if(!worldObj.isRemote)
		{
			int energy = 0;
			for(EnumFacing fd : new EnumFacing[]{EnumFacing.DOWN,EnumFacing.NORTH,EnumFacing.WEST})
				if(!worldObj.isAirBlock(getPos().offset(fd)) && !worldObj.isAirBlock(getPos().offset(fd.getOpposite())))
				{
					int temp0 = getTemperature(getPos().offset(fd));
					int temp1 = getTemperature(getPos().offset(fd.getOpposite()));
					if(temp0>-1&&temp1>-1)
					{
						int diff = Math.abs(temp0-temp1);
						energy += (int) (Math.sqrt(diff)/2 *Config.getDouble("thermoelectric_output"));
					}
				}
			outputEnergy(energy);
		}
	}

	public void outputEnergy(int amount)
	{
		for(EnumFacing fd : EnumFacing.VALUES)
		{
			TileEntity te = worldObj.getTileEntity(getPos().offset(fd));
			if(te instanceof IFluxReceiver)
				amount -= ((IFluxReceiver)te).receiveEnergy(fd.getOpposite(), amount, false);
		}
	}


	int getTemperature(BlockPos pos)
	{
		Fluid f = getFluid(pos);
		if(f!=null)
			return f.getTemperature(worldObj, pos);
		IBlockState state = worldObj.getBlockState(pos);
		return ThermoelectricHandler.getTemperature(state.getBlock(), state.getBlock().getMetaFromState(state));
	}
	Fluid getFluid(BlockPos pos)
	{
		IBlockState state = worldObj.getBlockState(pos);
		Block b = state.getBlock();
		Fluid f = FluidRegistry.lookupFluidForBlock(b);
		if(f==null && b instanceof BlockDynamicLiquid && b.getMetaFromState(state)==0)
			if(state.getMaterial().equals(Material.WATER))
				f = FluidRegistry.WATER;
			else if(state.getMaterial().equals(Material.LAVA))
				f = FluidRegistry.LAVA;
		if(b instanceof IFluidBlock && !((IFluidBlock)b).canDrain(worldObj, pos))
			return null;
		if(b instanceof BlockStaticLiquid && b.getMetaFromState(state)!=0)
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
	public boolean canConnectEnergy(EnumFacing from)
	{
		return true;
	}
}