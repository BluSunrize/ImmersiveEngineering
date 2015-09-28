package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.HashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPipe.DirectionalFluidOutput;
import blusunrize.immersiveengineering.common.util.Utils;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;

public class TileEntityFluidPump extends TileEntityIEBase implements IFluidHandler, IEnergyReceiver
{
	public int[] sideConfig = new int[] {0,-1,-1,-1,-1,-1};
	public boolean dummy = true;
	public FluidTank tank = new FluidTank(4000);
	public EnergyStorage energyStorage = new EnergyStorage(8000);

	@Override
	public void updateEntity()
	{
		if(dummy || worldObj.isRemote)
			return;
		if(tank.getFluidAmount()>0)
		{
			int i = outputFluid(tank.getFluid(), false);
			tank.drain(i, true);
		}

		if(worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)||worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord+1, zCoord))
		{
			for(int i=0; i<6; i++)
				if(sideConfig[i]==0)
				{
					ForgeDirection fd = ForgeDirection.getOrientation(i);
					TileEntity tile = worldObj.getTileEntity(xCoord+fd.offsetX,yCoord+fd.offsetY,zCoord+fd.offsetZ);
					if(tile instanceof IFluidHandler)
					{
						FluidStack drain = ((IFluidHandler)tile).drain(fd.getOpposite(), 500, false);
						if(drain==null || drain.amount<=0)
							continue;
						if(((IFluidHandler)tile).canDrain(fd.getOpposite(), drain.getFluid()))
						{
							int out = this.outputFluid(drain, false);
							((IFluidHandler)tile).drain(fd.getOpposite(), out, true);
						}
					}
					else if(worldObj.getTotalWorldTime()%8==((xCoord^zCoord)&7))
					{
						FluidStack fs = Utils.drainFluidBlock(worldObj, xCoord+fd.offsetX,yCoord+fd.offsetY,zCoord+fd.offsetZ, false);
						if(fs!=null && this.tank.fill(fs,false)==fs.amount)
						{
							int d = this.energyStorage.extractEnergy(Config.getInt("pump_consumption"), true);
							if(d>=Config.getInt("pump_consumption"))
							{
								this.energyStorage.extractEnergy(Config.getInt("pump_consumption"), false);
								fs = Utils.drainFluidBlock(worldObj, xCoord+fd.offsetX,yCoord+fd.offsetY,zCoord+fd.offsetZ, true);
								this.tank.fill(fs, true);
							}
						}
					}
				}
		}
	}

	public int outputFluid(FluidStack fs, boolean simulate)
	{
		if(fs==null)
			return 0;

		int canAccept = fs.amount;
		if(canAccept<=0)
			return 0;

		final int fluidForSort = canAccept;
		int sum = 0;
		HashMap<DirectionalFluidOutput,Integer> sorting = new HashMap<DirectionalFluidOutput,Integer>();
		for(int i=0; i<6; i++)
			if(sideConfig[i]==1)
			{
				ForgeDirection fd = ForgeDirection.getOrientation(i);
				TileEntity tile = worldObj.getTileEntity(xCoord+fd.offsetX,yCoord+fd.offsetY,zCoord+fd.offsetZ);
				if(tile instanceof IFluidHandler && ((IFluidHandler)tile).canFill(ForgeDirection.getOrientation(i).getOpposite(), fs.getFluid()))
				{
					FluidStack insertResource = new FluidStack(fs.getFluid(), fs.amount, new NBTTagCompound());
					insertResource.tag.setBoolean("pressurized", true);
					int temp = ((IFluidHandler)tile).fill(fd.getOpposite(), insertResource, false);
					if(temp>0)
					{
						sorting.put(new DirectionalFluidOutput((IFluidHandler)tile, fd), temp);
						sum += temp;
					}
				}
			}
		if(sum>0)
		{
			int f = 0;
			int i=0;
			for(DirectionalFluidOutput output : sorting.keySet())
			{
				float prio = sorting.get(output)/(float)sum;
				int amount = (int)(fluidForSort*prio);
				if(i++ == sorting.size()-1)
					amount = canAccept;
				FluidStack insertResource = new FluidStack(fs.getFluid(), amount, new NBTTagCompound());
				insertResource.tag.setBoolean("pressurized", true);
				int r = output.output.fill(output.direction.getOpposite(), insertResource, !simulate);
				f += r;
				canAccept -= r;
				if(canAccept<=0)
					break;
			}
			return f;
		}
		return 0;
	}


	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		sideConfig = nbt.getIntArray("sideConfig");
		if(sideConfig==null || sideConfig.length!=6)
			sideConfig = new int[]{0,-1,-1,-1,-1,-1};
		dummy = nbt.getBoolean("dummy");
		tank.readFromNBT(nbt.getCompoundTag("tank"));
		energyStorage.readFromNBT(nbt);
		if(descPacket)
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setIntArray("sideConfig", sideConfig);
		nbt.setBoolean("dummy", dummy);
		nbt.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
		energyStorage.writeToNBT(nbt);
	}

	public void toggleSide(int side)
	{
		if(side!=1)
		{
			sideConfig[side]++;
			if(sideConfig[side]>1)
				sideConfig[side]=-1;
		}
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if(resource==null || dummy || from==null || from==ForgeDirection.UNKNOWN || sideConfig[from.ordinal()]!=0)
			return 0;
		return this.tank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if(resource==null)
			return null;
		return this.drain(from, resource.amount, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		if(dummy || from==null || from==ForgeDirection.UNKNOWN || sideConfig[from.ordinal()]!=1)
			return null;
		return tank.drain(maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return !dummy && from!=null && from!=ForgeDirection.UNKNOWN &&sideConfig[from.ordinal()]==0;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return !dummy && from!=null && from!=ForgeDirection.UNKNOWN &&sideConfig[from.ordinal()]==1;
	}
	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		if(dummy)
		{
			if(worldObj.getTileEntity(xCoord, yCoord-1, zCoord) instanceof TileEntityFluidPump)	
				return ((TileEntityFluidPump)worldObj.getTileEntity(xCoord, yCoord-1, zCoord)).getTankInfo(from);
			return new FluidTankInfo[0];
		}
		return new FluidTankInfo[]{tank.getInfo()};
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return from == ForgeDirection.UP || (!dummy&&from!=ForgeDirection.UNKNOWN && this.sideConfig[from.ordinal()]==-1);
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if(dummy)
		{
			if(worldObj.getTileEntity(xCoord, yCoord-1, zCoord) instanceof TileEntityFluidPump)	
				return ((TileEntityFluidPump)worldObj.getTileEntity(xCoord, yCoord-1, zCoord)).receiveEnergy(from, maxReceive, simulate);
			return 0;
		}
		return energyStorage.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		if(dummy)
		{
			if(worldObj.getTileEntity(xCoord, yCoord-1, zCoord) instanceof TileEntityFluidPump)	
				return ((TileEntityFluidPump)worldObj.getTileEntity(xCoord, yCoord-1, zCoord)).getEnergyStored(from);
			return 0;
		}
		return energyStorage.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		if(dummy)
		{
			if(worldObj.getTileEntity(xCoord, yCoord-1, zCoord) instanceof TileEntityFluidPump)	
				return ((TileEntityFluidPump)worldObj.getTileEntity(xCoord, yCoord-1, zCoord)).getMaxEnergyStored(from);
			return 0;
		}
		return energyStorage.getMaxEnergyStored();
	}
}