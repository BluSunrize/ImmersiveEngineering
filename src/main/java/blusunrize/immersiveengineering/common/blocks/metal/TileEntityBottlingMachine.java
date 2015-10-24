package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockAssembler;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityBottlingMachine extends TileEntityMultiblockPart implements ISidedInventory, IEnergyReceiver, IFluidHandler
{
	public int facing = 2;
	public EnergyStorage energyStorage = new EnergyStorage(16000);
	public ItemStack[] inventory = new ItemStack[18];
	public FluidTank tank = new FluidTank(8000);


	@Override
	public TileEntityBottlingMachine master()
	{
		if(offset[0]==0&&offset[1]==0&&offset[2]==0)
			return null;
		TileEntity te = worldObj.getTileEntity(xCoord-offset[0], yCoord-offset[1], zCoord-offset[2]);
		return te instanceof TileEntityBottlingMachine?(TileEntityBottlingMachine)te : null;
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		if(pos<0)
			return null;
		ItemStack s = MultiblockAssembler.instance.getStructureManual() [pos/6][pos%6/3][pos%3];
		return s!=null?s.copy():null;
	}
	@Override
	public void updateEntity()
	{
		if(!formed || pos!=13)
			return;

		if(worldObj.isRemote)
			return;

		boolean update = false;

		if(update)
		{
			this.markDirty();
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}


	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = nbt.getInteger("facing");
		energyStorage.readFromNBT(nbt);

		tank.readFromNBT(nbt.getCompoundTag("tank"));

		NBTTagList invList = nbt.getTagList("inventory", 10);
		for (int i=0; i<invList.tagCount(); i++)
		{
			NBTTagCompound itemTag = invList.getCompoundTagAt(i);
			int slot = itemTag.getByte("Slot") & 255;
			if(slot>=0 && slot<this.inventory.length)
				this.inventory[slot] = ItemStack.loadItemStackFromNBT(itemTag);
		}
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing);
		energyStorage.writeToNBT(nbt);

		NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
		nbt.setTag("tank", tankTag);

		NBTTagList invList = new NBTTagList();
		for(int i=0; i<this.inventory.length; i++)
			if(this.inventory[i] != null)
			{
				NBTTagCompound itemTag = new NBTTagCompound();
				itemTag.setByte("Slot", (byte)i);
				this.inventory[i].writeToNBT(itemTag);
				invList.appendTag(itemTag);
			}
		nbt.setTag("inventory", invList);
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(pos==4)
			return AxisAlignedBB.getBoundingBox(xCoord-1,yCoord,zCoord-1, xCoord+2,yCoord+3,zCoord+2);
		return AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord,yCoord,zCoord);
	}
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return super.getMaxRenderDistanceSquared()*Config.getDouble("increasedTileRenderdistance");
	}
	@Override
	public float[] getBlockBounds()
	{
		if(pos<9 || pos==10||pos==13||pos==16 || pos==22)
			return new float[]{0,0,0,1,1,1};

		float xMin = 0;
		float yMin = 0;
		float zMin = 0;
		float xMax = 1;
		float yMax = 1;
		float zMax = 1;

		if((pos%9<3 && facing==2)||(pos%9>=6 && facing==3))
			zMin = .25f;
		else if((pos%9<3 && facing==3)||(pos%9>=6 && facing==2))
			zMax = .75f;
		else if((pos%9<3 && facing==4)||(pos%9>=6 && facing==5))
			xMin = .25f;
		else if((pos%9<3 && facing==5)||(pos%9>=6 && facing==4))
			xMax = .75f;

		if((pos%3==0 && facing==4)||(pos%3==2 && facing==5))
			zMin = .1875f;
		else if((pos%3==0 && facing==5)||(pos%3==2 && facing==4))
			zMax = .8125f;
		else if((pos%3==0 && facing==3)||(pos%3==2 && facing==2))
			xMin = .1875f;
		else if((pos%3==0 && facing==2)||(pos%3==2 && facing==3))
			xMax = .8125f;

		return new float[]{xMin,yMin,zMin, xMax,yMax,zMax};
	}

	@Override
	public void invalidate()
	{
		super.invalidate();

		if(formed && !worldObj.isRemote)
		{
			//			int f = facing;
			TileEntity master = master();
			if(master==null)
				master = this;

			int startX = master.xCoord;
			int startY = master.yCoord;
			int startZ = master.zCoord;

			for(int yy=-1;yy<=1;yy++)
				for(int zz=-1;zz<=1;zz++)
					for(int xx=-1;xx<=1;xx++)
					{
						ItemStack s = null;
						TileEntity te = worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz);
						if(te instanceof TileEntityBottlingMachine)
						{
							s = ((TileEntityBottlingMachine)te).getOriginalBlock();
							((TileEntityBottlingMachine)te).formed=false;
						}
						if(startX+xx==xCoord && startY+yy==yCoord && startZ+zz==zCoord)
							s = this.getOriginalBlock();
						if(s!=null && Block.getBlockFromItem(s.getItem())!=null)
						{
							if(startX+xx==xCoord && startY+yy==yCoord && startZ+zz==zCoord)
								worldObj.spawnEntityInWorld(new EntityItem(worldObj, xCoord+.5,yCoord+.5,zCoord+.5, s));
							else
							{
								if(Block.getBlockFromItem(s.getItem())==IEContent.blockMetalMultiblocks)
									worldObj.setBlockToAir(startX+xx,startY+yy,startZ+zz);
								worldObj.setBlock(startX+xx,startY+yy,startZ+zz, Block.getBlockFromItem(s.getItem()), s.getItemDamage(), 0x3);
							}
							TileEntity tile = worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz);
							if(tile instanceof TileEntityStructuralArm)
								((TileEntityStructuralArm)tile).facing = facing<4?(xx==-1?4:5):(zz==-1?2:3);
						}
					}
		}
	}

	@Override
	public int getSizeInventory()
	{
		if(!formed)
			return 0;
		return inventory.length;
	}
	@Override
	public ItemStack getStackInSlot(int slot)
	{
		if(!formed)
			return null;
		if(master()!=null)
			return master().getStackInSlot(slot);
		if(slot<inventory.length)
			return inventory[slot];
		return null;
	}
	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		if(!formed)
			return null;
		if(master()!=null)
			return master().decrStackSize(slot,amount);
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
		return stack;
	}
	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		if(!formed)
			return null;
		if(master()!=null)
			return master().getStackInSlotOnClosing(slot);
		ItemStack stack = getStackInSlot(slot);
		if (stack != null)
			setInventorySlotContents(slot, null);
		return stack;
	}
	@Override
	public void setInventorySlotContents(int slot, ItemStack stack)
	{
		if(!formed||worldObj.isRemote)
			return;
		if(master()!=null)
		{
			master().setInventorySlotContents(slot,stack);
			return;
		}
		inventory[slot] = stack;
		if (stack != null && stack.stackSize > getInventoryStackLimit())
			stack.stackSize = getInventoryStackLimit();
		this.markDirty();
	}
	@Override
	public String getInventoryName()
	{
		return "IEBottlingMachine";
	}
	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}
	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}
	@Override
	public boolean isUseableByPlayer(EntityPlayer p_70300_1_)
	{
		return true;
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
		if(!formed||stack==null)
			return true;
		if(master()!=null)
			return master().isItemValidForSlot(slot,stack);
		return true;
	}
	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if(!formed)
			return new int[0];
		if(pos==10||pos==16)
			return new int[]{0,1,2,3,4,5,6,7,8, 9,10,11,12,13,14,15,16,17};
		return new int[0];
	}
	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side)
	{
		if(!formed)
			return true;
		if(master()!=null)
			return master().canInsertItem(slot,stack,side);

		return isItemValidForSlot(slot,stack);
	}
	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side)
	{
		if(!formed)
			return true;
		if(master()!=null)
			return master().canExtractItem(slot,stack,side);
		return slot>=16&&slot<=22;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return formed && pos==22 && from==ForgeDirection.UP;
	}
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if(formed && pos==22 && from==ForgeDirection.UP && this.master()!=null)
		{
			TileEntityBottlingMachine master = master();
			int rec = master.energyStorage.receiveEnergy(maxReceive, simulate);
			master.markDirty();
			if(rec>0)
				worldObj.markBlockForUpdate(master().xCoord, master().yCoord, master().zCoord);
			return rec;
		}
		return 0;
	}
	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		if(this.master()!=null)
			return this.master().energyStorage.getEnergyStored();
		return energyStorage.getEnergyStored();
	}
	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		if(this.master()!=null)
			return this.master().energyStorage.getMaxEnergyStored();
		return energyStorage.getMaxEnergyStored();
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if(resource==null)
			return 0;
		if(master()!=null)
			return master().fill(from, resource, doFill);
		int fill = tank.fill(resource, doFill);
		if(fill>0)
		{
			markDirty();
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
		return fill<0?0:fill;
	}
	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}
	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}
	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return formed && pos==1 && from==ForgeDirection.getOrientation(facing).getOpposite();
	}
	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}
	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		if(this.master()!=null)
			return this.master().getTankInfo(from);
		return new FluidTankInfo[]{tank.getInfo()};
	}
}