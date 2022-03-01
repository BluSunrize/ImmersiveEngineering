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
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class TileInventory implements Container
{
	final BlockEntity tile;
	final IIEInventory inv;
	final String name;
	final AbstractContainerMenu eventHandler;

	public TileInventory(BlockEntity tile, AbstractContainerMenu eventHandler)
	{
		this.tile = tile;
		this.inv = (IIEInventory)tile;
		this.eventHandler = eventHandler;
		String name = tile.getClass().getName();
		this.name = "IE"+(name.substring(name.lastIndexOf("TileEntity")+"TileEntity".length()));
	}

	@Override
	public int getContainerSize()
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
	public ItemStack getItem(int index)
	{
		return inv.getInventory().get(index);
	}

	@Override
	public ItemStack removeItem(int index, int count)
	{
		ItemStack stack = inv.getInventory().get(index);
		if(!stack.isEmpty())
		{
			if(stack.getCount() <= count)
				inv.getInventory().set(index, ItemStack.EMPTY);
			else
			{
				stack = stack.split(count);
				if(stack.getCount()==0)
					inv.getInventory().set(index, ItemStack.EMPTY);
			}
			eventHandler.slotsChanged(this);
		}
		return stack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int index)
	{
		ItemStack ret = inv.getInventory().get(index).copy();
		inv.getInventory().set(index, ItemStack.EMPTY);
		return ret;
	}

	@Override
	public void setItem(int index, ItemStack stack)
	{
		inv.getInventory().set(index, stack);
		eventHandler.slotsChanged(this);
	}

	@Override
	public int getMaxStackSize()
	{
		return 64;
	}

	@Override
	public void setChanged()
	{
		tile.setChanged();
	}

	@Override
	public boolean stillValid(Player player)
	{
		return isValidForPlayer(tile, player);
	}

	public static boolean isValidForPlayer(BlockEntity tile, Player player)
	{
		if(tile instanceof IInteractionObjectIE&&!((IInteractionObjectIE)tile).canUseGui(player))
			return false;
		return !tile.isRemoved()&&Vec3.atCenterOf(tile.getBlockPos()).distanceToSqr(player.position()) < 64;
	}

	@Override
	public void startOpen(Player player)
	{
	}

	@Override
	public void stopOpen(Player player)
	{
		inv.doGraphicalUpdates();
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack)
	{
		return inv.isStackValid(index, stack);
	}

	@Override
	public void clearContent()
	{
		for(int i = 0; i < inv.getInventory().size(); i++)
			inv.getInventory().set(i, ItemStack.EMPTY);
	}

}