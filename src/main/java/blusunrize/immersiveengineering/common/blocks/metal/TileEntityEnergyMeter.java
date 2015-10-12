package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.WireType;
import blusunrize.immersiveengineering.common.blocks.TileEntityImmersiveConnectable;

public class TileEntityEnergyMeter extends TileEntityImmersiveConnectable
{
	public int sideAttached=0;
	public int facing=2;
	public int wires = 0;
	public int energyPassed = 0;

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
	public boolean canUpdate()
	{
		return false;
	}

//	@Override
//	public void onEnergyPassthrough(int amount, boolean simulate, int energyType)
//	{
//		if(!simulate)
//			energyPassed +=amount;
//	}

	@Override
	public boolean canConnectCable(WireType cableType, TargetingInfo target)
	{
		if(cableType!=null && !cableType.isEnergyWire())
			return false;
		if(wires>=2)
			return false;
		return limitType==null || cableType==limitType;
	}
	@Override
	public void connectCable(WireType cableType, TargetingInfo target)
	{
		if(this.limitType==null)
			this.limitType = cableType;
		wires++;
	}
	@Override
	public WireType getCableLimiter(TargetingInfo target)
	{
		return limitType;
	}
	@Override
	public void removeCable(Connection connection)
	{
		WireType type = connection!=null?connection.cableType:null;
		if(type==null)
			wires = 0;
		else
			wires--;
		if(wires<=0)
			limitType=null;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing);
		nbt.setInteger("sideAttached", sideAttached);
		nbt.setInteger("wires", wires);
		nbt.setInteger("energyPassed", energyPassed);
	}
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = nbt.getInteger("facing");
		sideAttached = nbt.getInteger("sideAttached");
		wires = nbt.getInteger("wires");
		energyPassed = nbt.getInteger("energyPassed");
	}

	@Override
	public Vec3 getRaytraceOffset(IImmersiveConnectable link)
	{
		return Vec3.createVectorHelper(.5,1.3125,.5);
	}
	@Override
	public Vec3 getConnectionOffset(Connection con)
	{
		return Vec3.createVectorHelper(facing==5?.25:facing==4?.75:.5, 1.3125, facing==3?.25:facing==2?.75:.5);
	}
}