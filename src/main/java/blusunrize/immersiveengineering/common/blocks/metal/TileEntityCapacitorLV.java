package blusunrize.immersiveengineering.common.blocks.metal;

import static blusunrize.immersiveengineering.common.util.Utils.toIIC;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyReceiver;

public class TileEntityCapacitorLV extends TileEntityIEBase implements IEnergyHandler, IBlockOverlayText
{
	int[] sideConfig={-1,0,-1,-1,-1,-1};
	EnergyStorage energyStorage = new EnergyStorage(getMaxStorage(),getMaxInput(),getMaxOutput());

	public int comparatorOutput=0;

	@Override
	public void updateEntity()
	{
		//		if(worldObj.isRemote)
		//		{
		//			if(worldObj.getTotalWorldTime()%20==0)
		//			{
		//				List<Vec3> vecs = new ArrayList();
		//				vecs.add(Vec3.createVectorHelper(0, 1, 0));
		//				for(int i=0; i<6; i++)
		//				{
		//					Vec3 off = Vec3.createVectorHelper(0, 1+(worldObj.rand.nextGaussian()*.25 -.125), 0);
		//					off.rotateAroundY((float) Math.toRadians(20*worldObj.rand.nextFloat() - 10));
		//					off.rotateAroundX((float) Math.toRadians(20*worldObj.rand.nextFloat() - 10));
		//					off.rotateAroundZ((float) Math.toRadians(20*worldObj.rand.nextFloat() - 10));
		//					vecs.add(off);
		//				}
		//
		//				double px = 0;
		//				double py = 0;
		//				double pz = 0;
		//				for(int i=0; i<vecs.size(); i++)
		//				{
		//					Vec3 vec = vecs.get(i);
		////					worldObj.spawnParticle("flame", xCoord+.5+px+vec.xCoord,yCoord+.5+py+vec.yCoord,zCoord+.5+pz+vec.zCoord, 0, 0, 0);
		//					for(int j=0; j<10; j++)
		//					{
		//						double dx = (vec.xCoord/10d) * j;
		//						double dy = (vec.yCoord/10d) * j;
		//						double dz = (vec.zCoord/10d) * j;
		//						worldObj.spawnParticle("flame", xCoord+.5+px+dx,yCoord+.5+py+dy,zCoord+.5+pz+dz, 0, 0, 0);
		//						}
		//					
		//					px += vec.xCoord;
		//					py += vec.yCoord;
		//					pz += vec.zCoord;
		//				}
		//
		//			}
		//			return;
		//		}

		for(int i=0; i<6; i++)
			this.transferEnergy(i);

		if(worldObj.getTotalWorldTime()%32==((xCoord^zCoord)&31))
		{
			int i = scaleStoredEnergyTo(15);
			if(i!=this.comparatorOutput)
			{
				this.comparatorOutput=i;
				worldObj.func_147453_f(xCoord, yCoord, zCoord, getBlockType());
			}
		}
	}
	public int scaleStoredEnergyTo(int scale)
	{
		return (int)(scale*(energyStorage.getEnergyStored()/(float)energyStorage.getMaxEnergyStored()));
	}

	protected void transferEnergy(int side)
	{
		if(this.sideConfig[side] != 1)
			return;
		ForgeDirection fd = ForgeDirection.VALID_DIRECTIONS[side];
		if(worldObj.getTileEntity(xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ) instanceof IEnergyReceiver)
			this.energyStorage.modifyEnergyStored(-((IEnergyReceiver)worldObj.getTileEntity(xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ)).receiveEnergy(fd.getOpposite(), Math.min(getMaxOutput(), this.energyStorage.getEnergyStored()), false));
		else if(worldObj.getTileEntity(xCoord+fd.offsetX,yCoord+fd.offsetY,zCoord+fd.offsetZ) instanceof TileEntityConnectorLV)
		{
			IImmersiveConnectable node = (IImmersiveConnectable) worldObj.getTileEntity(xCoord+fd.offsetX,yCoord+fd.offsetY,zCoord+fd.offsetZ);
			if(!node.isEnergyOutput())
				return;
			List<AbstractConnection> outputs = ImmersiveNetHandler.INSTANCE.getIndirectEnergyConnections(Utils.toCC(node), worldObj);
			int received = 0;
			int powerLeft = Math.min(getMaxOutput(), this.energyStorage.getEnergyStored());
			for(AbstractConnection con : outputs)
				if(con!=null && toIIC(con.end, worldObj)!=null)
				{
					int tempR = toIIC(con.end,worldObj).outputEnergy(Math.min(powerLeft,con.cableType.getTransferRate()), true, 0);
					tempR -= (int) Math.floor(tempR*con.getAverageLossRate());
					int r = toIIC(con.end, worldObj).outputEnergy(tempR, false, 0);
					received += r;
					powerLeft -= r;
					if(powerLeft<=0)
						break;
				}
			this.energyStorage.modifyEnergyStored(-received);
		}
	}
	public void toggleSide(int side)
	{
		sideConfig[side]++;
		if(sideConfig[side]>1)
			sideConfig[side]=-1;
		worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 0, 0);
	}
	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==0)
		{
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			return true;
		}
		return false;
	}

	public int getMaxStorage()
	{
		return Config.getInt("capacitorLV_storage");
	}
	public int getMaxInput()
	{
		return Config.getInt("capacitorLV_input");
	}
	public int getMaxOutput()
	{
		return Config.getInt("capacitorLV_output");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setIntArray("sideConfig", sideConfig);
		energyStorage.writeToNBT(nbt);
	}
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		sideConfig = nbt.getIntArray("sideConfig");
		if(sideConfig==null || sideConfig.length<6)
			sideConfig = new int[6];
		energyStorage.readFromNBT(nbt);
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection fd)
	{
		if(fd.ordinal()>=sideConfig.length || sideConfig[fd.ordinal()]<0)
			return false;
		return true;
	}
	@Override
	public int extractEnergy(ForgeDirection fd, int amount, boolean simulate)
	{
		if(worldObj.isRemote || fd.ordinal()>=sideConfig.length || sideConfig[fd.ordinal()]!=1)
			return 0;
		int r = energyStorage.extractEnergy(amount, simulate);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		return r;
	}
	@Override
	public int getEnergyStored(ForgeDirection fd)
	{
		return energyStorage.getEnergyStored();
	}
	@Override
	public int getMaxEnergyStored(ForgeDirection fd)
	{
		return energyStorage.getMaxEnergyStored();
	}
	@Override
	public int receiveEnergy(ForgeDirection fd, int amount, boolean simulate)
	{
		if(worldObj.isRemote || fd.ordinal()>=sideConfig.length || sideConfig[fd.ordinal()]!=0)
			return 0;
		int r = energyStorage.receiveEnergy(amount, simulate);
		return r;
	}
	@Override
	public String[] getOverlayText(MovingObjectPosition mop)
	{
		if(Config.getBoolean("colourblindSupport"))
		{
			int i = sideConfig[Math.min(sideConfig.length-1, mop.sideHit)];
			int j = sideConfig[Math.min(sideConfig.length-1, ForgeDirection.OPPOSITES[mop.sideHit])];
			return new String[]{
					StatCollector.translateToLocal(Lib.DESC_INFO+"capacitorSide.facing")
					+StatCollector.translateToLocal(Lib.DESC_INFO+"capacitorSide."+i),
					StatCollector.translateToLocal(Lib.DESC_INFO+"capacitorSide.opposite")
					+StatCollector.translateToLocal(Lib.DESC_INFO+"capacitorSide."+j)
			};
		}
		return null;
	}
}