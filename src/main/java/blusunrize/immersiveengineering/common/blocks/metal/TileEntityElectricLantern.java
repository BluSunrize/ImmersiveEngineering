package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumSkyBlock;
import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISpawnInterdiction;
import blusunrize.immersiveengineering.common.blocks.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.common.util.Utils;

public class TileEntityElectricLantern extends TileEntityImmersiveConnectable implements ISpawnInterdiction
{
	public int energyStorage = 0;
	public boolean active = false;
	private boolean interdictionList=false; 

	@Override
	public void updateEntity()
	{
		if(worldObj.isRemote)
			return;
		if(!interdictionList)
		{
			if(!EventHandler.interdictionTiles.contains(this))
				EventHandler.interdictionTiles.add(this);
			interdictionList=true;
		}
		boolean b = active;
		if(energyStorage>0)
		{
			energyStorage--;
			if(!active)
				active=true;
		}
		else if(active)
			active=false;

		if(active!=b)
		{
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			worldObj.updateLightByType(EnumSkyBlock.Block, xCoord, yCoord, zCoord);
			worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 1, 0);
		}
	}

	@Override
	public double getInterdictionRangeSquared()
	{
		return active?1024:0;
	}

	@Override
	public void invalidate()
	{
		if(EventHandler.interdictionTiles.contains(this))
			EventHandler.interdictionTiles.remove(this);
		super.invalidate();
	}
	
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		active = nbt.getBoolean("active");
		energyStorage = nbt.getInteger("energyStorage");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setBoolean("active",active);
		nbt.setInteger("energyStorage",energyStorage);
	}

	@Override
	protected boolean canTakeLV()
	{
		return true;
	}
	@Override
	public boolean isEnergyOutput()
	{
		return true;
	}
	@Override
	public int outputEnergy(int amount, boolean simulate, int energyType)
	{
		if(amount>0 && energyStorage<10)
		{
			if(!simulate)
				energyStorage++;
			return Math.min(10-energyStorage, 2);
		}
		return 0;
	}
	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==1)
		{
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			worldObj.updateLightByType(EnumSkyBlock.Block, xCoord, yCoord, zCoord);
			return true;
		}
		return super.receiveClientEvent(id, arg);
	}
	@Override
	public Vec3 getRaytraceOffset(IImmersiveConnectable link)
	{
		int xDif = xCoord - ((TileEntity)link).xCoord;
		int zDif = zCoord - ((TileEntity)link).zCoord;
		int h = ((TileEntity)link).yCoord>yCoord?1:0;
		if(xDif==0&&zDif==0)
			return Vec3.createVectorHelper(.5, h, .5);
		else if(Math.abs(xDif)>=Math.abs(zDif))
			return Vec3.createVectorHelper(xDif>0?.125:.875, h, .5);
		else
			return Vec3.createVectorHelper(.5, h, zDif>0?.125:.875);
	}
	@Override
	public Vec3 getConnectionOffset(Connection con)
	{
		int xDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.posX-xCoord: (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.posX-xCoord: 0;
		int zDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.posZ-zCoord: (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.posZ-zCoord: 0;
		if(Math.abs(xDif)>=Math.abs(zDif))
			return Vec3.createVectorHelper(xDif<0?.25:xDif>0?.75:.5, .0625, .5);
		return Vec3.createVectorHelper(.5, .0625, zDif<0?.25:zDif>0?.75:.5);
	}
}