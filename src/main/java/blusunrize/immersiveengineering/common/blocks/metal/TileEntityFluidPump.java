package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
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

	boolean checkingArea = false;
	Fluid searchFluid = null;
	ArrayList<ChunkCoordinates> openList = new ArrayList<ChunkCoordinates>();
	ArrayList<ChunkCoordinates> closedList = new ArrayList<ChunkCoordinates>();
	ArrayList<ChunkCoordinates> checked = new ArrayList<ChunkCoordinates>();

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
					else if(worldObj.getTotalWorldTime()%20==((xCoord^zCoord)&19) && worldObj.getBlock(xCoord+fd.offsetX,yCoord+fd.offsetY,zCoord+fd.offsetZ)==Blocks.water && tank.fill(new FluidStack(FluidRegistry.WATER,1000), false)==1000 && this.energyStorage.extractEnergy(Config.getInt("pump_consumption"), true)>=Config.getInt("pump_consumption"))
					{
						int connectedSources = 0;
						for(int j=2; j<6; j++)
							if(worldObj.getBlock(xCoord+fd.offsetX+(j==4?-1:j==5?1: 0), yCoord+fd.offsetY, zCoord+fd.offsetZ+(j==2?-1:j==3?1: 0))==Blocks.water && worldObj.getBlockMetadata(xCoord+fd.offsetX+(j==4?-1:j==5?1: 0), yCoord+fd.offsetY, zCoord+fd.offsetZ+(j==2?-1:j==3?1: 0))==0)
								connectedSources++;
						if(connectedSources>1)
						{
							this.energyStorage.extractEnergy(Config.getInt("pump_consumption"), false);
							this.tank.fill(new FluidStack(FluidRegistry.WATER,1000), true);
						}
					}
				}
			if(worldObj.getTotalWorldTime()%40==((xCoord^zCoord)&39))
			{
				if(closedList.isEmpty())
					prepareAreaCheck();
				else
				{
					int target = closedList.size()-1;
					ChunkCoordinates cc = closedList.get(target);
					FluidStack fs = Utils.drainFluidBlock(worldObj, cc.posX,cc.posY,cc.posZ, false);
					if(fs==null)
						closedList.remove(target);
					else if(tank.fill(fs, false)==fs.amount && this.energyStorage.extractEnergy(Config.getInt("pump_consumption"), true)>=Config.getInt("pump_consumption"))
					{
						this.energyStorage.extractEnergy(Config.getInt("pump_consumption"), false);
						fs = Utils.drainFluidBlock(worldObj, cc.posX,cc.posY,cc.posZ, true);
						int rainbow = (closedList.size()%11)+1;
						if(rainbow>6)
							rainbow+=2;
						if(rainbow>9)
							rainbow++;
						worldObj.setBlock( cc.posX,cc.posY,cc.posZ, Blocks.stained_glass,rainbow, 0x3);
						this.tank.fill(fs, true);
						closedList.remove(target);
					}
				}
			}
		}

		if(checkingArea)
			checkAreaTick();
	}

	public void prepareAreaCheck()
	{
		openList.clear();
		closedList.clear();
		checked.clear();
		for(int i=0; i<6; i++)
			if(sideConfig[i]==0)
			{
				ForgeDirection fd = ForgeDirection.getOrientation(i);
				openList.add(new ChunkCoordinates(xCoord+fd.offsetX,yCoord+fd.offsetY,zCoord+fd.offsetZ));
				checkingArea = true;
			}
	}
	public void checkAreaTick()
	{
		ChunkCoordinates next = null;
		final int closedListMax = 2048;
		int timeout = 0;
		while(timeout<64 && closedList.size()<closedListMax && !openList.isEmpty())
		{
			timeout++;
			next = openList.get(0);
			if(!checked.contains(next))
			{
				FluidStack fs = Utils.drainFluidBlock(worldObj, next.posX,next.posY,next.posZ, false);
				if(fs!=null && fs.getFluid()!=FluidRegistry.WATER && (searchFluid==null || fs.getFluid()==searchFluid))
				{
					if(searchFluid==null)
						searchFluid = fs.getFluid();
					closedList.add(next);
					for(ForgeDirection fd : ForgeDirection.VALID_DIRECTIONS)
					{
						ChunkCoordinates cc2 = new ChunkCoordinates(next.posX+fd.offsetX,next.posY+fd.offsetY,next.posZ+fd.offsetZ);
						FluidStack fs2 = Utils.drainFluidBlock(worldObj, cc2.posX,cc2.posY,cc2.posZ, false);
						if(!checked.contains(cc2) && !closedList.contains(cc2) && !openList.contains(cc2) && fs2!=null && fs2.getFluid()!=FluidRegistry.WATER && (searchFluid==null || fs2.getFluid()==searchFluid))
							openList.add(cc2);
					}
				}
				checked.add(next);
			}
			openList.remove(0);
		}
		if(closedList.size()>=closedListMax || openList.isEmpty())
			checkingArea = false;
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
			TileEntity te = worldObj.getTileEntity(xCoord, yCoord-1, zCoord);
			if(te instanceof TileEntityFluidPump)	
				return ((TileEntityFluidPump)te).getTankInfo(from);
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
			TileEntity te = worldObj.getTileEntity(xCoord, yCoord-1, zCoord);
			if(te instanceof TileEntityFluidPump)	
				return ((TileEntityFluidPump)te).receiveEnergy(from, maxReceive, simulate);
			return 0;
		}
		return energyStorage.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		if(dummy)
		{
			TileEntity te = worldObj.getTileEntity(xCoord, yCoord-1, zCoord);
			if(te instanceof TileEntityFluidPump)	
				return ((TileEntityFluidPump)te).getEnergyStored(from);
			return 0;
		}
		return energyStorage.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		if(dummy)
		{
			TileEntity te = worldObj.getTileEntity(xCoord, yCoord-1, zCoord);
			if(te instanceof TileEntityFluidPump)	
				return ((TileEntityFluidPump)te).getMaxEnergyStored(from);
			return 0;
		}
		return energyStorage.getMaxEnergyStored();
	}
}