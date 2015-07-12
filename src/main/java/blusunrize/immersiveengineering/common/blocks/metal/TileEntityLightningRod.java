package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.block.Block;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;

public class TileEntityLightningRod extends TileEntityMultiblockPart implements IEnergyProvider
{
	public EnergyStorage energyStorage = new EnergyStorage(Config.getInt("lightning_output"));

	public static boolean _Immovable()
	{
		return true;
	}

	@Override
	public void updateEntity()
	{
		if(!worldObj.isRemote && formed && pos==4)
		{
			if(energyStorage.getEnergyStored()>0)
			{
				for(ForgeDirection fd : new ForgeDirection[]{ForgeDirection.NORTH,ForgeDirection.SOUTH,ForgeDirection.EAST,ForgeDirection.WEST})
					if(worldObj.getTileEntity(xCoord+fd.offsetX*2, yCoord, zCoord+fd.offsetZ*2) instanceof IEnergyReceiver)
					{
						IEnergyReceiver ier = (IEnergyReceiver)worldObj.getTileEntity(xCoord+fd.offsetX*2, yCoord, zCoord+fd.offsetZ*2);
						int accepted = ier.receiveEnergy(fd.getOpposite(), energyStorage.getEnergyStored(), true);
						int extracted = energyStorage.extractEnergy(accepted, false);
						ier.receiveEnergy(fd.getOpposite(), extracted, false);
					}
			}
			if(worldObj.getTotalWorldTime()%257==((xCoord^zCoord)&256) && ( worldObj.isThundering() || (worldObj.isRaining()&&worldObj.rand.nextInt(10)==0) ))
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

				if (worldObj.rand.nextInt(4096*worldObj.getHeight())<height*(yCoord+height))
				{
					this.energyStorage.setEnergyStored(Config.getInt("lightning_output"));
					EntityLightningBolt entityLightningBolt = new EntityLightningBolt(worldObj, xCoord, yCoord+height, zCoord);
					worldObj.addWeatherEffect(entityLightningBolt);
					worldObj.spawnEntityInWorld(entityLightningBolt);
				}
			}
		}
	}

	public TileEntityLightningRod master()
	{
		if(offset[0]==0&&offset[1]==0&&offset[2]==0)
			return null;
		TileEntity te = worldObj.getTileEntity(xCoord-offset[0], yCoord-offset[1], zCoord-offset[2]);
		return te instanceof TileEntityLightningRod?(TileEntityLightningRod)te : null;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		energyStorage.readFromNBT(nbt);
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		energyStorage.writeToNBT(nbt);
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==0)
		{
			this.formed = arg==1;
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			return true;
		}
		return false;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		ForgeDirection fd = pos==1?ForgeDirection.NORTH: pos==7?ForgeDirection.SOUTH: pos==3?ForgeDirection.EAST: ForgeDirection.WEST;
		return from==fd.getOpposite();
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		if(master()!=null)
			return master().energyStorage.extractEnergy(maxExtract, simulate);
		return energyStorage.extractEnergy(maxExtract, simulate);
	}

	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		if(master()!=null)
			return master().getEnergyStored(from);
		return energyStorage.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		if(master()!=null)
			return master().getMaxEnergyStored(from);
		return energyStorage.getMaxEnergyStored();
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		return new ItemStack(IEContent.blockMetalMultiblocks,1,BlockMetalMultiblocks.META_lightningRod);
	}

	@Override
	public void invalidate()
	{
		super.invalidate();

		if(formed && !worldObj.isRemote)
		{
			TileEntityLightningRod master = master();
			if(master==null)
				master = this;
			for(int l=-1;l<=1;l++)
				for(int w=-1;w<=1;w++)
				{
					int xx = master.xCoord+w;
					int yy = master.yCoord;
					int zz = master.zCoord+l;

					ItemStack s = null;
					if(worldObj.getTileEntity(xx,yy,zz) instanceof TileEntityLightningRod)
					{
						s = ((TileEntityLightningRod)worldObj.getTileEntity(xx,yy,zz)).getOriginalBlock();
						((TileEntityLightningRod)worldObj.getTileEntity(xx,yy,zz)).formed=false;
					}
					if(xx==xCoord && yy==yCoord && zz==zCoord)
						s = this.getOriginalBlock();
					if(s!=null && Block.getBlockFromItem(s.getItem())!=null)
					{
						if(xx==xCoord && yy==yCoord && zz==zCoord)
							worldObj.spawnEntityInWorld(new EntityItem(worldObj, xCoord+.5,yCoord+.5,zCoord+.5, s));
						else
						{
							if(Block.getBlockFromItem(s.getItem())==IEContent.blockMetalMultiblocks)
								worldObj.setBlockToAir(xx,yy,zz);
							worldObj.setBlock(xx,yy,zz, Block.getBlockFromItem(s.getItem()), s.getItemDamage(), 0x3);
							worldObj.addBlockEvent(xx, yy, zz, IEContent.blockMetalMultiblocks, 0, 0);
						}
					}
				}
		}
	}
}