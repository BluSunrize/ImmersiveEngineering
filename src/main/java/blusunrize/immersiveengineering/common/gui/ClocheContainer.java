/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.metal.ClocheBlockEntity;
import blusunrize.immersiveengineering.common.gui.IESlot.Cloche;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;

import static blusunrize.immersiveengineering.common.blocks.metal.ClocheBlockEntity.*;

public class ClocheContainer extends IEBaseContainer<ClocheBlockEntity>
{
	public ClocheContainer(MenuType<?> type, int id, Inventory inventoryPlayer, ClocheBlockEntity bEntity)
	{
		super(type, bEntity, id);
		this.addSlot(new Cloche(SLOT_SOIL, this, this.inv, SLOT_SOIL, 62, 54, tile.getLevel()));
		this.addSlot(new Cloche(SLOT_SEED, this, this.inv, SLOT_SEED, 62, 34, tile.getLevel()));
		this.addSlot(new Cloche(SLOT_FERTILIZER, this, this.inv, SLOT_FERTILIZER, 8, 59, tile.getLevel()));

		for(int i = 0; i < 4; i++)
			this.addSlot(new IESlot.Output(this, this.inv, 3+i, 116+i%2*18, 34+i/2*18){
				@Override
				public void onTake(Player pPlayer, ItemStack pStack)
				{
					super.onTake(pPlayer, pStack);
					if(pStack.getItem()==Items.CHORUS_FRUIT)
						Utils.unlockIEAdvancement(pPlayer, "main/chorus_cloche");
				}
			});

		this.slotCount = 7;
		this.tile = bEntity;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 143));
		addGenericData(GenericContainerData.energy(bEntity.energyStorage));
		addGenericData(GenericContainerData.fluid(bEntity.tank));
		addGenericData(GenericContainerData.int32(() -> bEntity.fertilizerAmount, i -> bEntity.fertilizerAmount = i));
		addGenericData(GenericContainerData.float32(() -> bEntity.fertilizerMod, i -> bEntity.fertilizerMod = i));
	}
}