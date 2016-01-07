package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralWorldInfo;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Lib;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SidedComponent;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

@Optional.InterfaceList({
		@Optional.Interface(iface = "li.cil.oc.api.network.SidedComponent", modid = "OpenComputers"),
		@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")})
public class TileEntitySampleDrill extends TileEntityIEBase implements IEnergyReceiver, SidedComponent, SimpleComponent
{
	public EnergyStorage energyStorage = new EnergyStorage(8000);
	public int pos=0;
	public int process=0;

	public static boolean _Immovable()
	{
		return true;
	}

	@Override
	public void updateEntity()
	{
		if(pos!=0 || worldObj.isRemote || worldObj.isAirBlock(xCoord,yCoord-1,zCoord))
			return;
		if(process<Config.getInt("coredrill_time"))
			if(energyStorage.extractEnergy(Config.getInt("coredrill_consumption"), false)==Config.getInt("coredrill_consumption"))
			{
				process++;
				this.markDirty();
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
	}

	/*
	 CAUTION: all these getters will not check whether or not they belong to the master!
	 Check this.pos and call them on the master TileEntity (pos==0) to avoid incorrect data.
	 They will also provide information at all times and ignore the sampling progress.
	  */
	public float getSampleProgress()
	{
		return process/(float)Config.getInt("coredrill_time");
	}
	public boolean isSamplingFinished()
	{
		return process>=Config.getInt("coredrill_time");
	}
	public String getVeinUnlocalizedName()
	{
		ExcavatorHandler.MineralMix mineral = ExcavatorHandler.getRandomMineral(worldObj, (xCoord>>4), (zCoord>>4));
		return (mineral==null)? null: mineral.name;
	}
	public String getVeinLocalizedName()
	{
		String name = getVeinUnlocalizedName();
		if(name==null)
			return null;
		String unlocalizedName = Lib.DESC_INFO+"mineral."+name;
		String localizedName = StatCollector.translateToLocal(unlocalizedName);
		if(unlocalizedName.equals(localizedName))
			return name;
		return localizedName;
	}
	public float getVeinIntegrity()
	{
		MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(worldObj, (xCoord>>4), (zCoord>>4));
		if(ExcavatorHandler.mineralVeinCapacity<0||info.depletion<0)
			return -1;
		else if(info.mineralOverride==null && info.mineral==null)
			return 0;
		else
			return (Config.getInt("excavator_depletion")-info.depletion)/(float)Config.getInt("excavator_depletion");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("pos", pos);
		nbt.setInteger("process", process);
	}
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		pos = nbt.getInteger("pos");
		process = nbt.getInteger("process");
	}

	@SideOnly(Side.CLIENT)
	private AxisAlignedBB renderAABB;
	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(renderAABB==null)
			if(pos==0)
				renderAABB = AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord+1,yCoord+3,zCoord+1);
			else
				renderAABB = AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord,yCoord,zCoord);
		return renderAABB;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return pos==0;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if(pos!=0)
		{
			TileEntity te = worldObj.getTileEntity(xCoord, yCoord-pos, zCoord);
			if(te instanceof TileEntitySampleDrill)
				return ((TileEntitySampleDrill)te).receiveEnergy(from, maxReceive, simulate);
		}
		return energyStorage.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		if(pos!=0)
		{
			TileEntity te = worldObj.getTileEntity(xCoord, yCoord-pos, zCoord);
			if(te instanceof TileEntitySampleDrill)
				return ((TileEntitySampleDrill)te).getEnergyStored(from);
		}
		return energyStorage.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		if(pos!=0)
		{
			TileEntity te = worldObj.getTileEntity(xCoord, yCoord-pos, zCoord);
			if(te instanceof TileEntitySampleDrill)
				return ((TileEntitySampleDrill)te).getMaxEnergyStored(from);
		}
		return energyStorage.getMaxEnergyStored();
	}

	@Optional.Method(modid = "OpenComputers")
	@Override
	public boolean canConnectNode(ForgeDirection side)
	{
		return (pos==0);
	}

	@Optional.Method(modid = "OpenComputers")
	@Override
	public String getComponentName()
	{
		return "IE:sampleDrill";
	}

	/*
	 only the master will connect, so we don't need to check or find it before calling the getters.
	 */
	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] getSampleProgress(Context context, Arguments args)
	{
		return new Object[]{getSampleProgress()};
	}
	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] isSamplingFinished(Context context, Arguments args)
	{
		return new Object[]{isSamplingFinished()};
	}
	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] getVeinUnlocalizedName(Context context, Arguments args)
	{
		if(isSamplingFinished())
			return new Object[]{getVeinUnlocalizedName()};
		return new Object[0];
	}
	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] getVeinLocalizedName(Context context, Arguments args)
	{
		if(isSamplingFinished())
			return new Object[]{getVeinLocalizedName()};
		return new Object[0];
	}
	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] getVeinIntegrity(Context context, Arguments args)
	{
		if(isSamplingFinished())
			return new Object[]{getVeinIntegrity()};
		return new Object[0];
	}
	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] getEnergyStored(Context context, Arguments args)
	{
		return new Object[]{energyStorage.getEnergyStored()};
	}
	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] getMaxEnergyStored(Context context, Arguments args)
	{
		return new Object[]{energyStorage.getMaxEnergyStored()};
	}
}