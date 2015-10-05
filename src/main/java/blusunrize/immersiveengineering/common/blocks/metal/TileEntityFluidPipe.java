package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Utils;

import com.google.common.collect.ArrayListMultimap;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class TileEntityFluidPipe extends TileEntityIEBase implements IFluidHandler
{
	static ArrayListMultimap<ChunkCoordinates, DirectionalFluidOutput> indirectConnections = ArrayListMultimap.create();
	public int[] sideConfig = new int[] {0,0,0,0,0,0};

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	public static List<DirectionalFluidOutput> getConnectedFluidHandlers(ChunkCoordinates node, World world)
	{
		if(indirectConnections.containsKey(node))
			return indirectConnections.get(node);

		ArrayList<ChunkCoordinates> openList = new ArrayList();
		ArrayList<ChunkCoordinates> closedList = new ArrayList();
		ArrayList<DirectionalFluidOutput> fluidHandlers = new ArrayList();
		openList.add(node);
		while(!openList.isEmpty() && closedList.size()<1024)
		{
			ChunkCoordinates next = openList.get(0);
			TileEntity te = world.getTileEntity(next.posX,next.posY,next.posZ);
			if(!closedList.contains(next) && (te instanceof TileEntityFluidPipe || te instanceof TileEntityFluidPump))
			{
				if(te instanceof TileEntityFluidPipe)
					closedList.add(next);
				for(int i=0; i<6; i++)
				{
					boolean b = (te instanceof TileEntityFluidPipe)? (((TileEntityFluidPipe) te).sideConfig[i]==0): (((TileEntityFluidPump) te).sideConfig[i]==1);
					if(b)
					{
						ForgeDirection fd = ForgeDirection.getOrientation(i);
						if(te instanceof TileEntityFluidPipe)
							openList.add(new ChunkCoordinates(next.posX+fd.offsetX,next.posY+fd.offsetY,next.posZ+fd.offsetZ));
						else if(te instanceof IFluidHandler)
						{
							IFluidHandler handler = (IFluidHandler)te;
							fluidHandlers.add(new DirectionalFluidOutput(handler, fd));
						}
					}
				}
			}
			openList.remove(0);
		}
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			if(!indirectConnections.containsKey(node))
				indirectConnections.putAll(node, fluidHandlers);
		}
		return fluidHandlers;
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		indirectConnections.clear();
	}


	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		sideConfig = nbt.getIntArray("sideConfig");
		if(sideConfig==null || sideConfig.length!=6)
			sideConfig = new int[]{0,0,0,0,0,0};
		//		tank.readFromNBT(nbt.getCompoundTag("tank"));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setIntArray("sideConfig", sideConfig);
		//		NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
		//		nbt.setTag("tank", tankTag);
	}


	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if(resource==null || from==null || from==ForgeDirection.UNKNOWN || sideConfig[from.ordinal()]!=0)
			return 0;
		int limit = resource.tag!=null&&resource.tag.hasKey("pressurized")?1000: 50;
		int canAccept = Math.min(resource.amount, limit);
		if(canAccept<=0)
			return 0;
		FluidStack insertResource = Utils.copyFluidStackWithAmount(resource, canAccept);

		List<DirectionalFluidOutput> outputList = getConnectedFluidHandlers(new ChunkCoordinates(xCoord,yCoord,zCoord), worldObj);
		if(outputList.size()<1)
			return 0;
		ChunkCoordinates ccFrom = new ChunkCoordinates(xCoord+from.offsetX,yCoord+from.offsetY,zCoord+from.offsetZ);
		final int fluidForSort = canAccept;
		int sum = 0;
		HashMap<DirectionalFluidOutput,Integer> sorting = new HashMap<DirectionalFluidOutput,Integer>();
		for(DirectionalFluidOutput output : outputList)
			if(!Utils.toCC(output.output).equals(ccFrom) && output.output.canFill(output.direction, insertResource.getFluid()))
			{
				int temp = output.output.fill(output.direction.getOpposite(), insertResource, false);
				if(temp>0)
				{
					sorting.put(output, temp);
					sum += temp;
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
				int r = output.output.fill(output.direction.getOpposite(), Utils.copyFluidStackWithAmount(resource, amount), doFill);
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
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return from!=null&&from!=ForgeDirection.UNKNOWN&&sideConfig[from.ordinal()]==0;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return new FluidTankInfo[0];
	}

	public static class DirectionalFluidOutput
	{
		IFluidHandler output;
		ForgeDirection direction;

		public DirectionalFluidOutput(IFluidHandler output, ForgeDirection direction)
		{
			this.output = output;
			this.direction = direction;
		}
	}

	public byte getConnectionByte()
	{
		byte connections = 0;
		for(int i=5; i>=0; i--)
		{
			TileEntity con = worldObj.getTileEntity(xCoord+(i==4?-1: i==5?1: 0),yCoord+(i==0?-1: i==1?1: 0),zCoord+(i==2?-1: i==3?1: 0));
			connections <<= 1;
			if(sideConfig[i]==0 && con instanceof IFluidHandler)
				connections |= 1;
		}
		return connections;
	}
	public byte getAvailableConnectionByte()
	{
		byte connections = 0;
		for(int i=5; i>=0; i--)
		{
			TileEntity con = worldObj.getTileEntity(xCoord+(i==4?-1: i==5?1: 0),yCoord+(i==0?-1: i==1?1: 0),zCoord+(i==2?-1: i==3?1: 0));
			connections <<= 1;
			if(con instanceof IFluidHandler)
				connections |= 1;
		}
		return connections;
	}
	public int getConnectionStyle(int connection)
	{
		if(sideConfig[connection]==-1)
			return 0;
		byte thisConnections = getConnectionByte();
		if((thisConnections&(1<<connection))==0)
			return 0;

		if(thisConnections!=3&&thisConnections!=12&&thisConnections!=48)
			return 1;
		TileEntity con = worldObj.getTileEntity(xCoord+(connection==4?-1: connection==5?1: 0),yCoord+(connection==0?-1: connection==1?1: 0),zCoord+(connection==2?-1: connection==3?1: 0));
		if(con instanceof TileEntityFluidPipe)
		{
			byte tileConnections = ((TileEntityFluidPipe)con).getConnectionByte();
			if(thisConnections==tileConnections)
				return 0;
		}
		return 1;
	}

	public void toggleSide(int side)
	{
		sideConfig[side]++;
		if(sideConfig[side]>0)
			sideConfig[side] = -1;
	}
}