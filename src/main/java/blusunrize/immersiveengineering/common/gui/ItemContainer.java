/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public abstract class ItemContainer extends Container implements Supplier<World>
{
	protected final PlayerInventory inventoryPlayer;
	protected final World world;
	protected int blockedSlot;
	protected final EquipmentSlotType equipmentSlot;
	protected final ItemStack heldItem;
	protected final PlayerEntity player;
	public int internalSlots;

	public ItemContainer(ContainerType<?> type, int id, PlayerInventory inventoryPlayer, World world, EquipmentSlotType entityEquipmentSlot, ItemStack heldItem)
	{
		super(type, id);
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

	public EquipmentSlotType getEquipmentSlot()
	{
		return equipmentSlot;
	}

	@Nonnull
	@Override
	public ItemStack transferStackInSlot(PlayerEntity par1EntityPlayer, int slot)
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
							insert = stackInSlot.split(space);
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
	public boolean canInteractWith(@Nonnull PlayerEntity entityplayer)
	{
		return ItemStack.areItemsEqual(player.getItemStackFromSlot(equipmentSlot), heldItem);
	}

	@Nonnull
	@Override
	public ItemStack slotClick(int par1, int par2, ClickType par3, PlayerEntity par4EntityPlayer)
	{
		if(par1==this.blockedSlot||(par3==ClickType.SWAP&&par2==par4EntityPlayer.inventory.currentItem))
			return ItemStack.EMPTY;
		ItemStack ret = super.slotClick(par1, par2, par3, par4EntityPlayer);
		updatePlayerItem();
		detectAndSendChanges();
		return ret;
	}

	@Override
	public void onContainerClosed(PlayerEntity par1EntityPlayer)
	{
		super.onContainerClosed(par1EntityPlayer);
		if(!this.world.isRemote)
			updatePlayerItem();
	}

	protected void updatePlayerItem()
	{
	}

	@Override
	public World get()
	{
		return world;
	}
}