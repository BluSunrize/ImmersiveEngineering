/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public abstract class ContainerInternalStorageItem extends ContainerItem
{
	public IItemHandler inv;

	public ContainerInternalStorageItem(InventoryPlayer iinventory, World world, EntityEquipmentSlot entityEquipmentSlot, ItemStack heldItem)
	{
		super(iinventory, world, entityEquipmentSlot, heldItem);
		this.inv = heldItem.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
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
	public void onContainerClosed(EntityPlayer par1EntityPlayer)
	{
		super.onContainerClosed(par1EntityPlayer);
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