package blusunrize.immersiveengineering.common.blocks;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import blusunrize.immersiveengineering.api.WireType;
import blusunrize.immersiveengineering.api.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.TargetingInfo;
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
	public void removeCable(WireType type)
	{
		if(ImmersiveNetHandler.getConnections(worldObj,Utils.toCC(this)).isEmpty())
		{
			if(type==limitType || type==null)
				this.limitType = null;
			this.markDirty();
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		this.writeToNBT(nbttagcompound);
		if(worldObj!=null && !worldObj.isRemote)
		{
			//			System.out.println("SERVER sending descinfo for "+this.xCoord+", "+this.yCoord+", "+this.zCoord);
			NBTTagList connectionList = new NBTTagList();
			List<Connection> conL = ImmersiveNetHandler.getConnections(worldObj, Utils.toCC(this));
			//			System.out.println("SERVER sending descinfo for "+this.xCoord+", "+this.yCoord+", "+this.zCoord+" L:"+conL.size());
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
		if(worldObj!=null && worldObj.isRemote)
		{
			NBTTagList connectionList = nbt.getTagList("connectionList", 10);
			//			System.out.println("CLIENT reading connections, "+connectionList.tagCount());
			ImmersiveNetHandler.clearConnectionsOriginatingFrom(Utils.toCC(this), worldObj);
			for(int i=0; i<connectionList.tagCount(); i++)
			{
				NBTTagCompound conTag = connectionList.getCompoundTagAt(i);
				Connection con = Connection.readFromNBT(conTag);
				if(con!=null)
				{
					ImmersiveNetHandler.addConnection(worldObj, Utils.toCC(this), con);
				}
				else
					System.out.println("CLIENT read connection as null");
			}
		}
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt)
	{
		try{

			if(nbt.hasKey("limitType"))
				limitType = WireType.getValue(nbt.getInteger("limitType"));
		}catch(Exception e)
		{
			System.out.println("MASSIVE ERROR. DOES NOT COMPUTE WRITE.");
		}
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt)
	{
		try{

			if(limitType!=null)
				nbt.setInteger("limitType", limitType.ordinal());
		}catch(Exception e)
		{
			System.out.println("MASSIVE ERROR. DOES NOT COMPUTE WRITE.");
		}
	}
}