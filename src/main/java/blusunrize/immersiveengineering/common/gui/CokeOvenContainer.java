/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.blocks.stone.CokeOvenBlockEntity;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CokeOvenContainer extends IEBaseContainerOld<CokeOvenBlockEntity>
{
	public CokeOvenBlockEntity.CokeOvenData data;

	public CokeOvenContainer(MenuType<?> type, int id, Inventory inventoryPlayer, CokeOvenBlockEntity tile)
	{
		super(type, tile, id);

		this.addSlot(new IESlot(this, this.inv, 0, 30, 35)
		{
			@Override
			public boolean mayPlace(ItemStack itemStack)
			{
				return CokeOvenRecipe.findRecipe(tile.getLevel(), itemStack)!=null;
			}
		});
		this.addSlot(new IESlot.Output(this, this.inv, 1, 85, 35));
		this.addSlot(new IESlot.FluidContainer(this, this.inv, 2, 152, 17, 0));
		this.addSlot(new IESlot.Output(this, this.inv, 3, 152, 53));
		ownSlotCount = 4;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 142));
		data = tile.guiData;
		addDataSlots(data);
		addGenericData(GenericContainerData.fluid(tile.tank));
	}
}