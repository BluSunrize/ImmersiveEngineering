/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public abstract class ItemContainer extends AbstractContainerMenu implements Supplier<Level>
{
	protected final Inventory inventoryPlayer;
	protected final Level world;
	protected int blockedSlot;
	protected final EquipmentSlot equipmentSlot;
	protected final ItemStack heldItem;
	protected final Player player;
	public int internalSlots;

	public ItemContainer(MenuType<?> type, int id, Inventory inventoryPlayer, Level world, EquipmentSlot entityEquipmentSlot, ItemStack heldItem)
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
		this.blockedSlot = (this.inventoryPlayer.selected+27+internalSlots);
	}

	abstract int addSlots();

	public EquipmentSlot getEquipmentSlot()
	{
		return equipmentSlot;
	}

	@Nonnull
	@Override
	public ItemStack quickMoveStack(Player par1EntityPlayer, int slot)
	{
		ItemStack oldStackInSlot = ItemStack.EMPTY;
		Slot slotObject = slots.get(slot);

		if(slotObject!=null&&slotObject.hasItem())
		{
			ItemStack stackInSlot = slotObject.getItem();
			oldStackInSlot = stackInSlot.copy();

			if(slot < internalSlots)
			{
				if(!this.moveItemStackTo(stackInSlot, internalSlots, (internalSlots+36), true))
					return ItemStack.EMPTY;
			}
			else if(allowShiftclicking()&&!stackInSlot.isEmpty())
			{
				boolean b = true;
				for(int i = 0; i < internalSlots; i++)
				{
					Slot s = slots.get(i);
					if(s!=null&&s.mayPlace(stackInSlot))
					{
						if(!s.getItem().isEmpty()&&(!ItemStack.isSameItem(stackInSlot, s.getItem())||!Utils.compareItemNBT(stackInSlot, s.getItem())))
							continue;
						int space = Math.min(s.getMaxStackSize(stackInSlot), stackInSlot.getMaxStackSize());
						if(!s.getItem().isEmpty())
							space -= s.getItem().getCount();
						if(space <= 0)
							continue;
						ItemStack insert = stackInSlot;
						if(space < stackInSlot.getCount())
							insert = stackInSlot.split(space);
						if(this.moveItemStackTo(insert, i, i+1, true))
						{
							b = false;
						}
					}
				}
				if(b)
					return ItemStack.EMPTY;
			}

			if(stackInSlot.getCount()==0)
				slotObject.set(ItemStack.EMPTY);
			else
				slotObject.setChanged();

			slotObject.container.setChanged();
			if(stackInSlot.getCount()==oldStackInSlot.getCount())
				return ItemStack.EMPTY;
			slotObject.onTake(player, oldStackInSlot);

			broadcastChanges();
		}
		return oldStackInSlot;
	}

	protected boolean allowShiftclicking()
	{
		return true;
	}

	@Override
	public boolean stillValid(@Nonnull Player entityplayer)
	{
		return ItemStack.isSameItem(player.getItemBySlot(equipmentSlot), heldItem);
	}

	@Override
	public void clicked(int par1, int par2, ClickType par3, Player par4EntityPlayer)
	{
		if(par1==this.blockedSlot||(par3==ClickType.SWAP&&par2==par4EntityPlayer.getInventory().selected))
			return;
		super.clicked(par1, par2, par3, par4EntityPlayer);
		broadcastChanges();
	}

	@Override
	public Level get()
	{
		return world;
	}
}