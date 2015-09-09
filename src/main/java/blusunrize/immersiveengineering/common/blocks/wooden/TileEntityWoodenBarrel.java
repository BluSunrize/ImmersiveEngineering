package blusunrize.immersiveengineering.common.blocks.wooden;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;

public class TileEntityWoodenBarrel extends TileEntityIEBase implements IFluidHandler, IBlockOverlayText
{
	int[] sideConfig = {-1,0};
	FluidTank tank = new FluidTank(12000);
	public static final int IGNITION_TEMPERATURE = 573;

	public void toggleSide(int side)
	{
		sideConfig[side]++;
		if(sideConfig[side]>1)
			sideConfig[side]=-1;
		worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 0, 0);
	}
	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==0)
		{
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			return true;
		}
		return false;
	}

	@Override
	public void updateEntity()
	{
		if(worldObj.isRemote)
			return;

		boolean update = false;
		for(int i=0; i<2; i++)
			if(tank.getFluidAmount()>0 && sideConfig[i]==1)
			{
				ForgeDirection f = ForgeDirection.getOrientation(i);
				int out = Math.min(40,tank.getFluidAmount());
				TileEntity te = worldObj.getTileEntity(xCoord,yCoord+f.offsetY,zCoord);
				if(te!=null && te instanceof IFluidHandler && ((IFluidHandler)te).canFill(f.getOpposite(), tank.getFluid().getFluid()))
				{
					int accepted = ((IFluidHandler)te).fill(f.getOpposite(), new FluidStack(tank.getFluid().getFluid(),out), false);
					FluidStack drained = this.tank.drain(accepted, true);
					((IFluidHandler)te).fill(f.getOpposite(), drained, true);
					update = true;
				}
			}
		if(update)
		{
			this.markDirty();
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	@Override
	public String[] getOverlayText(EntityPlayer player, MovingObjectPosition mop, boolean hammer)
	{
		if(Utils.isFluidRelatedItemStack(player.getCurrentEquippedItem()))
		{
			String s = null;
			if(tank.getFluid()!=null)
				s = tank.getFluid().getLocalizedName()+": "+tank.getFluidAmount()+"mB";
			else
				s = StatCollector.translateToLocal(Lib.GUI+"empty");
			return new String[]{s};
		}
		if(hammer && Config.getBoolean("colourblindSupport") && mop.sideHit<2)
		{
			int i = sideConfig[Math.min(sideConfig.length-1, mop.sideHit)];
			int j = sideConfig[Math.min(sideConfig.length-1, ForgeDirection.OPPOSITES[mop.sideHit])];
			return new String[]{
					StatCollector.translateToLocal(Lib.DESC_INFO+"blockSide.facing")
					+": "+StatCollector.translateToLocal(Lib.DESC_INFO+"blockSide.connectFluid."+i),
					StatCollector.translateToLocal(Lib.DESC_INFO+"blockSide.opposite")
					+": "+StatCollector.translateToLocal(Lib.DESC_INFO+"blockSide.connectFluid."+j)
			};
		}
		return null;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		sideConfig = nbt.getIntArray("sideConfig");
		if(sideConfig==null || sideConfig.length<2)
			sideConfig = new int[]{-1,0};
		this.readTank(nbt);
	}
	public void readTank(NBTTagCompound nbt)
	{
		tank.readFromNBT(nbt.getCompoundTag("tank"));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setIntArray("sideConfig", sideConfig);
		this.writeTank(nbt, false);
	}
	public void writeTank(NBTTagCompound nbt, boolean toItem)
	{
		boolean write = tank.getFluidAmount()>0;
		NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
		if(!toItem || write)
			nbt.setTag("tank", tankTag);
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if(canFill(from, resource!=null?resource.getFluid():null))
		{
			int i = tank.fill(resource, doFill);
			if(i>0)
			{
				this.markDirty();
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
			return i;
		}
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return this.drain(from, resource!=null?resource.amount:0, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		if(canDrain(from,null))
		{
			FluidStack f = tank.drain(maxDrain, doDrain);
			if(f!=null && f.amount>0)
			{
				this.markDirty();
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
			return f;
		}
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return (from==ForgeDirection.UNKNOWN || (from.ordinal()<2 && sideConfig[from.ordinal()]==0) && (fluid==null||fluid.getTemperature()<IGNITION_TEMPERATURE));
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return (from==ForgeDirection.UNKNOWN || (from.ordinal()<2 && sideConfig[from.ordinal()]==1));
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return new FluidTankInfo[]{tank.getInfo()};
	}
}