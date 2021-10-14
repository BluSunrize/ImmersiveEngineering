/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IEBaseContainer<T extends BlockEntity> extends AbstractContainerMenu
{
	public T tile;
	@Nullable
	public Container inv;
	public int slotCount;

	public IEBaseContainer(MenuType<?> type, Inventory inventoryPlayer, T tile, int id)
	{
		super(type, id);
		this.tile = tile;
		if(tile instanceof IIEInventory)
			this.inv = new BlockEntityInventory(tile, this);
	}

	@Override
	public boolean stillValid(@Nonnull Player player)
	{
		return inv!=null&&inv.stillValid(player);//Override for TE's that don't implement IIEInventory
	}

	@Override
	public void clicked(int id, int dragType, ClickType clickType, Player player)
	{
		Slot slot = id < 0?null: this.slots.get(id);
		if(!(slot instanceof IESlot.ItemHandlerGhost))
		{
			super.clicked(id, dragType, clickType, player);
			return;
		}
		//Spooky Ghost Slots!!!!
		//TODO fix/test
		ItemStack stackSlot = slot.getItem();

		if(dragType==2)
			slot.set(ItemStack.EMPTY);
		else if(dragType==0||dragType==1)
		{
			ItemStack stackHeld = getCarried();
			int amount = Math.min(slot.getMaxStackSize(), stackHeld.getCount());
			if(dragType==1)
				amount = 1;
			if(stackSlot.isEmpty())
			{
				if(!stackHeld.isEmpty()&&slot.mayPlace(stackHeld))
					slot.set(ItemHandlerHelper.copyStackWithSize(stackHeld, amount));
			}
			else if(stackHeld.isEmpty())
			{
				slot.set(ItemStack.EMPTY);
			}
			else if(slot.mayPlace(stackHeld))
			{
				if(ItemStack.isSame(stackSlot, stackHeld))
					stackSlot.grow(amount);
				else
					slot.set(ItemHandlerHelper.copyStackWithSize(stackHeld, amount));
			}
			if(stackSlot.getCount() > slot.getMaxStackSize())
				stackSlot.setCount(slot.getMaxStackSize());
		}
		else if(dragType==5)
		{
			ItemStack stackHeld = getCarried();
			int amount = Math.min(slot.getMaxStackSize(), stackHeld.getCount());
			if(!slot.hasItem())
			{
				slot.set(ItemHandlerHelper.copyStackWithSize(stackHeld, amount));
			}
		}
	}

	@Nonnull
	@Override
	public ItemStack quickMoveStack(Player player, int slot)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slotObject = this.slots.get(slot);
		if(slotObject!=null&&slotObject.hasItem())
		{
			ItemStack itemstack1 = slotObject.getItem();
			itemstack = itemstack1.copy();
			if(slot < slotCount)
			{
				if(!this.moveItemStackTo(itemstack1, slotCount, this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(!this.moveItemStackToWithMayPlace(itemstack1, 0, slotCount, false))
			{
				return ItemStack.EMPTY;
			}

			if(itemstack1.isEmpty())
			{
				slotObject.set(ItemStack.EMPTY);
			}
			else
			{
				slotObject.setChanged();
			}
		}

		return itemstack;
	}

	protected boolean moveItemStackToWithMayPlace(ItemStack pStack, int pStartIndex, int pEndIndex, boolean pReverseDirection)
	{
		boolean inAllowedRange = true;
		int allowedStart = pStartIndex;
		for(int i = pStartIndex; i < pEndIndex; i++)
		{
			boolean mayplace = this.slots.get(i).mayPlace(pStack);
			if(inAllowedRange&&(!mayplace||i==pEndIndex-1))
			{
				if(moveItemStackTo(pStack, allowedStart, i, pReverseDirection))
					return true;
				inAllowedRange = false;
			}
			else if(!inAllowedRange&&mayplace)
			{
				allowedStart = i;
				inAllowedRange = true;
			}
		}
		return false;
	}

	@Override
	public void removed(Player playerIn)
	{
		super.removed(playerIn);
		if(inv!=null)
			this.inv.stopOpen(playerIn);
	}

	public void receiveMessageFromScreen(CompoundTag nbt)
	{

	}
}