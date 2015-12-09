package blusunrize.immersiveengineering.common.blocks.metal;

import static blusunrize.immersiveengineering.common.util.Utils.toIIC;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
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
	public int currentTickAccepted=0;
	public static int[] connectorInputValues = {};
	public int energyStored=0;

	@Override
	public void updateEntity()
	{
		if(Lib.IC2 && !this.inICNet && !FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			IC2Helper.loadIC2Tile(this);
			this.inICNet = true;
		}
		if(energyStored>0)
		{
			int temp = this.transferEnergy(energyStored, true, 0);
			if(temp>0)
				energyStored -= this.transferEnergy(temp, false, 0);
		}
		currentTickAccepted=0;
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
		return true;
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
		return tile !=null && (tile instanceof IEnergyReceiver || (Lib.IC2 && IC2Helper.isEnergySink(tile)) || (Lib.GREG && GregTechHelper.gregtech_isValidEnergyOutput(tile)));
	}
	@Override
	public int outputEnergy(int amount, boolean simulate, int energyType)
	{
		int acceptanceLeft = getMaxOutput()-currentTickAccepted;
		if(acceptanceLeft<=0)
			return 0;
		int toAccept = Math.min(acceptanceLeft, amount);

		ForgeDirection fd = ForgeDirection.getOrientation(facing);
		TileEntity capacitor = worldObj.getTileEntity(xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ);
		int ret = 0;
		if(capacitor instanceof IEnergyReceiver && ((IEnergyReceiver)capacitor).canConnectEnergy(fd.getOpposite()))
		{
			ret = ((IEnergyReceiver)capacitor).receiveEnergy(fd.getOpposite(), toAccept, simulate);
		}
		else if(Lib.IC2 && IC2Helper.isAcceptingEnergySink(capacitor, this, fd.getOpposite()))
		{
			double left = IC2Helper.injectEnergy(capacitor, fd.getOpposite(), ModCompatability.convertRFtoEU(toAccept, getIC2Tier()), canTakeHV()?(256*256): canTakeMV()?(128*128) : (32*32), simulate);
			ret = amount-ModCompatability.convertEUtoRF(left);
		}
		else if(Lib.GREG && GregTechHelper.gregtech_isValidEnergyOutput(capacitor))
		{
			long translAmount = (long)ModCompatability.convertRFtoEU(toAccept, getIC2Tier());
			long accepted = GregTechHelper.gregtech_outputGTPower(capacitor, (byte)fd.getOpposite().ordinal(), translAmount, 1L, simulate);
			int reConv =  ModCompatability.convertEUtoRF(accepted);
			ret = reConv;
		}
		if(!simulate)
			currentTickAccepted+=ret;
		return ret;
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
		if(worldObj.isRemote)
			return 0;
		if(worldObj.getTotalWorldTime()==lastTransfer)
			return 0;

		int accepted = Math.min(Math.min(getMaxOutput(),getMaxInput()), maxReceive);
		accepted = Math.min(getMaxOutput()-energyStored, accepted);
		if(accepted<=0)
			return 0;

		if(!simulate)
		{
			energyStored += accepted;
			lastTransfer = worldObj.getTotalWorldTime();
		}

		return accepted;
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
			Set<AbstractConnection> outputs = ImmersiveNetHandler.INSTANCE.getIndirectEnergyConnections(Utils.toCC(this), worldObj);
			int powerLeft = Math.min(Math.min(getMaxOutput(),getMaxInput()), energy);
			final int powerForSort = powerLeft;

			IELogger.debug("");
			IELogger.debug("Sending power: "+powerLeft+" at "+xCoord+","+yCoord+","+zCoord);
			if(outputs.size()<1)
				return 0;

			int sum = 0;
			HashMap<AbstractConnection,Integer> powerSorting = new HashMap<AbstractConnection,Integer>();
			for(AbstractConnection con : outputs)
			{
				IImmersiveConnectable end = toIIC(con.end, worldObj);
				if(con.cableType!=null && end!=null)
				{
					int atmOut = Math.min(powerForSort,con.cableType.getTransferRate());
					int tempR = end.outputEnergy(atmOut, true, energyType);
					IELogger.debug("trying "+atmOut+"RF for: "+con.end+", accepted "+tempR);
					if(tempR>0)
					{
						IELogger.debug("Can output "+tempR+"RF to "+con.end);
						powerSorting.put(con, tempR);
						sum += tempR;
					}
				}
			}

			if(sum>0)
				for(AbstractConnection con : powerSorting.keySet())
				{
					IImmersiveConnectable end = toIIC(con.end, worldObj);
					if(con.cableType!=null && end!=null)
					{
						float prio = powerSorting.get(con)/(float)sum;
						int output = (int)(powerForSort*prio);

						int tempR = end.outputEnergy(Math.min(output, con.cableType.getTransferRate()), true, energyType);
						int r = tempR;
						int maxInput = getMaxInput();
						tempR -= (int) Math.max(0, Math.floor(tempR*con.getPreciseLossRate(tempR,maxInput)));
						end.outputEnergy(tempR, simulate, energyType);
						HashSet<IImmersiveConnectable> passedConnectors = new HashSet<IImmersiveConnectable>();
						float intermediaryLoss = 0;
						for(Connection sub : con.subConnections)
						{
							float length = sub.length/(float)sub.cableType.getMaxLength();
							float baseLoss = (float)sub.cableType.getLossRatio();
							float mod = (((maxInput-tempR)/(float)maxInput)/.25f)*.1f;
							intermediaryLoss = MathHelper.clamp_float(intermediaryLoss+length*(baseLoss+baseLoss*mod), 0,1);

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
							{
								ImmersiveNetHandler.INSTANCE.getTransferedRates(worldObj.provider.dimensionId).put(sub,transferredPerCon);
								IImmersiveConnectable subStart = toIIC(sub.start,worldObj);
								IImmersiveConnectable subEnd = toIIC(sub.end,worldObj);
								if(subStart!=null && passedConnectors.add(subStart))
									subStart.onEnergyPassthrough((int)(r-r*intermediaryLoss));
								if(subEnd!=null && passedConnectors.add(subEnd))
									subEnd.onEnergyPassthrough((int)(r-r*intermediaryLoss));
							}
						}
						received += r;
						powerLeft -= r;
						if(powerLeft<=0)
							break;
					}
				}
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
		return ModCompatability.convertRFtoEU(getMaxInput(), getIC2Tier());
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
		int rf = ModCompatability.convertEUtoRF(amount);
		if(rf>this.getMaxInput())//More Input than allowed results in blocking
			return amount;
		int rSimul = transferEnergy(rf, true, 1);
		if(rSimul==0)//This will prevent full power void but allow partial transfer
			return amount;
		int r = transferEnergy(rf, false, 1);
		double eu = ModCompatability.convertRFtoEU(r, getIC2Tier());
		return amount-eu;
	}
}