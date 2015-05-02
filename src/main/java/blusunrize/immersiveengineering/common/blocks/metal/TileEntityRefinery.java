package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockRefinery;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityRefinery extends TileEntityMultiblockPart implements IFluidHandler, IEnergyReceiver
{
	public int facing = 2;
	public FluidTank tank0 = new FluidTank(12000);
	public FluidTank tank1 = new FluidTank(12000);
	public FluidTank tank2 = new FluidTank(12000);
	public EnergyStorage energyStorage = new EnergyStorage(32000,256,32000);


	public TileEntityRefinery master()
	{
		if(offset[0]==0&&offset[1]==0&&offset[2]==0)
			return null;
		TileEntity te = worldObj.getTileEntity(xCoord-offset[0], yCoord-offset[1], zCoord-offset[2]);
		return te instanceof TileEntityRefinery?(TileEntityRefinery)te : null;
	}

	public static boolean _Immovable()
	{
		return true;
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		ItemStack s = MultiblockRefinery.instance.getStructureManual()[pos%15/5][pos%5][pos/15];
		return s!=null?s.copy():null;
	}

	@Override
	public void updateEntity()
	{
		if(!formed || pos!=17)
			return;

		if(!worldObj.isRemote && worldObj.getBlockPowerInput(xCoord+(facing==4?-1:facing==5?1:facing==2?-2:2),yCoord+1,zCoord+(facing==2?-1:facing==3?1:facing==4?2:-2))<=0)
		{
			boolean update = false;
			if(tank0.getFluidAmount()>=8 && tank1.getFluidAmount()>=8)
			{
				int consumed = Config.getInt("refinery_consumption");
				if(energyStorage.extractEnergy(consumed, true)==consumed && tank2.fill(new FluidStack(IEContent.fluidBiodiesel,16), false)==16)
				{
					energyStorage.extractEnergy(consumed, false);
					tank0.drain(8, true);
					tank1.drain(8, true);
					tank2.fill(new FluidStack(IEContent.fluidBiodiesel,16), true);
					update = true;
				}
			}
			if(tank2.getFluidAmount()>0)
			{
				int connected=0;
				ForgeDirection f = ForgeDirection.getOrientation(facing);
				TileEntity te = worldObj.getTileEntity(xCoord+f.offsetX*2,yCoord,zCoord+f.offsetZ*2);
				if(te!=null && te instanceof IFluidHandler && ((IFluidHandler)te).canFill(f.getOpposite(), IEContent.fluidBiodiesel))
					connected++;
				te = worldObj.getTileEntity(xCoord+f.getOpposite().offsetX*2,yCoord,zCoord+f.getOpposite().offsetZ*2);
				if(te!=null && te instanceof IFluidHandler && ((IFluidHandler)te).canFill(f, IEContent.fluidBiodiesel))
					connected++;

				if(connected!=0)
				{
					int out = Math.min(144,tank2.getFluidAmount())/connected;
					te = worldObj.getTileEntity(xCoord+f.offsetX*2,yCoord,zCoord+f.offsetZ*2);
					if(te!=null && te instanceof IFluidHandler && ((IFluidHandler)te).canFill(f.getOpposite(), IEContent.fluidBiodiesel))
					{
						int accepted = ((IFluidHandler)te).fill(f.getOpposite(), new FluidStack(IEContent.fluidBiodiesel,out), false);
						FluidStack drained = this.tank2.drain(accepted, true);
						((IFluidHandler)te).fill(f.getOpposite(), drained, true);
					}
					te = worldObj.getTileEntity(xCoord+f.getOpposite().offsetX*2,yCoord,zCoord+f.getOpposite().offsetZ*2);
					if(te!=null && te instanceof IFluidHandler && ((IFluidHandler)te).canFill(f, IEContent.fluidBiodiesel))
					{
						int accepted = ((IFluidHandler)te).fill(f, new FluidStack(IEContent.fluidBiodiesel,out), false);
						FluidStack drained = this.tank2.drain(accepted, true);
						((IFluidHandler)te).fill(f, drained, true);
						update = true;
					}
				}
			}
			if(update)
			{
				this.markDirty();
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		}
	}
	boolean hasTankSpace()
	{
		return false;
	}


	@Override
	public void readCustomNBT(NBTTagCompound nbt)
	{
		super.readCustomNBT(nbt);
		facing = nbt.getInteger("facing");
		tank0.readFromNBT(nbt.getCompoundTag("tank0"));
		tank1.readFromNBT(nbt.getCompoundTag("tank1"));
		tank1.readFromNBT(nbt.getCompoundTag("tank2"));
		energyStorage.readFromNBT(nbt);
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt)
	{
		super.writeCustomNBT(nbt);
		nbt.setInteger("facing", facing);
		NBTTagCompound tankTag = tank0.writeToNBT(new NBTTagCompound());
		nbt.setTag("tank0", tankTag);
		tankTag = tank1.writeToNBT(new NBTTagCompound());
		nbt.setTag("tank1", tankTag);
		tankTag = tank2.writeToNBT(new NBTTagCompound());
		nbt.setTag("tank2", tankTag);
		energyStorage.writeToNBT(nbt);
	}



	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if(!formed)
			return 0;
		if(master()!=null)
		{
			if(pos!=15&&pos!=19)
				return 0;
			return master().fill(from,resource,doFill);
		}
		else if(resource!=null)
		{
			if(from==ForgeDirection.NORTH||from==ForgeDirection.WEST)
				return tank0.fill(resource, doFill);
			else
				return tank1.fill(resource, doFill);
		}
		return 0;
	}
	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if(!formed)
			return null;
		if(master()!=null)
		{
			if(pos!=2&&pos!=32)
				return null;
			return master().drain(from,resource,doDrain);
		}
		else if(resource!=null)
			return drain(from, resource.amount, doDrain);
		return null;
	}
	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		if(!formed)
			return null;
		if(master()!=null)
		{
			if(pos!=2&&pos!=32)
				return null;
			return master().drain(from,maxDrain,doDrain);
		}
		else
			return tank2.drain(maxDrain, doDrain);
	}
	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		if(!formed)
			return false;
		return true;
	}
	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		if(!formed)
			return false;
		return true;
	}
	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		if(!formed)
			return new FluidTankInfo[]{};
		if(master()!=null)
			return master().getTankInfo(from);
		return new FluidTankInfo[]{tank0.getInfo(),tank1.getInfo(),tank2.getInfo()};
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(pos==17)
			return AxisAlignedBB.getBoundingBox(xCoord-(facing==2||facing==3?2:1),yCoord,zCoord-(facing==4||facing==5?2:1), xCoord+(facing==2||facing==3?3:2),yCoord+2,zCoord+(facing==4||facing==5?3:2));
		return AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord,yCoord,zCoord);
	}
	
	@Override
	public void invalidate()
	{
		super.invalidate();

		if(formed && !worldObj.isRemote)
		{
			int f = facing;
			int il = pos/15;
			int ih = (pos%15/5)-1;
			int iw = (pos%5)-2;
			int startX = xCoord-(f==4?il: f==5?-il: f==2?-iw: iw);
			int startY = yCoord-ih;
			int startZ = zCoord-(f==2?il: f==3?-il: f==5?-iw: iw);
			for(int l=0;l<3;l++)
				for(int w=-2;w<=2;w++)
					for(int h=-1;h<=1;h++)
					{
						if(w==0&&(h==1||(h==0&&l==1)))
							continue;
						int xx = (f==4?l: f==5?-l: f==2?-w : w);
						int yy = h;
						int zz = (f==2?l: f==3?-l: f==5?-w : w);
						if((startX+xx!=xCoord) || (startY+yy!=yCoord) || (startZ+zz!=zCoord))
						{
							ItemStack s = null;
							if(worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz) instanceof TileEntityRefinery)
							{
								((TileEntityRefinery)worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz)).formed=false;
								s = ((TileEntityRefinery)worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz)).getOriginalBlock();
							}
							if(s!=null && Block.getBlockFromItem(s.getItem())!=null)
							{
								if(Block.getBlockFromItem(s.getItem())==this.getBlockType() && s.getItemDamage()==this.getBlockMetadata())
									worldObj.setBlockToAir(startX+xx,startY+yy,startZ+zz);
								worldObj.setBlock(startX+xx,startY+yy,startZ+zz, Block.getBlockFromItem(s.getItem()), s.getItemDamage(), 0x3);
							}
						}
					}
		}
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return formed && pos==37 && ForgeDirection.getOrientation(facing).getOpposite().equals(from);
	}
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if(formed && pos==37 && master()!=null && ForgeDirection.getOrientation(facing).getOpposite().equals(from))
		{
			TileEntityRefinery master = master();
			int rec = master.energyStorage.receiveEnergy(maxReceive, simulate);
			master.markDirty();
			worldObj.markBlockForUpdate(master.xCoord, master.yCoord, master.zCoord);
			return rec;
		}
		return 0;
	}
	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		if(this.master()!=null)
			this.master().energyStorage.getEnergyStored();
		return energyStorage.getEnergyStored();
	}
	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		if(this.master()!=null)
			this.master().energyStorage.getMaxEnergyStored();
		return energyStorage.getMaxEnergyStored();
	}
}