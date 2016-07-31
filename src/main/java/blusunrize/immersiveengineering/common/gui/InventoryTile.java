package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.ITextComponent;

public class InventoryTile implements IInventory
{
	TileEntity tile;
	IIEInventory inv;
	String name;

	public InventoryTile(TileEntity tile)
	{
		this.tile = tile;
		this.inv = (IIEInventory)tile;
		this.name = tile.getClass().getName();
		this.name = "IE"+(name.substring(name.lastIndexOf("TileEntity")+"TileEntity".length()));
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public boolean hasCustomName()
	{
		return false;
	}
	@Override
	public ITextComponent getDisplayName()
	{
		return new TextComponentString(this.name);
	}

	@Override
	public int getSizeInventory()
	{
		return inv.getInventory().length;
	}

	@Override
	public ItemStack getStackInSlot(int index)
	{
		return inv.getInventory()[index];
	}

	@Override
	public ItemStack decrStackSize(int index, int count)
	{
		ItemStack stack = inv.getInventory()[index];
		if(stack != null)
			if(stack.stackSize<=count)
				inv.getInventory()[index] = null;
			else
			{
				stack = stack.splitStack(count);
				if(stack.stackSize==0)
					inv.getInventory()[index] = null;
			}
		return stack;
	}

	@Override
	public ItemStack removeStackFromSlot(int index)
	{
		ItemStack ret = inv.getInventory()[index].copy();
		inv.getInventory()[index] = null;
		return ret;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack)
	{
		inv.getInventory()[index] = stack;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public void markDirty()
	{
		tile.markDirty();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return !tile.isInvalid() && tile.getDistanceSq(player.posX, player.posY, player.posZ)<64;
	}

	@Override
	public void openInventory(EntityPlayer player)
	{
	}

	@Override
	public void closeInventory(EntityPlayer player)
	{
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack)
	{
		return inv.isStackValid(index, stack);
	}

	@Override
	public int getField(int id)
	{
		return 0;
	}

	@Override
	public void setField(int id, int value)
	{
	}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public void clear()
	{
		for(int i=0; i<inv.getInventory().length; i++)
			inv.getInventory()[i] = null;
	}

}