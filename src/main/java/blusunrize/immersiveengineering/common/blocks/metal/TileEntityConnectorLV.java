package blusunrize.immersiveengineering.common.blocks.metal;

import static blusunrize.immersiveengineering.common.util.Utils.toIIC;

import java.util.HashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.WireType;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.compat.GregTechHelper;
import blusunrize.immersiveengineering.common.util.compat.IC2Helper;
import blusunrize.immersiveengineering.common.util.compat.ModCompatability;
import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Optional.Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "IC2")
public class TileEntityConnectorLV extends TileEntityImmersiveConnectable implements IEnergyHandler, ic2.api.energy.tile.IEnergySink
{
	boolean inICNet=false;
	public int facing=0;
	private long lastTransfer = -1;
	public static int[] connectorInputValues = {};

	@Override
	public void updateEntity()
	{
		if(Lib.IC2 && !this.inICNet && !FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			IC2Helper.loadIC2Tile(this);
			this.inICNet = true;
		}
	}
	@Override
	public void invalidate()
	{
		super.invalidate();
		unload();
	}
	void unload()
	{
		if(Lib.IC2 && this.inICNet)
		{
			IC2Helper.unloadIC2Tile(this);
			this.inICNet = false;
		}
	}
	@Override
	public void onChunkUnload()
	{
		unload();
	}

	@Override
	public boolean canUpdate()
	{
		return Lib.IC2;
	}

	@Override
	protected boolean canTakeLV()
	{
		return true;
	}

