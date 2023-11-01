/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.wooden.LogicUnitBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import static blusunrize.immersiveengineering.common.blocks.wooden.LogicUnitBlockEntity.NUM_SLOTS;

public class LogicUnitMenu extends IEContainerMenu
{
	public static LogicUnitMenu makeServer(
			MenuType<?> type, int id, Inventory invPlayer, LogicUnitBlockEntity be
	)
	{
		return new LogicUnitMenu(blockCtx(type, id, be), invPlayer, new ItemStackHandler(be.getInventory()));
	}

	public static LogicUnitMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		return new LogicUnitMenu(clientCtx(type, id), invPlayer, new ItemStackHandler(NUM_SLOTS));
	}

	private LogicUnitMenu(
			MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv
	)
	{
		super(ctx);
		for(int i = 0; i < inv.getSlots(); i++)
			this.addSlot(new IESlot.LogicCircuit(inv, i, 44+(i%5)*18, 19+(i/5)*18));
		this.ownSlotCount = inv.getSlots();

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 143));
	}
}