package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;
import java.util.Set;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.WireType;
import blusunrize.immersiveengineering.common.blocks.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;

public class TileEntityEnergyMeter extends TileEntityImmersiveConnectable
{
	public int facing=3;
	public int lastEnergyPassed = 0;
	public ArrayList<Integer> lastPackets = new ArrayList<Integer>(25);
	public boolean dummy=false;
	private int maxTransfer = -1;
	public int compVal = -1;

	@Override
	protected boolean canTakeLV()
	{
		return true;
	}
	@Override
	protected boolean canTakeMV()
	{
		return true;
	}
	@Override
	protected boolean canTakeHV()
	{
		return true;
	}

	@Override
	public void updateEntity()
	{
		if(dummy || worldObj.isRemote)
			return;
		synchronized (lastPackets) {
			//Yes, this might tick in between different connectors sending power, but since this is a block for statistical evaluation over a tick, that is irrelevant.
			lastPackets.add(lastEnergyPassed);
			if (lastPackets.size() > 20)
				lastPackets.remove(0);

			if ((worldObj.getTotalWorldTime()&31)==((xCoord^zCoord)&31)||compVal<0)
				updateComparatorValues();
		}
		lastEnergyPassed = 0;
	}

	private void updateComparatorValues()
	{
		if (maxTransfer<0)
		{
			Set<Connection> conns = ImmersiveNetHandler.INSTANCE.getConnections(worldObj, Utils.toCC(this));
			if (conns==null)
				return;
			maxTransfer = 0;
			for (Connection c:conns)
				maxTransfer+=c.cableType.getTransferRate();
		}
		float percentage = (getAveragePower()*2)/(float) maxTransfer;
		int oldComp = compVal;
		compVal = (int)Math.min(Math.ceil(15*percentage), 15);
		if (oldComp!=compVal)
		{
			TileEntity te = worldObj.getTileEntity(xCoord, yCoord-1, zCoord);
			worldObj.func_147453_f(xCoord, yCoord, zCoord, getBlockType());
			if (!(te instanceof TileEntityEnergyMeter))
				return;
			((TileEntityEnergyMeter)te).compVal = compVal;
			worldObj.func_147453_f(xCoord, yCoord-1, zCoord, getBlockType());
		}
	}
	@Override
	public boolean canConnect()
	{
		return !dummy;
	}

	@Override
	public void onEnergyPassthrough(int amount)
	{
		lastEnergyPassed += amount;
	}

	@Override
	public boolean canConnectCable(WireType cableType, TargetingInfo target)
	{
		if(dummy)
		{
			TileEntity above = worldObj.getTileEntity(xCoord, yCoord+1, zCoord);
			if(above instanceof TileEntityEnergyMeter)
				return ((TileEntityEnergyMeter)above).canConnectCable(cableType, target);
			return false;
		}
		return super.canConnectCable(cableType, target);
	}
	@Override
	public void connectCable(WireType cableType, TargetingInfo target)
	{
		if(dummy)
		{
			TileEntity above = worldObj.getTileEntity(xCoord, yCoord+1, zCoord);
			if(above instanceof TileEntityEnergyMeter)
				((TileEntityEnergyMeter)above).connectCable(cableType, target);
		}
		else
		{
			super.connectCable(cableType, target);
			if (!worldObj.isRemote)
				maxTransfer+=cableType.getTransferRate();
		}
	}
	@Override
	public void removeCable(Connection connection) {
		super.removeCable(connection);
		if (!worldObj.isRemote)
		{
			if (connection==null)
				maxTransfer = 0;
			else
				maxTransfer-=connection.cableType.getTransferRate();
		}
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing);
		nbt.setBoolean("dummy", dummy);
	}
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = nbt.getInteger("facing");
		dummy = nbt.getBoolean("dummy");
	}

	@Override
	public Vec3 getRaytraceOffset(IImmersiveConnectable link)
	{
		int xDif = ((TileEntity)link).xCoord-xCoord;
		int zDif = ((TileEntity)link).zCoord-zCoord;
		if(facing>3)
			return Vec3.createVectorHelper(.5,.4375,zDif>0?.8125:.1875);
		else
			return Vec3.createVectorHelper(xDif>0?.8125:.1875,.4375,.5);
	}
	@Override
	public Vec3 getConnectionOffset(Connection con)
	{
		int xDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.posX-xCoord: (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.posX-xCoord: 0;
		int zDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.posZ-zCoord: (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.posZ-zCoord: 0;
		if(facing>3)
			return Vec3.createVectorHelper(.5,.4375,zDif>0?.8125:.1875);
		else
			return Vec3.createVectorHelper(xDif>0?.8125:.1875,.4375,.5);
	}

	public int getAveragePower()
	{
		TileEntityEnergyMeter te = this;
		if (te.dummy)
		{
			TileEntity tmp = worldObj.getTileEntity(xCoord, yCoord+1, zCoord);
			if (!(tmp instanceof TileEntityEnergyMeter))
				return -1;
			te = (TileEntityEnergyMeter) tmp;
		}
		if (te.lastPackets.size()==0)
			return 0;
		int sum = 0;
		synchronized (te.lastPackets)
		{
			for (int transfer: te.lastPackets)
				sum += transfer;
		}
		return sum/te.lastPackets.size();
	}

}