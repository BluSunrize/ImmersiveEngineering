package blusunrize.immersiveengineering.common.blocks.wooden;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Utils;

public class TileEntityWoodenCrate extends TileEntityIEBase implements IInventory
{
	ItemStack[] inventory = new ItemStack[27];
	@Override
	public int getSizeInventory()
	{
		return inventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		if(slot<inventory.length)
			return inventory[slot];
		return null;
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
		inventory[slot] = stack;
		if (stack != null && stack.stackSize > getInventoryStackLimit())
			stack.stackSize = getInventoryStackLimit();
	}

	@Override
	public String getInventoryName()
	{
		return "IEWoodenCrate";
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
		return !OreDictionary.itemMatches(new ItemStack(IEContent.blockWoodenDevice,1,4), stack, true);
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		if(!descPacket)
		{
			inventory = Utils.readInventory(nbt.getTagList("inventory", 10), 27);
		}
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		if(!descPacket)
		{
			writeInv(nbt, false);
		}
	}
	public void writeInv(NBTTagCompound nbt, boolean toItem)
	{
		boolean write = false;
		NBTTagList invList = new NBTTagList();
		for(int i=0; i<this.inventory.length; i++)
			if(this.inventory[i] != null)
			{
				if(toItem)
					write = true;
				NBTTagCompound itemTag = new NBTTagCompound();
				itemTag.setByte("Slot", (byte)i);
				this.inventory[i].writeToNBT(itemTag);
				invList.appendTag(itemTag);
			}
		if(!toItem || write)
			nbt.setTag("inventory", invList);
	}

}