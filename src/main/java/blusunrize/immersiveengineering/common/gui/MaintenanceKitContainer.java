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
import blusunrize.immersiveengineering.mixin.accessors.ContainerAccess;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.Objects;

import static blusunrize.immersiveengineering.common.gui.ModWorkbenchContainer.MAX_NUM_DYNAMIC_SLOTS;

public class MaintenanceKitContainer extends ItemContainer
{
	private final Container inv = new SimpleContainer(ItemStack.EMPTY);
	private final ItemStackHandler clientInventory = new ItemStackHandler(MAX_NUM_DYNAMIC_SLOTS+1);
	private boolean wasUsed = false;

	public MaintenanceKitContainer(MenuType<?> type, int id, Inventory inventoryPlayer, Level world, EquipmentSlot slot, ItemStack item)
	{
		super(type, id, inventoryPlayer, world, slot, item);
		updateSlots();
	}

	private void bindPlayerInv(Inventory inventoryPlayer)
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
		this.slots.clear();
		((ContainerAccess)this).getLastSlots().clear();
		this.addSlot(new IESlot.Maintenance(this, this.inv, 0, 28, 10));
		int slotCount = 1;

		ItemStack tool = this.getSlot(0).getItem();
		if(tool.getItem() instanceof IUpgradeableTool upgradeableTool)
		{
			wasUsed = true;
			IItemHandler toolInv = Objects.requireNonNull(tool.getCapability(ItemHandler.ITEM));
			Slot[] slots = upgradeableTool.getWorkbenchSlots(
					this, tool, world, () -> player, world.isClientSide?clientInventory: toolInv
			);
			if(slots!=null)
				for(Slot s : slots)
				{
					this.addSlot(s);
					slotCount++;
				}
		}
		for(; slotCount < MAX_NUM_DYNAMIC_SLOTS; ++slotCount)
			addSlot(new IESlot.AlwaysEmptySlot(this));
		bindPlayerInv(this.inventoryPlayer);
		ImmersiveEngineering.proxy.reInitGui();
		return slotCount;
	}

	@Nonnull
	@Override
	public ItemStack quickMoveStack(Player player, int slot)
	{
		ItemStack stack = ItemStack.EMPTY;
		Slot slotObject = slots.get(slot);

		if(slotObject!=null&&slotObject.hasItem())
		{
			ItemStack stackInSlot = slotObject.getItem();
			stack = stackInSlot.copy();

			if(slot < this.internalSlots)
			{
				if(!this.moveItemStackTo(stackInSlot, this.internalSlots, (this.internalSlots+36), true))
					return ItemStack.EMPTY;
			}
			else if(!stackInSlot.isEmpty())
			{
				if(stackInSlot.getItem() instanceof IUpgradeableTool&&((IUpgradeableTool)stackInSlot.getItem()).canModify(stackInSlot))
				{
					if(!this.moveItemStackTo(stackInSlot, 0, 1, true))
						return ItemStack.EMPTY;
				}
				else if(stackInSlot.getItem() instanceof IConfigurableTool&&((IConfigurableTool)stackInSlot.getItem()).canConfigure(stackInSlot))
				{
					if(!this.moveItemStackTo(stackInSlot, 0, 1, true))
						return ItemStack.EMPTY;
				}
				else if(this.internalSlots > 1)
				{
					boolean b = true;
					for(int i = 1; i < this.internalSlots; i++)
					{
						Slot s = slots.get(i);
						if(s!=null&&s.mayPlace(stackInSlot))
							if(this.moveItemStackTo(stackInSlot, i, i+1, true))
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
				slotObject.set(ItemStack.EMPTY);
			else
				slotObject.setChanged();

			if(stackInSlot.getCount()==stack.getCount())
				return ItemStack.EMPTY;
			slotObject.onTake(player, stack);
		}
		return stack;
	}

	@Override
	public void removed(Player par1EntityPlayer)
	{
		if(wasUsed)
		{
			this.heldItem.hurtAndBreak(1, this.player, this.equipmentSlot);
			player.setItemSlot(this.equipmentSlot, this.heldItem);
		}
		super.removed(par1EntityPlayer);
		this.clearContainer(par1EntityPlayer, this.inv);
	}
}