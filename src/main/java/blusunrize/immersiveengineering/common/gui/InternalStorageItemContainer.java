/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.Objects;

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
		this.inv = Objects.requireNonNull(heldItem.getCapability(ItemHandler.ITEM));
		updateSlots();
	}

	@Override
	protected void updateSlots()
	{
		if(inv==null)
			return;
		super.updateSlots();
	}
}