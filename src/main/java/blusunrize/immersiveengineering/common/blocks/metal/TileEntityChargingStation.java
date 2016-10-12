package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxContainerItem;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import cofh.api.energy.IEnergyContainerItem;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileEntityChargingStation extends TileEntityIEBase implements ITickable, IFluxReceiver,IEnergyReceiver, IIEInventory, IDirectionalTile, IBlockBounds, IComparatorOverride, IPlayerInteraction
{
	public FluxStorageAdvanced energyStorage = new FluxStorageAdvanced(32000);
	public EnumFacing facing = EnumFacing.NORTH;
	public ItemStack[] inventory = new ItemStack[1];
	private boolean charging = true;
	public int comparatorOutput=0;

	@Override
	public void update()
	{
		if(inventory[0]!=null && (inventory[0].getItem() instanceof IFluxContainerItem || inventory[0].getItem() instanceof IEnergyContainerItem /*|| Lib.IC2 && IC2Helper.isElectricItem(inventory))*/))
		{
			if(worldObj.isRemote&&charging)
			{
				float charge = 0;
				if(inventory[0].getItem() instanceof IFluxContainerItem)
				{
					IFluxContainerItem container = (IFluxContainerItem)inventory[0].getItem();
					int max = container.getMaxEnergyStored(inventory[0]);
					if(max>0)
						charge = container.getEnergyStored(inventory[0])/(float)max;
				}
				else if(inventory[0].getItem() instanceof IEnergyContainerItem)
				{
					IEnergyContainerItem container = (IEnergyContainerItem)inventory[0].getItem();
					int max = container.getMaxEnergyStored(inventory[0]);
					if(max>0)
						charge = container.getEnergyStored(inventory[0])/(float)max;
				}
				else
				{
					//					double max = IC2Helper.getMaxItemCharge(inventory);
					//					if(max>0)
					//						charge = (float)(IC2Helper.getCurrentItemCharge(inventory)/max);
				}
				int max = 3;//charge>.66?3: charge>.33?2: 1;
				for(int i=0; i<max; i++)
				{
					long time = worldObj.getTotalWorldTime();
					if(charge>=1 || (time%12>=i*4&&time%12<=i*4+2))
					{
						int shift = i-1;
						double x = getPos().getX()+.5+(facing==EnumFacing.WEST?-.46875:facing==EnumFacing.EAST?.46875: facing==EnumFacing.NORTH?(-.1875*shift): (.1875*shift));
						double y = getPos().getY()+.25;
						double z = getPos().getZ()+.5+(facing==EnumFacing.NORTH?-.46875:facing==EnumFacing.SOUTH?.46875: facing==EnumFacing.EAST?(-.1875*shift): (.1875*shift));
						ImmersiveEngineering.proxy.spawnRedstoneFX(worldObj, x,y,z, .25,.25,.25, .5f, 1-charge,charge,0);
					}
				}
			}
			else if (charging)
			{
				if (energyStorage.getEnergyStored()==0)
				{
					charging = false;
					this.markContainingBlockForUpdate(null);
					return;
				}
				if(inventory[0].getItem() instanceof IFluxContainerItem)
				{
					IFluxContainerItem container = (IFluxContainerItem)inventory[0].getItem();
					int max = container.getMaxEnergyStored(inventory[0]);
					int space = max-container.getEnergyStored(inventory[0]);
					if(max>0 && space>0)
					{
						int energyDec = (10*container.getEnergyStored(inventory[0]))/max;
						int insert = Math.min(space, Math.max(energyStorage.getAverageInsertion(), IEConfig.Machines.charger_consumption));
						int accepted = Math.min(container.receiveEnergy(inventory[0], insert, true), this.energyStorage.extractEnergy(insert, true));
						if((accepted=this.energyStorage.extractEnergy(accepted, false))>0)
							container.receiveEnergy(inventory[0], accepted, false);
						int energyDecNew = (10*container.getEnergyStored(inventory[0]))/max;
						if (energyDec!=energyDecNew)
							this.markContainingBlockForUpdate(null);
					}
				}
				else if(inventory[0].getItem() instanceof IEnergyContainerItem)
				{
					IEnergyContainerItem container = (IEnergyContainerItem)inventory[0].getItem();
					int max = container.getMaxEnergyStored(inventory[0]);
					int space = max-container.getEnergyStored(inventory[0]);
					if(max>0 && space>0)
					{
						int energyDec = (10*container.getEnergyStored(inventory[0]))/max;
						int insert = Math.min(space, Math.max(energyStorage.getAverageInsertion(), IEConfig.Machines.charger_consumption));
						int accepted = Math.min(container.receiveEnergy(inventory[0], insert, true), this.energyStorage.extractEnergy(insert, true));
						if((accepted=this.energyStorage.extractEnergy(accepted, false))>0)
							container.receiveEnergy(inventory[0], accepted, false);
						int energyDecNew = (10*container.getEnergyStored(inventory[0]))/max;
						if (energyDec!=energyDecNew)
							this.markContainingBlockForUpdate(null);
					}
				}
				else
				{
					//					double max = IC2Helper.getMaxItemCharge(inventory);
					//					double space = max-IC2Helper.getCurrentItemCharge(inventory);
					//					if(max>0 && space>0)
					//					{
					//						int energyDec = (int) ((10*IC2Helper.getCurrentItemCharge(inventory))/max);
					//						double insert = Math.min(space, ModCompatability.convertRFtoEU(Config.getInt("charger_consumption"),5));
					//						double accepted = IC2Helper.chargeItem(inventory, insert, true);
					//						int rfAccepted = this.energyStorage.extractEnergy(ModCompatability.convertEUtoRF(accepted), false);
					//						if(rfAccepted>0)
					//							IC2Helper.chargeItem(inventory, ModCompatability.convertRFtoEU(rfAccepted,5), false);
					//						int energyDecNew = (int) ((10*IC2Helper.getCurrentItemCharge(inventory))/max);
					//						if (energyDec!=energyDecNew)
					//							worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
					//					}
				}
			}
			else if (energyStorage.getEnergyStored()>=energyStorage.getMaxEnergyStored()*.95)
			{
				charging = true;
				this.markContainingBlockForUpdate(null);
			}
		}


		if(!worldObj.isRemote && worldObj.getTotalWorldTime()%32==((getPos().getX()^getPos().getZ())&31))
		{
			float charge = 0;
			if(inventory[0]!=null && (inventory[0].getItem() instanceof IFluxContainerItem || inventory[0].getItem() instanceof IEnergyContainerItem /*|| (Lib.IC2 && IC2Helper.isElectricItem(inventory))*/))
				if(inventory[0].getItem() instanceof IFluxContainerItem)
				{
					IFluxContainerItem container = (IFluxContainerItem)inventory[0].getItem();
					charge = container.getEnergyStored(inventory[0])/(float)container.getMaxEnergyStored(inventory[0]);
				}
				else if(inventory[0].getItem() instanceof IEnergyContainerItem)
				{
					IEnergyContainerItem container = (IEnergyContainerItem)inventory[0].getItem();
					charge = container.getEnergyStored(inventory[0])/(float)container.getMaxEnergyStored(inventory[0]);
				}
			//				else
			//					charge = (float)(IC2Helper.getCurrentItemCharge(inventory)/IC2Helper.getMaxItemCharge(inventory));
			int i = (int)(15*charge);
			if(i!=this.comparatorOutput)
			{
				this.comparatorOutput=i;
				worldObj.notifyNeighborsOfStateChange(getPos(), getBlockType());
			}
		}
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		energyStorage.readFromNBT(nbt);
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
		inventory[0] = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("inventory"));
		charging = nbt.getBoolean("charging");
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		energyStorage.writeToNBT(nbt);
		nbt.setInteger("facing", facing.ordinal());
		nbt.setBoolean("charging", charging);
		if(inventory[0]!=null)
			nbt.setTag("inventory", inventory[0].writeToNBT(new NBTTagCompound()));
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==0)
		{
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from)
	{
		return from==null || from==EnumFacing.DOWN || (from!=null&&from.getOpposite()==facing);
	}
	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate)
	{
		return energyStorage.receiveEnergy(maxReceive, simulate);
	}
	@Override
	public int getEnergyStored(EnumFacing from)
	{
		return energyStorage.getEnergyStored();
	}
	@Override
	public int getMaxEnergyStored(EnumFacing from)
	{
		return energyStorage.getMaxEnergyStored();
	}

	@Override
	public int getComparatorInputOverride()
	{
		return this.comparatorOutput;
	}

	@Override
	public EnumFacing getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 2;
	}
	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return true;
	}
	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return true;
	}

	@Override
	public float[] getBlockBounds()
	{
		return new float[]{facing.getAxis()==Axis.X?0:.125f,0,facing.getAxis()==Axis.Z?0:.125f, facing.getAxis()==Axis.X?1:.875f,1,facing.getAxis()==Axis.Z?1:.875f};
	}

	@Override
	public ItemStack[] getInventory()
	{
		return inventory;
	}
	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return stack!=null && (stack.getItem() instanceof IFluxContainerItem || stack.getItem() instanceof IEnergyContainerItem);
	}
	@Override
	public int getSlotLimit(int slot)
	{
		return 1;
	}
	@Override
	public void doGraphicalUpdates(int slot)
	{
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return true;
		return super.hasCapability(capability, facing);
	}
	IItemHandler insertionHandler = new IEInventoryHandler(1,this);
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return (T)insertionHandler;
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(heldItem!=null && (heldItem.getItem() instanceof IFluxContainerItem || heldItem.getItem() instanceof IEnergyContainerItem /*|| (Lib.IC2 && IC2Helper.isElectricItem(equipped))*/))
		{
			ItemStack stored = inventory[0]!=null?inventory[0].copy():null;
			inventory[0] = heldItem.copy();
			player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, stored);
			markDirty();
			this.markContainingBlockForUpdate(null);
			return true;
		}
		else if(inventory[0]!=null)
		{
			if(!worldObj.isRemote)
				player.entityDropItem(inventory[0].copy(), .5f);
			inventory[0] = null;
			markDirty();
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}

	//	@Override
	//	public int getSizeInventory()
	//	{
	//		return 1;
	//	}
	//	@Override
	//	public ItemStack getStackInSlot(int slot)
	//	{
	//		return inventory;
	//	}
	//	@Override
	//	public ItemStack decrStackSize(int slot, int amount)
	//	{
	//		ItemStack stack = getStackInSlot(slot);
	//		if(stack != null)
	//			if(stack.stackSize <= amount)
	//				setInventorySlotContents(slot, null);
	//			else
	//			{
	//				stack = stack.splitStack(amount);
	//				if(stack.stackSize == 0)
	//					setInventorySlotContents(slot, null);
	//			}
	//		this.markDirty();
	//		worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 0, 0);
	//		return stack;
	//	}
	//	@Override
	//	public ItemStack getStackInSlotOnClosing(int slot)
	//	{
	//		ItemStack stack = getStackInSlot(slot);
	//		if (stack != null)
	//			setInventorySlotContents(slot, null);
	//		return stack;
	//	}
	//	@Override
	//	public void setInventorySlotContents(int slot, ItemStack stack)
	//	{
	//		inventory = stack;
	//		if(stack != null && stack.stackSize > getInventoryStackLimit())
	//			stack.stackSize = getInventoryStackLimit();
	//		this.markDirty();
	//		worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 0, 0);
	//	}
	//	@Override
	//	public String getInventoryName()
	//	{
	//		return "IEChargingStation";
	//	}
	//	@Override
	//	public boolean hasCustomInventoryName()
	//	{
	//		return false;
	//	}
	//	@Override
	//	public int getInventoryStackLimit()
	//	{
	//		return 1;
	//	}
	//	@Override
	//	public boolean isUseableByPlayer(EntityPlayer player)
	//	{
	//		return worldObj.getTileEntity(xCoord,yCoord,zCoord)!=this?false:player.getDistanceSq(xCoord+.5D,yCoord+.5D,zCoord+.5D)<=64;
	//	}
	//	@Override
	//	public void openInventory()
	//	{
	//	}
	//	@Override
	//	public void closeInventory()
	//	{
	//	}
	//	@Override
	//	public boolean isItemValidForSlot(int slot, ItemStack stack)
	//	{
	//		return stack!=null && stack.getItem() instanceof IEnergyContainerItem;
	//	}
}
