package blusunrize.immersiveengineering.common.blocks.stone;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.api.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Utils;

public class TileEntityBlastFurnace extends TileEntityIEBase implements ISidedInventory
{
	ItemStack[] inventory = new ItemStack[3];
	public int facing = 2;
	public boolean formed = false;
	public int process = 0;
	public int processMax = 0;
	public boolean active = false;
	public int burnTime = 0;
	public int lastBurnTime = 0;

	int[] offset = {0,0,0};
	public TileEntityBlastFurnace master()
	{
		if(offset[0]==0&&offset[1]==0&&offset[2]==0)
			return null;
		TileEntity te = worldObj.getTileEntity(xCoord-offset[0], yCoord-offset[1], zCoord-offset[2]);
		return te instanceof TileEntityBlastFurnace?(TileEntityBlastFurnace)te : null;
	}

	public static boolean _Immovable()
	{
		return true;
	}
	
	@Override
	public void updateEntity()
	{
//		if(formed&&master()==null)
//			System.out.println(worldObj+" "+processMax);
		if(!worldObj.isRemote&&formed&&master()==null)
		{
			boolean a = active;
			if(burnTime<=10 && getRecipe()!=null)
			{
				if(BlastFurnaceRecipe.isValidBlastFuel(inventory[1]))
				{
					burnTime += BlastFurnaceRecipe.getBlastFuelTime(inventory[1]);
					lastBurnTime = BlastFurnaceRecipe.getBlastFuelTime(inventory[1]);
					this.decrStackSize(1, 1);
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				}
			}

			if(burnTime>0)
			{			
				if(process>0)
				{
					if(inventory[0]==null)
					{
						process=0;
						processMax=0;
					}
					else
					{
						process--;
						if(!active)
							active=true;
					}
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				}
				else
				{
					if(active)
					{
						BlastFurnaceRecipe recipe = getRecipe();
						if(recipe!=null)
						{
							this.decrStackSize(0, 1);
							if(inventory[2]!=null)
								inventory[2].stackSize+=recipe.output.copy().stackSize;
							else
								inventory[2] = recipe.output.copy();
						}
						processMax=0;
						active=false;
					}
					else
					{
						BlastFurnaceRecipe recipe = getRecipe();
						if(recipe!=null)
						{
							this.process=recipe.time;
							this.processMax=process;
							this.active=true;
						}
					}
				}
				burnTime--;
			}
			else
			{
				if(active)
					active=false;
			}
			if(a!=active)
			{
				this.markDirty();
				int xMin= facing==5?-2: facing==4?0:-1;
				int xMax= facing==5? 0: facing==4?2: 1;
				int zMin= facing==3?-2: facing==2?0:-1;
				int zMax= facing==3? 0: facing==2?2: 1;
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				worldObj.markBlockRangeForRenderUpdate(xCoord+xMin,yCoord-1,zCoord+zMin, xCoord+xMax,yCoord+1,zCoord+zMax);
			}
		}
	}
	public BlastFurnaceRecipe getRecipe()
	{
		BlastFurnaceRecipe recipe = BlastFurnaceRecipe.findRecipe(inventory[0]);
		if(recipe==null)
			return null;
		if(inventory[2]==null || (OreDictionary.itemMatches(inventory[2],recipe.output,true) && inventory[2].stackSize+recipe.output.stackSize<getInventoryStackLimit()) )
			return recipe;
		return null;
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
		if(!formed)
			return;
		if(master()!=null)
		{
			master().setInventorySlotContents(slot,stack);
			return;
		}
		inventory[slot] = stack;
		if (stack != null && stack.stackSize > getInventoryStackLimit())
			stack.stackSize = getInventoryStackLimit();
	}

	@Override
	public String getInventoryName()
	{
		return "IECokeOven";
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
		if(!formed)
			return false;
		if(master()!=null)
			return master().isItemValidForSlot(slot,stack);
		if(slot==0)
			return stack!=null;
		if(slot==1)
			return Utils.compareToOreName(stack, "");

		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if(!formed)
			return new int[0];
		if(master()!=null)
			return master().getAccessibleSlotsFromSide(side);
		return new int[]{0,1,2};
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side)
	{
		if(!formed)
			return false;
		if(master()!=null)
			return master().canInsertItem(slot,stack,side);
		return (slot==0||slot==1) && isItemValidForSlot(slot,stack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side)
	{
		if(!formed)
			return false;
		if(master()!=null)
			return master().canExtractItem(slot,stack,side);
		return slot==2;
	}



	@Override
	public void readCustomNBT(NBTTagCompound nbt)
	{
		facing = nbt.getInteger("facing");
		offset = nbt.getIntArray("offset");
		formed = nbt.getBoolean("formed");
		process = nbt.getInteger("process");
		processMax = nbt.getInteger("processMax");
		active = nbt.getBoolean("active");
		burnTime = nbt.getInteger("burnTime");
		lastBurnTime = nbt.getInteger("lastBurnTime");
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
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
	public void writeCustomNBT(NBTTagCompound nbt)
	{
		nbt.setInteger("facing", facing);
		nbt.setIntArray("offset",offset);
		nbt.setBoolean("formed", formed);
		nbt.setInteger("process", process);
		nbt.setInteger("processMax", processMax);
		nbt.setBoolean("active", active);
		nbt.setInteger("burnTime", burnTime);
		nbt.setInteger("lastBurnTime", lastBurnTime);
	}
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
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
}