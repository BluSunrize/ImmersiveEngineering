/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.energy.IMutableEnergyStorage;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.common.blocks.metal.RefineryBlockEntity;
import blusunrize.immersiveengineering.common.gui.IESlot.NewFluidContainer.Filter;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class RefineryMenu extends IEContainerMenu
{
	public static RefineryMenu makeServer(MenuType<?> type, int id, Inventory invPlayer, RefineryBlockEntity be)
	{
		return new RefineryMenu(
				blockCtx(type, id, be), invPlayer, new ItemStackHandler(be.inventory), be.energyStorage, be.tanks
		);
	}

	public static RefineryMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		return new RefineryMenu(
				clientCtx(type, id),
				invPlayer,
				new ItemStackHandler(RefineryBlockEntity.NUM_SLOTS),
				new MutableEnergyStorage(RefineryBlockEntity.ENERGY_CAPACITY),
				RefineryBlockEntity.makeTanks()
		);
	}

	public final IEnergyStorage energy;
	public final FluidTank[] tanks;

	public RefineryMenu(
			MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, IMutableEnergyStorage energy, FluidTank[] tanks
	)
	{
		super(ctx);
		this.energy = energy;
		this.tanks = tanks;

		this.addSlot(new SlotItemHandler(inv, ownSlotCount++, 73, 26));
		this.addSlot(new IESlot.NewFluidContainer(inv, ownSlotCount++, 133, 15, Filter.ANY));
		this.addSlot(new IESlot.NewOutput(inv, ownSlotCount++, 133, 54));

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 143));
		addGenericData(GenericContainerData.energy(energy));
		for(int i = 0; i < 3; ++i)
			addGenericData(GenericContainerData.fluid(tanks[i]));
	}
}