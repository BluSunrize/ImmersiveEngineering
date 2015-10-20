package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class TileEntityFluidPipe extends TileEntityIEBase implements IFluidHandler
{
	static ConcurrentHashMap<ChunkCoordinates, ConcurrentSkipListSet<DirectionalFluidOutput>> indirectConnections = new ConcurrentHashMap<ChunkCoordinates, ConcurrentSkipListSet<DirectionalFluidOutput>>();
	public static ArrayList<ItemStack> validScaffoldCoverings = new ArrayList<ItemStack>();
	static{
		TileEntityFluidPipe.validScaffoldCoverings.add(new ItemStack(IEContent.blockMetalDecoration,1,1));
		TileEntityFluidPipe.validScaffoldCoverings.add(new ItemStack(IEContent.blockWoodenDecoration,1,5));
	}
	
	public int[] sideConfig = new int[] {0,0,0,0,0,0};
	public ItemStack scaffoldCovering = null;
	
	@Override
	public boolean canUpdate()
	{
		return false;
	}

	public static ConcurrentSkipListSet<DirectionalFluidOutput> getConnectedFluidHandlers(ChunkCoordinates node, World world)
	{
		if(indirectConnections.containsKey(node))
			return indirectConnections.get(node);

		ArrayList<ChunkCoordinates> openList = new ArrayList();
		ArrayList<ChunkCoordinates> closedList = new ArrayList();
		ConcurrentSkipListSet<DirectionalFluidOutput> fluidHandlers = new ConcurrentSkipListSet();
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
						TileEntity te2 = world.getTileEntity(next.posX+fd.offsetX,next.posY+fd.offsetY,next.posZ+fd.offsetZ);
						if(te2 instanceof TileEntityFluidPipe)
							openList.add(new ChunkCoordinates(next.posX+fd.offsetX,next.posY+fd.offsetY,next.posZ+fd.offsetZ));
						else if(te2 instanceof IFluidHandler)
						{
							IFluidHandler handler = (IFluidHandler)te2;
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
				indirectConnections.get(node).addAll(fluidHandlers);
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
		scaffoldCovering = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("scaffold"));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setIntArray("sideConfig", sideConfig);
		if(scaffoldCovering!=null)
			nbt.setTag("scaffold", (scaffoldCovering.writeToNBT(new NBTTagCompound())));
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

		ArrayList<DirectionalFluidOutput> outputList = new ArrayList(getConnectedFluidHandlers(new ChunkCoordinates(xCoord,yCoord,zCoord), worldObj));
		if(outputList.size()<1)
			return 0;
		ChunkCoordinates ccFrom = new ChunkCoordinates(xCoord+from.offsetX,yCoord+from.offsetY,zCoord+from.offsetZ);
		final int fluidForSort = canAccept;
		int sum = 0;
		HashMap<DirectionalFluidOutput,Integer> sorting = new HashMap<DirectionalFluidOutput,Integer>();
		for(DirectionalFluidOutput output : outputList)
			if(!Utils.toCC(output.output).equals(ccFrom) && output.output.canFill(output.direction, resource.getFluid()))
			{
				int temp = output.output.fill(output.direction.getOpposite(), Utils.copyFluidStackWithAmount(resource, fluidForSort,true), false);
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
				int r = output.output.fill(output.direction.getOpposite(), Utils.copyFluidStackWithAmount(resource, amount, true), doFill);
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
		worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 0,0);
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
}