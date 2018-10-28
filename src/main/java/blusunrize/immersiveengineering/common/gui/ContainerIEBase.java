/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ContainerIEBase<T extends TileEntity> extends Container
{
	public T tile;
	@Nullable
	public IInventory inv;
	public int slotCount;

	public ContainerIEBase(InventoryPlayer inventoryPlayer, T tile)
	{
		this.tile = tile;
		if(tile instanceof IIEInventory)
			this.inv = new InventoryTile(tile);
	}

	@Override
	public boolean canInteractWith(@Nonnull EntityPlayer player)
	{
		return inv!=null&&inv.isUsableByPlayer(player);//Override for TE's that don't implement IIEInventory
	}

	@Nonnull
	@Override
	public ItemStack slotClick(int id, int button, ClickType clickType, EntityPlayer player)
	{
		Slot slot = id < 0?null: this.inventorySlots.get(id);
		if(!(slot instanceof IESlot.Ghost))
			return super.slotClick(id, button, clickType, player);
		//Spooky Ghost Slots!!!!
		ItemStack stack = ItemStack.EMPTY;
		ItemStack stackSlot = slot.getStack();
		if(!stackSlot.isEmpty())
			stack = stackSlot.copy();

		if(button==2)
			slot.putStack(ItemStack.EMPTY);
		else if(button==0||button==1)
		{
			InventoryPlayer playerInv = player.inventory;
			ItemStack stackHeld = playerInv.getItemStack();
			if(stackSlot.isEmpty())
			{
				if(!stackHeld.isEmpty()&&slot.isItemValid(stackHeld))
				{
					slot.putStack(Utils.copyStackWithAmount(stackHeld, 1));
				}
			}
			else if(stackHeld.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else if(slot.isItemValid(stackHeld))
			{
				slot.putStack(Utils.copyStackWithAmount(stackHeld, 1));
			}
		}
		else if(button==5)
		{
			InventoryPlayer playerInv = player.inventory;
			ItemStack stackHeld = playerInv.getItemStack();
			if(!slot.getHasStack())
			{
				slot.putStack(Utils.copyStackWithAmount(stackHeld, 1));
			}
		}
		return stack;
	}

	@Nonnull
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slotObject = this.inventorySlots.get(slot);
		if(slotObject!=null&&slotObject.getHasStack())
		{
			ItemStack itemstack1 = slotObject.getStack();
			itemstack = itemstack1.copy();
			if(slot < slotCount)
			{
				if(!this.mergeItemStack(itemstack1, slotCount, this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(!this.mergeItemStack(itemstack1, 0, slotCount, false))
			{
				return ItemStack.EMPTY;
			}

			if(itemstack1.isEmpty())
			{
				slotObject.putStack(ItemStack.EMPTY);
			}
			else
			{
				slotObject.onSlotChanged();
			}
		}

		return itemstack;
//		ItemStack stack = ItemStack.EMPTY;
//		Slot slotObject = inventorySlots.get(slot);
//
//		if(slotObject!=null&&slotObject.getHasStack())
//		{
//			ItemStack stackInSlot = slotObject.getStack();
//			stack = stackInSlot.copy();
//
//			if(slot < slotCount)
//			{
//				if(!this.mergeItemStack(stackInSlot, slotCount, (slotCount+36), true))
//					return ItemStack.EMPTY;
//			}
//			else
//			{
//				boolean b = false;
//				for(int i = 0; i < slotCount; i++)
//					if(this.getSlot(i).isItemValid(stackInSlot))
//						if(this.mergeItemStack(stackInSlot, i, i+1, false))
//						{
//							b = true;
//							break;
//						}
//				if(!b)
//					return ItemStack.EMPTY;
//			}
//
//			if(stackInSlot.getCount()==0)
//				slotObject.putStack(ItemStack.EMPTY);
//			else
//				slotObject.onSlotChanged();
//
//			if(stackInSlot.getCount()==stack.getCount())
//				return ItemStack.EMPTY;
//			slotObject.onTake(player, stackInSlot);
//		}
//		return stack;
	}

	@Override
	protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection)
	{
		return super.mergeItemStack(stack, startIndex, endIndex, reverseDirection);
//		boolean flag = false;
//		int i = startIndex;
//
//		if(reverseDirection)
//			i = endIndex-1;
//
//		if(stack.isStackable())
//		{
//			while(!stack.isEmpty()&&(!reverseDirection&&i < endIndex||reverseDirection&&i >= startIndex))
//			{
//				Slot slot = this.inventorySlots.get(i);
//				ItemStack stackInSlot = slot.getStack();
//
//				if(!stackInSlot.isEmpty()&&areItemStacksEqual(stack, stackInSlot))
//				{
//					int j = stackInSlot.getCount()+stack.getCount();
//					int maxSize = Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());
//					if(j <= maxSize)
//					{
//						stack.setCount(0);
//						stackInSlot.setCount(j);
//						slot.onSlotChanged();
//						flag = true;
//					}
//					else if(stackInSlot.getCount() < maxSize)
//					{
//						stack.shrink(maxSize-stackInSlot.getCount());
//						stackInSlot.setCount(maxSize);
//						slot.onSlotChanged();
//					}
//				}
//
//				if(reverseDirection)
//					--i;
//				else
//					++i;
//			}
//		}
//
//		if(!stack.isEmpty())
//		{
//			if(reverseDirection)
//				i = endIndex-1;
//			else
//				i = startIndex;
//
//			while(!reverseDirection&&i < endIndex||reverseDirection&&i >= startIndex)
//			{
//				Slot slot = this.inventorySlots.get(i);
//				ItemStack stackInSlot = slot.getStack();
//				if(stackInSlot.isEmpty()&&slot.isItemValid(stack))
//				{
//					int maxSize = Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());
//					if(stack.getCount() <= maxSize)
//					{
//						slot.putStack(stack.copy());
//						slot.onSlotChanged();
//						stack.setCount(0);
//						flag = true;
//						break;
//					}
//					else
//					{
//						slot.putStack(Utils.copyStackWithAmount(stack, maxSize));
//						slot.onSlotChanged();
//						stack.shrink(maxSize);
//					}
//				}
//				if(reverseDirection)
//					--i;
//				else
//					++i;
//			}
//		}
//		return flag;
	}

	@Override
	public void onContainerClosed(EntityPlayer playerIn)
	{
		super.onContainerClosed(playerIn);
		if(inv!=null)
			this.inv.closeInventory(playerIn);
	}
}