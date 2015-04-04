package blusunrize.immersiveengineering.common.blocks.metal;

import static blusunrize.immersiveengineering.common.Utils.toIIC;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.api.WireType;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.Utils;
import blusunrize.immersiveengineering.common.blocks.TileEntityImmersiveConnectable;
import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityConnectorLV extends TileEntityImmersiveConnectable implements IEnergyHandler
{
	public int facing=0;
	@Override
	public boolean canUpdate()
	{
		return false;
	}
	
	@Override
	protected boolean canTakeLV()
	{
		return true;
	}

	@Override
	public boolean isRFInOutput()
	{
		ForgeDirection fd = ForgeDirection.getOrientation(facing);
		return worldObj.getTileEntity(xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ) instanceof IEnergyReceiver;
	}
	@Override
	public int outputRedstoneFlux(int amount, boolean simulate)
	{
		ForgeDirection fd = ForgeDirection.getOrientation(facing);
		TileEntity capacitor = worldObj.getTileEntity(xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ);
		if(capacitor instanceof IEnergyReceiver && ((IEnergyReceiver)capacitor).canConnectEnergy(fd.getOpposite()))
			return ((IEnergyReceiver)capacitor).receiveEnergy(fd.getOpposite(), amount, simulate);//.outputRedstoneFlux(amount, simulate);
		return 0;
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt)
	{
		super.writeCustomNBT(nbt);
		nbt.setInteger("facing", facing);
	}
	@Override
	public void readCustomNBT(NBTTagCompound nbt)
	{
		super.readCustomNBT(nbt);
		facing = nbt.getInteger("facing");
	}

	@Override
	public Vec3 getRaytraceOffset()
	{
		ForgeDirection fd = ForgeDirection.getOrientation(facing).getOpposite();
		return Vec3.createVectorHelper(.5+.5*fd.offsetX, .5+.5*fd.offsetY, .5+.5*fd.offsetZ);
	}
	@Override
	public Vec3 getConnectionOffset(Connection con)
	{
		ForgeDirection fd = ForgeDirection.getOrientation(facing).getOpposite();
		double conRadius = con.cableType==WireType.STEEL?.03125:.015625;
		return Vec3.createVectorHelper(.5-conRadius*fd.offsetX, .5-conRadius*fd.offsetY, .5-conRadius*fd.offsetZ);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(Config.getBoolean("increasedRenderboxes"))
			return AxisAlignedBB.getBoundingBox(xCoord-16,yCoord-16,zCoord-16, xCoord+17,yCoord+17,zCoord+17);
		return super.getRenderBoundingBox();
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return true;
	}
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive,boolean simulate)
	{
		int received = 0;
		if(!worldObj.isRemote)
		{
			List<AbstractConnection> outputs = ImmersiveNetHandler.getIndirectEnergyConnections(Utils.toCC(this), worldObj);
			int powerLeft = Math.min(Math.min(getMaxOutput(),getMaxInput()), maxReceive);
			for(AbstractConnection con : outputs)
				if(con!=null && toIIC(con.end, worldObj)!=null)
				{
					int tempR = toIIC(con.end,worldObj).outputRedstoneFlux(Math.min(powerLeft,con.cableType.getTransferRate()), true);
					tempR -= (int) Math.floor(tempR*con.getAverageLossRate());
					int r = toIIC(con.end, worldObj).outputRedstoneFlux(tempR, simulate);
					received += r;
					powerLeft -= r;
					if(powerLeft<=0)
						break;
				}
		}
		return received;
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
	
	public int getMaxInput()
	{
		return WireType.COPPER.getTransferRate();
	}
	public int getMaxOutput()
	{
		return WireType.COPPER.getTransferRate();
	}


}