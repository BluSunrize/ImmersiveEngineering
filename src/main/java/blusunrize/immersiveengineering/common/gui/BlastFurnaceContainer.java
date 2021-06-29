/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.common.blocks.stone.BlastFurnaceTileEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class BlastFurnaceContainer extends IEBaseContainer<BlastFurnaceTileEntity>
{
	public final BlastFurnaceTileEntity.BlastFurnaceState state;

	public BlastFurnaceContainer(ContainerType<?> type, int id, PlayerInventory inventoryPlayer, BlastFurnaceTileEntity tile)
	{
		super(type, inventoryPlayer, tile, id);

		this.addSlot(new IESlot(this, this.inv, 0, 52, 17)
		{
			@Override
			public boolean isItemValid(ItemStack itemStack)
			{
				return BlastFurnaceRecipe.findRecipe(itemStack)!=null;
			}
		});
		this.addSlot(new IESlot.BlastFuel(this, this.inv, 1, 52, 53));
		this.addSlot(new IESlot.Output(this, this.inv, 2, 112, 17));
		this.addSlot(new IESlot.Output(this, this.inv, 3, 112, 53));
		slotCount = 4;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 142));
		state = tile.getGuiInts();
		trackIntArray(state);
	}
}