	@Override
	public boolean isEnergyOutput()
	{
		ForgeDirection fd = ForgeDirection.getOrientation(facing);
		TileEntity tile = worldObj.getTileEntity(xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ);
		return tile !=null && (tile instanceof IEnergyReceiver || (Lib.IC2 && IC2Helper.isEnergySink(tile)) || (Lib.GREG && GregTechHelper.gregtech_isEnergyConnected(tile)));
	}
	@Override
	public int outputEnergy(int amount, boolean simulate, int energyType)
	{
		ForgeDirection fd = ForgeDirection.getOrientation(facing);
		TileEntity capacitor = worldObj.getTileEntity(xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ);
		if(capacitor instanceof IEnergyReceiver && ((IEnergyReceiver)capacitor).canConnectEnergy(fd.getOpposite()))
		{
			return  ((IEnergyReceiver)capacitor).receiveEnergy(fd.getOpposite(), amount, simulate);
		}
		else if(Lib.IC2 && IC2Helper.isAcceptingEnergySink(capacitor, this, fd.getOpposite()))
		{
			double left = IC2Helper.injectEnergy(capacitor, fd.getOpposite(), ModCompatability.convertRFtoEU(amount, getIC2Tier()), canTakeHV()?(256*256): canTakeMV()?(128*128) : (32*32), simulate);
			return amount-ModCompatability.convertEUtoRF(left);
		}
		else if(Lib.GREG && GregTechHelper.gregtech_isEnergyConnected(capacitor))
			if(simulate)
			{
				long accepted = GregTechHelper.gregtech_outputGTPower(capacitor, (byte)fd.getOpposite().ordinal(), (long)ModCompatability.convertRFtoEU(amount, getIC2Tier()), 1L);
				GregTechHelper.gregtech_outputGTPower(capacitor, (byte)fd.getOpposite().ordinal(), -accepted, 1L);
				return (int)accepted;
			}
			else
				return (int)GregTechHelper.gregtech_outputGTPower(capacitor, (byte)fd.getOpposite().ordinal(), (long)ModCompatability.convertRFtoEU(amount, getIC2Tier()), 1L);
		return 0;
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing);
		nbt.setLong("lastTransfer", lastTransfer);
	}
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = nbt.getInteger("facing");
		lastTransfer = nbt.getLong("lastTransfer");
	}

	@Override
	public Vec3 getRaytraceOffset(IImmersiveConnectable link)
	{
		ForgeDirection fd = ForgeDirection.getOrientation(facing).getOpposite();
		return Vec3.createVectorHelper(.5+fd.offsetX*.0625, .5+fd.offsetY*.0625, .5+fd.offsetZ*.0625);
	}
	@Override
	public Vec3 getConnectionOffset(Connection con)
	{
		ForgeDirection fd = ForgeDirection.getOrientation(facing).getOpposite();
		double conRadius = con.cableType.getRenderDiameter()/2;
		return Vec3.createVectorHelper(.5-conRadius*fd.offsetX, .5-conRadius*fd.offsetY, .5-conRadius*fd.offsetZ);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(Config.getBoolean("increasedRenderboxes"))
		{
			int inc = getRenderRadiusIncrease();
			return AxisAlignedBB.getBoundingBox(xCoord-inc,yCoord-inc,zCoord-inc, xCoord+inc+1,yCoord+inc+1,zCoord+inc+1);
		}
		return super.getRenderBoundingBox();
	}
	int getRenderRadiusIncrease()
	{
		return WireType.COPPER.getMaxLength();
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return from.ordinal()==facing;
	}
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive,boolean simulate)
	{
		return transferEnergy(maxReceive, simulate, 0);
	}
	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		return 0;
	}
	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		return 0;
	}
	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract,boolean simulate)
	{
		return 0;
	}

	public int transferEnergy(int energy, boolean simulate, final int energyType)
	{
		int received = 0;
		if(!worldObj.isRemote)
		{
			if(worldObj.getTotalWorldTime()==lastTransfer)
				return 0;
			ConcurrentSkipListSet<AbstractConnection> outputs = ImmersiveNetHandler.INSTANCE.getIndirectEnergyConnections(Utils.toCC(this), worldObj);
			int powerLeft = Math.min(Math.min(getMaxOutput(),getMaxInput()), energy);
			final int powerForSort = powerLeft;

			IELogger.debug("");
			IELogger.debug("Sending power: "+powerLeft+" at "+xCoord+","+yCoord+","+zCoord);
			if(outputs.size()<1)
				return 0;

			int sum = 0;
			HashMap<AbstractConnection,Integer> powerSorting = new HashMap<AbstractConnection,Integer>();
			for(AbstractConnection con : outputs)
				if(con!=null && con.cableType!=null && toIIC(con.end, worldObj)!=null)
				{
					int atmOut = Math.min(powerForSort,con.cableType.getTransferRate());
					int tempR = toIIC(con.end,worldObj).outputEnergy(atmOut, true, energyType);
					IELogger.debug("trying "+atmOut+"RF for: "+con.end+", accepted "+tempR);
					if(tempR>0)
					{
						IELogger.debug("Can output "+tempR+"RF to "+con.end);
						powerSorting.put(con, tempR);
						sum += tempR;
					}
				}

			if(sum>0)
				for(AbstractConnection con : powerSorting.keySet())
					if(con!=null && con.cableType!=null && toIIC(con.end, worldObj)!=null)
					{
						float prio = powerSorting.get(con)/(float)sum;
						int output = (int)(powerForSort*prio);

						int tempR = toIIC(con.end,worldObj).outputEnergy(Math.min(output,con.cableType.getTransferRate()), true, energyType);
						int r = tempR;
						tempR -= (int) Math.max(0, Math.floor(tempR*con.getPreciseLossRate(tempR,getMaxInput())));
						toIIC(con.end, worldObj).outputEnergy(tempR, simulate, energyType);
						
						for(Connection sub : con.subConnections)
						{
							IELogger.debug("Sub Con"+sub.start+" to "+sub.end);
							int transferredPerCon = ImmersiveNetHandler.INSTANCE.getTransferedRates(worldObj.provider.dimensionId).containsKey(sub)?ImmersiveNetHandler.INSTANCE.getTransferedRates(worldObj.provider.dimensionId).get(sub):0;
							IELogger.debug("old t "+transferredPerCon);
							transferredPerCon += r;
							IELogger.debug("new t "+transferredPerCon);
							if(transferredPerCon>sub.cableType.getTransferRate())
							{
								IELogger.debug("Okay, this wire is HAWT.");
								IELogger.debug("Or at least hotter than "+sub.cableType.getTransferRate());
							}	
							if(!simulate)
								ImmersiveNetHandler.INSTANCE.getTransferedRates(worldObj.provider.dimensionId).put(sub,transferredPerCon);
						}

						received += r;
						powerLeft -= r;
						if(powerLeft<=0)
							break;
					}
			if(!simulate)
				lastTransfer = worldObj.getTotalWorldTime();
		}
		return received;
	}


	public int getMaxInput()
	{
		return connectorInputValues[0];
	}
	public int getMaxOutput()
	{
		return connectorInputValues[0];
	}

	@Optional.Method(modid = "IC2")
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		return Lib.IC2 && canConnectEnergy(direction);
	}
	@Optional.Method(modid = "IC2")
	public double getDemandedEnergy()
	{
		return getMaxInput()/4;
	}
	@Optional.Method(modid = "IC2")
	public int getSinkTier()
	{
		return getIC2Tier();
	}
	int getIC2Tier()
	{
		return this.canTakeHV()?3: this.canTakeMV()?2: 1;
	}
	@Optional.Method(modid = "IC2")
	public double injectEnergy(ForgeDirection directionFrom, double amount, double voltage)
	{
		int r =  transferEnergy(ModCompatability.convertEUtoRF(amount), false, 1);
		double left = amount-(r/4f);
		return left;
	}
}