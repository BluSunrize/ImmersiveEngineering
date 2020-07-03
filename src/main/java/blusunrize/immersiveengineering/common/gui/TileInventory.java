/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.vector.Vector3d;

public class TileInventory implements IInventory
{
	TileEntity tile;
	IIEInventory inv;
	String name;

	public TileInventory(TileEntity tile)
	{
		this.tile = tile;
		this.inv = (IIEInventory)tile;
		this.name = tile.getClass().getName();
		this.name = "IE"+(name.substring(name.lastIndexOf("TileEntity")+"TileEntity".length()));
	}

	@Override
	public int getSizeInventory()
	{
		return inv.getInventory().size();
	}

	@Override
	public boolean isEmpty()
	{
		for(ItemStack stack : inv.getInventory())
		{
			if(!stack.isEmpty())
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getStackInSlot(int index)
	{
		return inv.getInventory().get(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count)
	{
		ItemStack stack = inv.getInventory().get(index);
		if(!stack.isEmpty())
			if(stack.getCount() <= count)
				inv.getInventory().set(index, ItemStack.EMPTY);
			else
			{
				stack = stack.split(count);
				if(stack.getCount()==0)
					inv.getInventory().set(index, ItemStack.EMPTY);
			}
		return stack;
	}

	@Override
	public ItemStack removeStackFromSlot(int index)
	{
		ItemStack ret = inv.getInventory().get(index).copy();
		inv.getInventory().set(index, ItemStack.EMPTY);
		return ret;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack)
	{
		inv.getInventory().set(index, stack);
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
	public boolean isUsableByPlayer(PlayerEntity player)
	{
		if(tile instanceof IInteractionObjectIE&&!((IInteractionObjectIE)tile).canUseGui(player))
			return false;
		return !tile.isRemoved()&&Vector3d.func_237491_b_(tile.getPos()).squareDistanceTo(player.getPositionVec()) < 64;
	}

	@Override
	public void openInventory(PlayerEntity player)
	{
	}

	@Override
	public void closeInventory(PlayerEntity player)
	{
		for(int i = 0; i < getSizeInventory(); i++)
			inv.doGraphicalUpdates(i);
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack)
	{
		return inv.isStackValid(index, stack);
	}

	@Override
	public void clear()
	{
		for(int i = 0; i < inv.getInventory().size(); i++)
			inv.getInventory().set(i, ItemStack.EMPTY);
	}

}