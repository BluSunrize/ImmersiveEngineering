/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool;
import blusunrize.immersiveengineering.api.tool.IUpgradeableTool;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class MaintenanceKitContainer extends ItemContainer
{
	private final IInventory inv = new Inventory(ItemStack.EMPTY);
	private boolean wasUsed = false;

	public MaintenanceKitContainer(int id, PlayerInventory inventoryPlayer, World world, EquipmentSlotType slot, ItemStack item)
	{
		super(id, inventoryPlayer, world, slot, item);
		updateSlots();
	}

	private void bindPlayerInv(PlayerInventory inventoryPlayer)
	{
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 143));
	}

	@Override
	public int addSlots()
	{
		if(this.inv==null)
			return 0;
		//Don't rebind if the tool didn't change
		if(world.isRemote)
			for(Slot slot : inventorySlots)
				if(slot instanceof IESlot.Upgrades)
					if(ItemStack.areItemsEqual(((IESlot.Upgrades)slot).upgradeableTool, inv.getStackInSlot(0)))
						return this.internalSlots;
		this.inventorySlots.clear();
		this.inventoryItemStacks.clear();
		this.addSlot(new IESlot.Maintenance(this, this.inv, 0, 28, 10));
		int slotCount = 1;

		ItemStack tool = this.getSlot(0).getStack();
		if(tool.getItem() instanceof IUpgradeableTool)
		{
			wasUsed = true;
			Slot[] slots = ((IUpgradeableTool)tool.getItem()).getWorkbenchSlots(this, tool, () -> world, () -> player);
			if(slots!=null)
				for(Slot s : slots)
				{
					this.addSlot(s);
					slotCount++;
				}
		}
		bindPlayerInv(this.inventoryPlayer);
		ImmersiveEngineering.proxy.reInitGui();
		return slotCount;
	}

	@Nonnull
	@Override
	public ItemStack transferStackInSlot(PlayerEntity player, int slot)
	{
		ItemStack stack = ItemStack.EMPTY;
		Slot slotObject = inventorySlots.get(slot);

		if(slotObject!=null&&slotObject.getHasStack())
		{
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();

			if(slot < this.internalSlots)
			{
				if(!this.mergeItemStack(stackInSlot, this.internalSlots, (this.internalSlots+36), true))
					return ItemStack.EMPTY;
			}
			else if(!stackInSlot.isEmpty())
			{
				if(stackInSlot.getItem() instanceof IUpgradeableTool&&((IUpgradeableTool)stackInSlot.getItem()).canModify(stackInSlot))
				{
					if(!this.mergeItemStack(stackInSlot, 0, 1, true))
						return ItemStack.EMPTY;
				}
				else if(stackInSlot.getItem() instanceof IConfigurableTool&&((IConfigurableTool)stackInSlot.getItem()).canConfigure(stackInSlot))
				{
					if(!this.mergeItemStack(stackInSlot, 0, 1, true))
						return ItemStack.EMPTY;
				}
				else if(this.internalSlots > 1)
				{
					boolean b = true;
					for(int i = 1; i < this.internalSlots; i++)
					{
						Slot s = inventorySlots.get(i);
						if(s!=null&&s.isItemValid(stackInSlot))
							if(this.mergeItemStack(stackInSlot, i, i+1, true))
							{
								b = false;
								break;
							}
							else
								continue;
					}
					if(b)
						return ItemStack.EMPTY;
				}
			}

			if(stackInSlot.getCount()==0)
				slotObject.putStack(ItemStack.EMPTY);
			else
				slotObject.onSlotChanged();

			if(stackInSlot.getCount()==stack.getCount())
				return ItemStack.EMPTY;
			slotObject.onTake(player, stack);
		}
		return stack;
	}

	@Override
	public void onContainerClosed(PlayerEntity par1EntityPlayer)
	{
		if(wasUsed)
		{
			this.heldItem.damageItem(1, this.player, player -> {
			});
			player.setItemStackToSlot(this.equipmentSlot, this.heldItem);
		}
		super.onContainerClosed(par1EntityPlayer);
		this.clearContainer(par1EntityPlayer, this.world, this.inv);
	}
}