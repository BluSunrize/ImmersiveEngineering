package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler.IExternalHeatable;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityFurnaceHeater extends TileEntityIEBase implements IEnergyReceiver
{
	public EnergyStorage energyStorage = new EnergyStorage(32000,Math.max(256, Math.max(Config.getInt("heater_consumption"),Config.getInt("heater_speedupConsumption"))));
	public int[] sockets = new int[6];
	public boolean active = false;

	@Override
	public void updateEntity()
	{
		if(!worldObj.isRemote)
		{
			boolean a = active;
			if(active)
				active=false;
			for(ForgeDirection fd : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tileEntity = worldObj.getTileEntity(xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ);
				int consumed = 0;
				if(tileEntity!=null)
					if(tileEntity instanceof ExternalHeaterHandler.IExternalHeatable)
						consumed = ((IExternalHeatable)tileEntity).doHeatTick(energyStorage.getEnergyStored(), worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord));
					else
					{
						ExternalHeaterHandler.HeatableAdapter adapter = ExternalHeaterHandler.getHeatableAdapter(tileEntity.getClass());
						if(adapter!=null)
							consumed = adapter.doHeatTick(tileEntity, energyStorage.getEnergyStored(), worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord));
					}
				if(consumed>0)
				{
					this.energyStorage.extractEnergy(consumed, false);
					if(!active)
						active = true;
				}
			}
			if(active!=a)
			{
				this.markDirty();
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				worldObj.addBlockEvent(xCoord, yCoord, zCoord, IEContent.blockMetalDevice, 1,active?1:0);
			}
		}
	}

	public boolean canHeat(TileEntityFurnace furnace)
	{
		ItemStack input = furnace.getStackInSlot(0);
		if(input == null)
			return false;
		ItemStack output = FurnaceRecipes.smelting().getSmeltingResult(input);
		if(output == null)
			return false;
		ItemStack existingOutput = furnace.getStackInSlot(2);
		if(existingOutput==null)
			return true;
		if(!existingOutput.isItemEqual(output))
			return false;
		int stackSize = existingOutput.stackSize+output.stackSize;
		return stackSize<=furnace.getInventoryStackLimit() && stackSize<=output.getMaxStackSize();
	}

	public boolean showActiveTexture()
	{
		return active || worldObj.isBlockIndirectlyGettingPowered(xCoord,yCoord,zCoord);
	}

	public void toggleSide(int side)
	{
		sockets[side] = sockets[side]==1?0:1;
		worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 0, 0);
	}
	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==1)
			this.active = arg==1;
		this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		return true;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		energyStorage.readFromNBT(nbt);
		sockets = nbt.getIntArray("sockets");
		if(sockets.length<6)
			sockets = new int[0];
		active = nbt.getBoolean("active");
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		energyStorage.writeToNBT(nbt);
		nbt.setIntArray("sockets", sockets);
		nbt.setBoolean("active", active);
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return sockets[from.ordinal()]==1;
	}
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if(sockets[from.ordinal()]==0)
			return 0;
		return energyStorage.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		return energyStorage.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		return energyStorage.getMaxEnergyStored();
	}

}
