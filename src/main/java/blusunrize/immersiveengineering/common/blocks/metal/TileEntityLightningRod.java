package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
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

	ArrayList<ChunkCoordinates> fenceNet = null;
	int height;

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

			if(worldObj.getTotalWorldTime()%256==((xCoord^zCoord)&255))
				fenceNet = null;
			if(fenceNet==null)
				fenceNet = this.getFenceNet();

			if(fenceNet!=null && worldObj.getTotalWorldTime()%128==((xCoord^zCoord)&127) && ( worldObj.isThundering() || (worldObj.isRaining()&&worldObj.rand.nextInt(10)==0) ))
			{
				int i = this.height + this.fenceNet.size();
				if(worldObj.rand.nextInt(4096*worldObj.getHeight())<i*(yCoord+i))
				{
					this.energyStorage.setEnergyStored(Config.getInt("lightning_output"));
					ChunkCoordinates cc = fenceNet.get(worldObj.rand.nextInt(fenceNet.size()));
					EntityLightningBolt entityLightningBolt = new EntityLightningBolt(worldObj, cc.posX,cc.posY,cc.posZ);
					worldObj.addWeatherEffect(entityLightningBolt);
					worldObj.spawnEntityInWorld(entityLightningBolt);
				}
			}
		}
	}

	ArrayList<ChunkCoordinates> getFenceNet()
	{
		this.height = 0;
		boolean broken = false;
		for(int i=yCoord+1; i<worldObj.getHeight()-1; i++)
		{
			if(!broken && worldObj.getBlock(xCoord, i, zCoord).equals(IEContent.blockMetalDecoration) && worldObj.getBlockMetadata(xCoord, i, zCoord)==0)
				this.height++;
			else if(!worldObj.isAirBlock(xCoord, i, zCoord))
				return null;
			else
			{
				if(!broken)
					broken=true;
			}
		}

		ArrayList<ChunkCoordinates> openList = new ArrayList();
		ArrayList<ChunkCoordinates> closedList = new ArrayList();
		openList.add(new ChunkCoordinates(xCoord,yCoord+this.height,zCoord));
		while(!openList.isEmpty() && closedList.size()<256)
		{
			ChunkCoordinates next = openList.get(0);
			if(!closedList.contains(next) && worldObj.getBlock(next.posX,next.posY,next.posZ).equals(IEContent.blockMetalDecoration) && worldObj.getBlockMetadata(next.posX,next.posY,next.posZ)==0)
			{
				closedList.add(next);
				openList.add(new ChunkCoordinates(next.posX+1,next.posY,next.posZ));
				openList.add(new ChunkCoordinates(next.posX-1,next.posY,next.posZ));
				openList.add(new ChunkCoordinates(next.posX,next.posY,next.posZ+1));
				openList.add(new ChunkCoordinates(next.posX,next.posY,next.posZ-1));
				openList.add(new ChunkCoordinates(next.posX,next.posY+1,next.posZ));
			}
			openList.remove(0);
		}
		return closedList;
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
	public float[] getBlockBounds()
	{
		return new float[]{0,0,0,1,1,1};
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