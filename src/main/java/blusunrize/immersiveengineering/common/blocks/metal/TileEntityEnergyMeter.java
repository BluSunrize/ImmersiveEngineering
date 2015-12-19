package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.WireType;
import blusunrize.immersiveengineering.common.blocks.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.common.Optional;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SidedComponent;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

@Optional.InterfaceList({
		@Optional.Interface(iface = "li.cil.oc.api.network.SidedComponent", modid = "OpenComputers"),
		@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")})
public class TileEntityEnergyMeter extends TileEntityImmersiveConnectable implements SidedComponent, SimpleComponent
{
	public int facing=3;
	public int lastEnergyPassed = 0;
	public ArrayList<Integer> lastPackets = new ArrayList<Integer>(25);
	public boolean dummy=false;

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
		//Yes, this might tick in between different connectors sending power, but since this is a block for statistical evaluation over a tick, that is irrelevant.
		lastPackets.add(lastEnergyPassed);
		if(lastPackets.size()>20)
			lastPackets.remove(0);
		lastEnergyPassed = 0;
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
			super.connectCable(cableType, target);
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

	@Override
	public boolean canConnectNode(ForgeDirection side)
	{
		return dummy;
	}

	@Override
	public String getComponentName()
	{
		return "energy_meter";
	}

	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] getLastMeasurements(Context context, Arguments args)
	{
		TileEntity master = worldObj.getTileEntity(xCoord, yCoord+1, zCoord);
		if(master instanceof TileEntityEnergyMeter)
		{
			int sum = 0;
			for(int transfer: ((TileEntityEnergyMeter)master).lastPackets)
				sum += transfer;
			return new Object[]{sum/((TileEntityEnergyMeter) master).lastPackets.size()};
		}
		return null;
	}
}