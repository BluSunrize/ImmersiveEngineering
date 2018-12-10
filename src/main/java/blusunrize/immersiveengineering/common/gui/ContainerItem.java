/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public abstract class ContainerItem extends Container
{
	protected final InventoryPlayer inventoryPlayer;
	protected final World world;
	protected int blockedSlot;
	protected final EntityEquipmentSlot equipmentSlot;
	protected final ItemStack heldItem;
	protected final EntityPlayer player;
	public int internalSlots;

	public ContainerItem(InventoryPlayer inventoryPlayer, World world, EntityEquipmentSlot entityEquipmentSlot, ItemStack heldItem)
	{
		this.inventoryPlayer = inventoryPlayer;
		this.world = world;
		this.player = inventoryPlayer.player;
		this.equipmentSlot = entityEquipmentSlot;
		this.heldItem = heldItem.copy();
		updateSlots();
	}

	protected void updateSlots(){
		this.internalSlots = this.addSlots();
		this.blockedSlot = (this.inventoryPlayer.currentItem+27+internalSlots);
	}

	abstract int addSlots();

	public EntityEquipmentSlot getEquipmentSlot()
	{
		return equipmentSlot;
	}

	@Nonnull
	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slot)
	{
		ItemStack oldStackInSlot = ItemStack.EMPTY;
		Slot slotObject = inventorySlots.get(slot);

		if(slotObject!=null&&slotObject.getHasStack())
		{
			ItemStack stackInSlot = slotObject.getStack();
			oldStackInSlot = stackInSlot.copy();

			if(slot < internalSlots)
			{
				if(!this.mergeItemStack(stackInSlot, internalSlots, (internalSlots+36), true))
					return ItemStack.EMPTY;
			}
			else if(allowShiftclicking()&&!stackInSlot.isEmpty())
			{
				boolean b = true;
				for(int i = 0; i < internalSlots; i++)
				{
					Slot s = inventorySlots.get(i);
					if(s!=null&&s.isItemValid(stackInSlot))
					{
						if(!s.getStack().isEmpty()&&(!ItemStack.areItemsEqual(stackInSlot, s.getStack())||!Utils.compareItemNBT(stackInSlot, s.getStack())))
							continue;
						int space = Math.min(s.getItemStackLimit(stackInSlot), stackInSlot.getMaxStackSize());
						if(!s.getStack().isEmpty())
							space -= s.getStack().getCount();
						if(space <= 0)
							continue;
						ItemStack insert = stackInSlot;
						if(space < stackInSlot.getCount())
							insert = stackInSlot.splitStack(space);
						if(this.mergeItemStack(insert, i, i+1, true))
						{
							b = false;
						}
					}
				}
				if(b)
					return ItemStack.EMPTY;
			}

			if(stackInSlot.getCount()==0)
				slotObject.putStack(ItemStack.EMPTY);
			else
				slotObject.onSlotChanged();

			slotObject.inventory.markDirty();
			if(stackInSlot.getCount()==oldStackInSlot.getCount())
				return ItemStack.EMPTY;
			slotObject.onTake(player, oldStackInSlot);

			updatePlayerItem();
			detectAndSendChanges();
		}
		return oldStackInSlot;
	}

	protected boolean allowShiftclicking()
	{
		return true;
	}

	@Override
	public boolean canInteractWith(@Nonnull EntityPlayer entityplayer)
	{
		return ItemStack.areItemsEqual(player.getItemStackFromSlot(equipmentSlot), heldItem);
	}

	@Nonnull
	@Override
	public ItemStack slotClick(int par1, int par2, ClickType par3, EntityPlayer par4EntityPlayer)
	{
		if(par1==this.blockedSlot||(par3==ClickType.SWAP&&par2==par4EntityPlayer.inventory.currentItem))
			return ItemStack.EMPTY;
		ItemStack ret = super.slotClick(par1, par2, par3, par4EntityPlayer);
		updatePlayerItem();
		return ret;
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer)
	{
		super.onContainerClosed(par1EntityPlayer);
		if(!this.world.isRemote)
			updatePlayerItem();
	}

	protected void updatePlayerItem()
	{
	}
}