package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import blusunrize.immersiveengineering.api.DirectionalChunkCoords;
import blusunrize.immersiveengineering.api.fluid.PipeConnection;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Utils;

import com.google.common.collect.ImmutableSortedMap;

public class TileEntityFluidPipe_old extends TileEntityIEBase implements IEBlockInterfaces.IBlockOverlayText
{
	public static int MODE_NORMAL = 0;
	public static int MODE_INPUT = 1;
	public static int MODE_OUTPUT = 2;

	public HashSet<PipeConnection> connections = new HashSet<PipeConnection>();
	public int mode = 0;


	protected int cooldown = 5;

	@Override
	public void updateEntity()
	{
		if(hasWorldObj() && !isInvalid() && !worldObj.isRemote)
		{
			if (mode == MODE_INPUT)
			{
				if(cooldown-- == 0)
				{
					cooldown = 5;
					HashMap<DirectionalChunkCoords, Integer> tanks = null;
					ImmutableSortedMap<DirectionalChunkCoords, Integer> sortedTanks = null;

					for(PipeConnection connection : connections)
					{
						TileEntity tankTile = connection.getEndTile(worldObj);
						if(PipeConnection.isTank(tankTile, connection.direction.getOpposite()))
						{
							if(tanks==null)
							{
								tanks = getAllTanks();
								sortedTanks = ImmutableSortedMap.copyOf(tanks, new DistanceComparator(tanks));
							}
							FluidStack insert = ((IFluidHandler) tankTile).drain(connection.direction.getOpposite(), 500, false);

							if(insert == null)
								break;
							int initAmount = insert.amount;
							insert = insert.copy();

							for(DirectionalChunkCoords tankCoords : sortedTanks.keySet())
								if(tankCoords.posX!=xCoord || tankCoords.posY!=yCoord || tankCoords.posZ!=zCoord)
								{
									TileEntity tileEntity = tankCoords.getTile(worldObj);
									if(tileEntity instanceof IFluidHandler)
									{
										insert.amount -= ((IFluidHandler) tileEntity).fill(tankCoords.direction, insert, true);
										if(insert.amount==0)
											break;
									}
								}

							((IFluidHandler) tankTile).drain(connection.direction.getOpposite(), initAmount - insert.amount, true);
						}
					}
				}
			}
		}
	}

	public HashMap<DirectionalChunkCoords, Integer> getAllTanks()
	{
		HashSet<String> traversed = new HashSet<String>();
		HashMap<DirectionalChunkCoords, Integer> tanks = new HashMap<DirectionalChunkCoords, Integer>();
		traversed.add(xCoord + ":" + yCoord + ":" + zCoord);
		getAllTanks(traversed, tanks, 1);
		traversed.clear();
		return tanks;
	}

	public void getAllTanks(HashSet<String> traversed, HashMap<DirectionalChunkCoords, Integer> tanksMap, int depth)
	{
		for(PipeConnection connection : connections)
		{
			String strCon = connection.to.posX + ":" + connection.to.posY + ":" + connection.to.posZ;
			if(!traversed.contains(strCon))
			{
				traversed.add(strCon);
				TileEntity tileEntity = worldObj.getTileEntity(connection.to.posX, connection.to.posY, connection.to.posZ);
				if(tileEntity instanceof TileEntityFluidPipe_old)
					((TileEntityFluidPipe_old) tileEntity).getAllTanks(traversed, tanksMap, depth + 1);
				else if(this.mode == MODE_OUTPUT && PipeConnection.isTank(tileEntity, connection.direction.getOpposite()))
					tanksMap.put(Utils.toDirCC(tileEntity, connection.direction.getOpposite()), depth);
			}
		}
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		mode = nbt.getInteger("PipeMode");
		NBTTagList connectionTags = nbt.getTagList("PipeConnections", 10);
		HashSet<PipeConnection> newConnections = new HashSet<PipeConnection>();
		for(int i = 0; i < connectionTags.tagCount(); i++)
			newConnections.add(PipeConnection.fromNBT(connectionTags.getCompoundTagAt(i)));
		this.connections = newConnections;

		if(descPacket)
			worldObj.markBlockRangeForRenderUpdate(xCoord - 1, yCoord - 1, zCoord - 1, xCoord + 1, yCoord + 1, zCoord + 1);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("PipeMode", mode);
		NBTTagList connectionTags = new NBTTagList();
		for(PipeConnection connection : connections)
			connectionTags.appendTag(connection.toNBT());
		nbt.setTag("PipeConnections", connectionTags);
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		for(PipeConnection connection : connections)
		{
			TileEntity connectedTE = worldObj.getTileEntity(connection.to.posX, connection.to.posY, connection.to.posZ);
			if(connectedTE instanceof TileEntityFluidPipe_old)
			{
				TileEntityFluidPipe_old fluidPipe = (TileEntityFluidPipe_old) connectedTE;
				HashSet<PipeConnection> connectionsToRemove = new HashSet<PipeConnection>();
				for(PipeConnection con : fluidPipe.connections)
					if(con.to.equals(connection.from))
						connectionsToRemove.add(con);
				for(PipeConnection remove : connectionsToRemove)
					fluidPipe.connections.remove(remove);
				fluidPipe.markDirty();
				worldObj.markBlockForUpdate(connection.to.posX, connection.to.posY, connection.to.posZ);
			}
		}
	}

	@Override
	public String[] getOverlayText(EntityPlayer player, MovingObjectPosition mop, boolean hammer)
	{
		return new String[]{
				"Mode: " + (mode == 0 ? "Normal" : (mode == 1 ? "Input" : "Output")),
				"Connections: " + connections.size()
		};
	}

	public byte getConnections()
	{
		byte connections = 0;
		for(PipeConnection connection : this.connections)
			if(connection.direction != ForgeDirection.UNKNOWN)
				connections = (byte) (connections | 1 << connection.direction.ordinal());
		return connections;
	}

	private class DistanceComparator implements Comparator<DirectionalChunkCoords>
	{
		protected Map<DirectionalChunkCoords, Integer> base;
		public DistanceComparator(Map<DirectionalChunkCoords, Integer> base)
		{
			this.base = base;
		}

		@Override
		public int compare(DirectionalChunkCoords dirA, DirectionalChunkCoords dirB)
		{
			if (base.get(dirA) < base.get(dirB))
				return -1;
			return 1;
		}
	}
}