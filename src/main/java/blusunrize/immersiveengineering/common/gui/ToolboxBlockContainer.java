/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.metal.ToolboxBlockEntity;
import blusunrize.immersiveengineering.common.gui.IESlot.ICallbackContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class ToolboxBlockContainer extends IEBaseContainerOld<ToolboxBlockEntity> implements ICallbackContainer
{
	public ToolboxBlockContainer(MenuType<?> type, int id, Inventory inventoryPlayer, ToolboxBlockEntity tile)
	{
		super(type, tile, id);
		this.tile = tile;
		IItemHandler inv = new ItemStackHandler(tile.getInventory());

		this.ownSlotCount = ToolboxContainer.addSlots(this::addSlot, this, inv, inventoryPlayer);
	}

	@Override
	public boolean canInsert(ItemStack stack, int slotNumber, Slot slotObject)
	{
		return ToolboxContainer.canInsert(stack, slotNumber);
	}

	@Override
	public boolean canTake(ItemStack stack, int slotNumber, Slot slotObject)
	{
		return true;
	}
}