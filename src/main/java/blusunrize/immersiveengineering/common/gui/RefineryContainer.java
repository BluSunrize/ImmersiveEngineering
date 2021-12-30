/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.common.blocks.metal.RefineryBlockEntity;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import java.util.Optional;

public class RefineryContainer extends IEBaseContainer<RefineryBlockEntity>
{
	public RefineryContainer(MenuType<?> type, int id, Inventory inventoryPlayer, RefineryBlockEntity tile)
	{
		super(type, tile, id);

		this.addSlot(new Slot(this.inv, slotCount++, 73, 26));
		this.addSlot(new IESlot.FluidContainer(this, this.inv, slotCount++, 133, 15, 0));
		this.addSlot(new IESlot.Output(this, this.inv, slotCount++, 133, 54));

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 143));
		addGenericData(GenericContainerData.energy(tile.energyStorage));
		for(int i = 0; i < 3; ++i)
			addGenericData(GenericContainerData.fluid(tile.tanks[i]));
	}
}