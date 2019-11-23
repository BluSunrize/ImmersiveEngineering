/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.blocks.stone.CokeOvenTileEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class CokeOvenContainer extends IEBaseContainer<CokeOvenTileEntity>
{
	public CokeOvenTileEntity.CokeOvenData data;
	public CokeOvenContainer(int id, PlayerInventory inventoryPlayer, CokeOvenTileEntity tile)
	{
		super(inventoryPlayer, tile, id);

		this.addSlot(new IESlot(this, this.inv, 0, 30, 35)
		{
			@Override
			public boolean isItemValid(ItemStack itemStack)
			{
				return CokeOvenRecipe.findRecipe(itemStack)!=null;
			}
		});
		this.addSlot(new IESlot.Output(this, this.inv, 1, 85, 35));
		this.addSlot(new IESlot.FluidContainer(this, this.inv, 2, 152, 17, 0));
		this.addSlot(new IESlot.Output(this, this.inv, 3, 152, 53));
		slotCount = 4;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 142));
		data = tile.guiData;
		trackIntArray(data);
	}
}