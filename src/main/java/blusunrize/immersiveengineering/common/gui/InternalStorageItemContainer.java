/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public abstract class InternalStorageItemContainer extends ItemContainer
{
	public final EquipmentSlot entityEquipmentSlot;
	public IItemHandler inv;

	public InternalStorageItemContainer(
			MenuType<?> type, int id, Inventory iinventory, Level world, EquipmentSlot entityEquipmentSlot, ItemStack heldItem
	)
	{
		super(type, id, iinventory, world, entityEquipmentSlot, heldItem);
		this.entityEquipmentSlot = entityEquipmentSlot;
		this.inv = heldItem.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(RuntimeException::new);
		if(inv instanceof IEItemStackHandler)
			((IEItemStackHandler)inv).setInventoryForUpdate(iinventory);
		updateSlots();
	}

	@Override
	protected void updateSlots()
	{
		if(inv==null)
			return;
		super.updateSlots();
	}

	@Override
	public void removed(Player par1EntityPlayer)
	{
		super.removed(par1EntityPlayer);
		if(inv instanceof IEItemStackHandler)
			((IEItemStackHandler)inv).setInventoryForUpdate(null);
	}

	@Override
	protected void updatePlayerItem()
	{
		/*((IInternalStorageItem)this.heldItem.getItem()).setContainedItems(this.heldItem, ((InventoryStorageItem)this.input).stackList);
		ItemStack hand = player.getItemStackFromSlot(this.equipmentSlot);
		if(!hand.isEmpty() && !hand.equals(heldItem))
			player.setItemStackToSlot(this.equipmentSlot, this.heldItem);
		player.inventory.markDirty();TODO remove?*/
	}
}