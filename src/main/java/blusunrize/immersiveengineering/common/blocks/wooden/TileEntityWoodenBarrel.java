package blusunrize.immersiveengineering.common.blocks.wooden;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;

public class TileEntityWoodenBarrel extends TileEntityIEBase implements IFluidHandler
{
	int[] sideMap = {-1,1};
	FluidTank tank = new FluidTank(8000);

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		sideMap = nbt.getIntArray("sideMap");
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setIntArray("sideMap", sideMap);
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		return tank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return this.drain(from, resource!=null?resource.amount:0, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return tank.drain(maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return from.ordinal()<2 && sideMap[from.ordinal()]==0;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return from.ordinal()<2 && sideMap[from.ordinal()]==1;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return new FluidTankInfo[]{tank.getInfo()};
	}

}
