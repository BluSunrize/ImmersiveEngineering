/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenCrate;
import invtweaks.api.container.ChestContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

@ChestContainer
public class ContainerCrate extends ContainerIEBase
{
	public ContainerCrate(InventoryPlayer inventoryPlayer, TileEntityWoodenCrate tile)
	{
		super(inventoryPlayer, tile);
		for(int i = 0; i < tile.getInventory().size(); i++)
			this.addSlotToContainer(new Slot(this.inv, i, 8+(i%9)*18, 18+(i/9)*18)
			{
				@Override
				public boolean isItemValid(ItemStack stack)
				{
					return IEApi.isAllowedInCrate(stack);
				}
			});
		this.slotCount = tile.getInventory().size();
		this.tile = tile;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 87+i*18));
		for(int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 145));
	}
}