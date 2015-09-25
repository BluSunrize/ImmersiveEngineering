package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.HashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPipe.DirectionalFluidOutput;

public class TileEntityFluidPump extends TileEntityIEBase //implements IFluidPipe
{
	public int[] sideConfig = new int[] {0,-1,0,0,0,0};
	public boolean dummy = true;
	//	public PipeController pipeController = new PipeController();

	@Override
	public void updateEntity()
	{
		for(int i=0; i<6; i++)
			if(sideConfig[i]==1)
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
			if(sideConfig[i]==2)
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
			sideConfig = new int[]{0,-1,0,0,0,0};
		dummy = nbt.getBoolean("dummy");
		if(descPacket)
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setIntArray("sideConfig", sideConfig);
		nbt.setBoolean("dummy", dummy);
	}

	public void toggleSide(int side)
	{
		if (side != 1)
		{
			sideConfig[side]++;
			if (sideConfig[side] > 2)
				sideConfig[side] = 0;
		}
	}


	//	////
	//	// IFluidPipe
	//	////
	//
	//	@Override
	//	public void addConnection(PipeConnection connection) {
	//		pipeController.addConnection(connection);
	//	}
}
