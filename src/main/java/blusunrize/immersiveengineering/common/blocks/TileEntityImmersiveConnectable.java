package blusunrize.immersiveengineering.common.blocks;

import java.util.concurrent.ConcurrentSkipListSet;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.WireType;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.Utils;

public abstract class TileEntityImmersiveConnectable extends TileEntityIEBase implements IImmersiveConnectable
{
	protected WireType limitType = null;

	protected boolean canTakeLV()
	{
		return false;
	}
	protected boolean canTakeMV()
	{
		return false;
	}
	protected boolean canTakeHV()
	{
		return false;
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		if(worldObj!=null && !worldObj.isRemote)
			ImmersiveNetHandler.INSTANCE.clearAllConnectionsFor(Utils.toCC(this),worldObj);
	}

	@Override
	public boolean allowEnergyToPass(Connection con)
	{
		return true;
	}

	@Override
	public boolean canConnect()
	{
		return true;
	}
	@Override
	public boolean isEnergyOutput()
	{
		return false;
	}
	@Override
	public int outputEnergy(int amount, boolean simulate, int energyType)
	{
		return 0;
	}
	@Override
	public boolean canConnectCable(WireType cableType, TargetingInfo target)
	{
		if(cableType==WireType.STEEL&&!canTakeHV())
			return false;
		if(cableType==WireType.ELECTRUM&&!canTakeMV())
			return false;
		if(cableType==WireType.COPPER&&!canTakeLV())
			return false;
		if(cableType==WireType.STRUCTURE_ROPE)
			return false;
		if(cableType==WireType.STRUCTURE_STEEL)
			return false;
		return limitType==null||limitType==cableType;
	}
	@Override
	public void connectCable(WireType cableType, TargetingInfo target)
	{
		this.limitType = cableType;
	}
	@Override
	public WireType getCableLimiter(TargetingInfo target)
	{
		return this.limitType;
	}
	@Override
	public void removeCable(Connection connection)
	{
		WireType type = connection!=null?connection.cableType:null;
		ConcurrentSkipListSet<Connection> outputs = ImmersiveNetHandler.INSTANCE.getConnections(worldObj,Utils.toCC(this));
		if (outputs==null||outputs.size()==0)
		{
			if(type==limitType || type==null)
				this.limitType = null;
		}
		this.markDirty();
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		this.writeToNBT(nbttagcompound);
		if(worldObj!=null && !worldObj.isRemote)
		{
			NBTTagList connectionList = new NBTTagList();
			ConcurrentSkipListSet<Connection> conL = ImmersiveNetHandler.INSTANCE.getConnections(worldObj, Utils.toCC(this));
			if(conL!=null)
				for(Connection con : conL)
					connectionList.appendTag(con.writeToNBT());
			nbttagcompound.setTag("connectionList", connectionList);
		}
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 3, nbttagcompound);
	}
	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
	{
		NBTTagCompound nbt = pkt.func_148857_g();
		this.readFromNBT(nbt);
		if(worldObj!=null && worldObj.isRemote && !Minecraft.getMinecraft().isSingleplayer())
		{
			NBTTagList connectionList = nbt.getTagList("connectionList", 10);
			ImmersiveNetHandler.INSTANCE.clearConnectionsOriginatingFrom(Utils.toCC(this), worldObj);
			for(int i=0; i<connectionList.tagCount(); i++)
			{
				NBTTagCompound conTag = connectionList.getCompoundTagAt(i);
				Connection con = Connection.readFromNBT(conTag);
				if(con!=null)
				{
					ImmersiveNetHandler.INSTANCE.addConnection(worldObj, Utils.toCC(this), con);
				}
				else
					IELogger.error("CLIENT read connection as null");
			}
		}
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==-1)
		{
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			return true;
		}
		return false;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		try{
			if(nbt.hasKey("limitType"))
				limitType = ApiUtils.getWireTypeFromNBT(nbt, "limitType");

			//			int[] prevPos = nbt.getIntArray("prevPos");
			//			if(prevPos!=null && prevPos.length>3 && FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
			//			{
			//				//				if(worldObj.provider.dimensionId!=prevPos[0])
			//				//				{
			//				//					ImmersiveNetHandler.clearAllConnectionsFor(Utils.toCC(this),worldObj);
			//				//				}
			//				//				else
			//				World worldTest = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(prevPos[0]);
			//				if(xCoord!=prevPos[1] || yCoord!=prevPos[2] || zCoord!=prevPos[3])
			//				{
			//					IELogger.info("Tile was moved! Attmpting to update connections...");
			//					
			//					if(worldTest.getTileEntity(prevPos[1],prevPos[2],prevPos[3]) instanceof IImmersiveConnectable)
			//					{
			//						IELogger.info("Someone else has taken my place");
			//					}
			//					
			//					Iterator<Connection> it = ImmersiveNetHandler.getAllConnections(worldTest).iterator();
			//					ChunkCoordinates node = new ChunkCoordinates(prevPos[1],prevPos[2],prevPos[3]);
			//					while(it.hasNext())
			//					{
			//						Connection con = it.next();
			//						if(node.equals(con.start))
			//							con.start = Utils.toCC(this);
			//						if(node.equals(con.end))
			//							con.end = Utils.toCC(this);
			//						//						if(node.equals(con.start) || node.equals(con.end))
			//						//						{
			//						////							it.remove();
			//						//							//if(node.equals(con.start) && toIIC(con.end, world)!=null && getConnections(world,con.end).isEmpty())
			//						////							iic = toIIC(con.end, worldObj);
			//						////							if(iic!=null)
			//						////								iic.removeCable(con.cableType);
			//						//							//if(node.equals(con.end) && toIIC(con.start, world)!=null && getConnections(world,con.start).isEmpty())
			//						////							iic = toIIC(con.start, worldObj);
			//						////							if(iic!=null)
			//						////								iic.removeCable(con.cableType);
			//						//
			//						//							if(node.equals(con.end))
			//						//							{
			//						//								double dx = node.posX+.5+Math.signum(con.start.posX-con.end.posX);
			//						//								double dy = node.posY+.5+Math.signum(con.start.posY-con.end.posY);
			//						//								double dz = node.posZ+.5+Math.signum(con.start.posZ-con.end.posZ);
			//						//								worldObj.spawnEntityInWorld(new EntityItem(worldObj, dx,dy,dz, new ItemStack(IEContent.itemWireCoil,1,con.cableType.ordinal())));
			//						//							}
			//						//						}
			//					}
			//					IESaveData.setDirty(worldTest.provider.dimensionId);
			//					//					ImmersiveNetHandler.indirectConnections.clear();
			//				}
			//			}
		}catch(Exception e)
		{
			IELogger.error("TileEntityImmersiveConenctable encountered MASSIVE error reading NBT. You shoudl probably report this.");
		}
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		try{
			if(limitType!=null)
				nbt.setString("limitType", limitType.getUniqueName());

			if(this.worldObj!=null)
			{
				nbt.setIntArray("prevPos", new int[]{this.worldObj.provider.dimensionId,xCoord,yCoord,zCoord});
			}
		}catch(Exception e)
		{
			IELogger.error("TileEntityImmersiveConenctable encountered MASSIVE error writing NBT. You shoudl probably report this.");
		}
	}
}