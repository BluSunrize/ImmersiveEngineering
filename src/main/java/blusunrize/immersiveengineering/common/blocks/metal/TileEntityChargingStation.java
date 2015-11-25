package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.compat.IC2Helper;
import blusunrize.immersiveengineering.common.util.compat.ModCompatability;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyContainerItem;
import cofh.api.energy.IEnergyReceiver;

public class TileEntityChargingStation extends TileEntityIEBase implements IEnergyReceiver, IInventory
{
	public EnergyStorage energyStorage = new EnergyStorage(32000,Math.max(1024, Config.getInt("heater_consumption")));
	public int facing = 2;
	public ItemStack inventory;

	@Override
	public void updateEntity()
	{
		if(inventory!=null && (inventory.getItem() instanceof IEnergyContainerItem || (Lib.IC2 && IC2Helper.isElectricItem(inventory))))
		{
			if(worldObj.isRemote)
			{
				float charge = 0;
				if(inventory.getItem() instanceof IEnergyContainerItem)
				{
					IEnergyContainerItem container = (IEnergyContainerItem)inventory.getItem();
					charge = container.getEnergyStored(inventory)/(float)container.getMaxEnergyStored(inventory);
				}
				else
					charge = (float)(IC2Helper.getCurrentItemCharge(inventory)/IC2Helper.getMaxItemCharge(inventory));
				int max = 3;//charge>.66?3: charge>.33?2: 1;
				for(int i=0; i<max; i++)
				{
					long time = worldObj.getTotalWorldTime();
					if(charge>=1 || (time%12>=i*4&&time%12<=i*4+2))
					{
						int shift = i-1;
						double x = xCoord+.5+(facing==4?-.46875:facing==5?.46875: facing==2?(-.1875*shift): (.1875*shift));
						double y = yCoord+.25;
						double z = zCoord+.5+(facing==2?-.46875:facing==3?.46875: facing==5?(-.1875*shift): (.1875*shift));
						ImmersiveEngineering.proxy.spawnRedstoneFX(worldObj, x,y,z, .25,.25,.25, .5f, 1-charge,charge,0);
					}
				}
			}
			else
			{
				if(inventory.getItem() instanceof IEnergyContainerItem)
				{
					IEnergyContainerItem container = (IEnergyContainerItem)inventory.getItem();
					int space = container.getMaxEnergyStored(inventory)-container.getEnergyStored(inventory);
					if(space>0)
					{
						int insert = Math.min(space, Config.getInt("charger_consumption"));
						int accepted = Math.min(container.receiveEnergy(inventory, insert, true), this.energyStorage.extractEnergy(insert, true));
						if((accepted=this.energyStorage.extractEnergy(accepted, false))>0)
							container.receiveEnergy(inventory, accepted, false);
					}
				}
				else
				{
					double space = IC2Helper.getMaxItemCharge(inventory)-IC2Helper.getCurrentItemCharge(inventory);
					if(space>0)
					{
						double insert = Math.min(space, ModCompatability.convertRFtoEU(Config.getInt("charger_consumption"),5));
						double accepted = IC2Helper.chargeItem(inventory, insert, true);
						int rfAccepted = this.energyStorage.extractEnergy(ModCompatability.convertEUtoRF(accepted), false);
						if(rfAccepted>0)
							IC2Helper.chargeItem(inventory, ModCompatability.convertRFtoEU(rfAccepted,5), false);
					}
				}
			}
		}
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		energyStorage.readFromNBT(nbt);
		facing = nbt.getInteger("facing");
		inventory = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("inventory"));
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		energyStorage.writeToNBT(nbt);
		nbt.setInteger("facing", facing);
		if(inventory!=null)
			nbt.setTag("inventory", inventory.writeToNBT(new NBTTagCompound()));
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

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return from==ForgeDirection.DOWN || (from!=null&&from.getOpposite().ordinal()==facing);
	}
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
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

	@Override
	public int getSizeInventory()
	{
		return 1;
	}
	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return inventory;
	}
	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		ItemStack stack = getStackInSlot(slot);
		if(stack != null)
			if(stack.stackSize <= amount)
				setInventorySlotContents(slot, null);
			else
			{
				stack = stack.splitStack(amount);
				if(stack.stackSize == 0)
					setInventorySlotContents(slot, null);
			}
		this.markDirty();
		worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 0, 0);
		return stack;
	}
	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		ItemStack stack = getStackInSlot(slot);
		if (stack != null)
			setInventorySlotContents(slot, null);
		return stack;
	}
	@Override
	public void setInventorySlotContents(int slot, ItemStack stack)
	{
		inventory = stack;
		if(stack != null && stack.stackSize > getInventoryStackLimit())
			stack.stackSize = getInventoryStackLimit();
		this.markDirty();
		worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 0, 0);
	}
	@Override
	public String getInventoryName()
	{
		return "IEChargingStation";
	}
	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}
	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}
	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return worldObj.getTileEntity(xCoord,yCoord,zCoord)!=this?false:player.getDistanceSq(xCoord+.5D,yCoord+.5D,zCoord+.5D)<=64;
	}
	@Override
	public void openInventory()
	{
	}
	@Override
	public void closeInventory()
	{
	}
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack)
	{
		return stack!=null && stack.getItem() instanceof IEnergyContainerItem;
	}
}
