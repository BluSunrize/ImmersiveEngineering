package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import cofh.api.energy.IEnergyReceiver;

public class TileEntityLightningRod extends TileEntityIEBase
{
	public boolean formed = false;
	public byte type = 4;
	int powerStored=0;
	
	public static boolean _Immovable()
	{
		return true;
	}
	
	@Override
	public void updateEntity()
	{
		if(!worldObj.isRemote && formed && (type==1||type==3||type==5||type==7) && powerStored>0)
		{
			ForgeDirection fd = type==1?ForgeDirection.NORTH: type==7?ForgeDirection.SOUTH: type==3?ForgeDirection.EAST: ForgeDirection.WEST;
			if(worldObj.getTileEntity(xCoord+fd.offsetX, yCoord, zCoord+fd.offsetZ) instanceof IEnergyReceiver)
			{
				IEnergyReceiver ier = (IEnergyReceiver)worldObj.getTileEntity(xCoord+fd.offsetX, yCoord, zCoord+fd.offsetZ);
				powerStored -= ier.receiveEnergy(fd.getOpposite(), powerStored, false);
			}
		}

		if(!worldObj.isRemote && formed && type==4)
			if(worldObj.getTotalWorldTime()%256==0 )
				//&& ( worldObj.isThundering() || (worldObj.isRaining()&&worldObj.rand.nextInt(10)==0) ))
			{
				int height = 0;
				boolean broken = false;
				for(int i=yCoord+1; i<worldObj.getHeight()-1; i++)
				{
					if(!broken && worldObj.getBlock(xCoord, i, zCoord).equals(IEContent.blockMetalDecoration) && worldObj.getBlockMetadata(xCoord, i, zCoord)==0)
						height++;
					else if(!worldObj.isAirBlock(xCoord, i, zCoord))
						return;
					else
					{
						if(!broken)
							broken=true;
					}
				}
				//				if(GuiScreen.isShiftKeyDown())
				//				System.out.println(xCoord+","+zCoord+": "+height);

				//				if(!worldObj.isThundering()&&yCoord+rodvalue<128) rodvalue=0;

								if (worldObj.rand.nextInt(4096*worldObj.getHeight())<height*(yCoord+height))
				//					worldObj.addWeatherEffect(new EntityLightningBolt(worldObj, xCoord, yCoord+height, zCoord));
				worldObj.spawnEntityInWorld(new EntityLightningBolt(worldObj, xCoord, yCoord+height, zCoord));
			}
	}

	public void outputEnergy(int amount)
	{
		for(ForgeDirection fd : new ForgeDirection[]{ForgeDirection.NORTH,ForgeDirection.SOUTH,ForgeDirection.EAST,ForgeDirection.WEST})
			if(worldObj.getTileEntity(xCoord+fd.offsetX, yCoord, zCoord+fd.offsetZ) instanceof TileEntityLightningRod)
				((TileEntityLightningRod)worldObj.getTileEntity(xCoord+fd.offsetX, yCoord, zCoord+fd.offsetZ)).powerStored=amount/4;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt)
	{
		formed = nbt.getBoolean("formed");
		type = nbt.getByte("type");
		powerStored = nbt.getInteger("powerStored");
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt)
	{
		nbt.setBoolean("formed", formed);
		nbt.setByte("type", type);
		nbt.setInteger("powerStored", powerStored);
	}

}
