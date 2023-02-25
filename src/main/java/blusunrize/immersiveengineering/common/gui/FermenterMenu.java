/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.FermenterLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.FermenterLogic.State;
import blusunrize.immersiveengineering.common.gui.IESlot.NewFluidContainer.Filter;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class FermenterMenu extends IEContainerMenu
{
	public final EnergyStorage energyStorage;
	public final FluidTank tank;

	public static FermenterMenu makeServer(
			MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<State> ctx
	)
	{
		final State state = ctx.mbContext().getState();
		return new FermenterMenu(
				multiblockCtx(type, id, ctx), invPlayer, state.getInventory(), state.getEnergy(), state.getTank()
		);
	}

	public static FermenterMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		return new FermenterMenu(
				clientCtx(type, id), invPlayer,
				new ItemStackHandler(FermenterLogic.NUM_SLOTS),
				new MutableEnergyStorage(FermenterLogic.ENERGY_CAPACITY),
				new FluidTank(FermenterLogic.TANK_CAPACITY)
		);
	}

	private FermenterMenu(
			MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv,
			MutableEnergyStorage energyStorage, FluidTank tank
	)
	{
		super(ctx);
		this.energyStorage = energyStorage;
		this.tank = tank;

		for(int i = 0; i < 8; i++)
			this.addSlot(new SlotItemHandler(inv, i, 8+(i%4)*18, 19+(i/4)*18));
		this.addSlot(new IESlot.NewOutput(inv, 8, 91, 53));
		this.addSlot(new IESlot.NewFluidContainer(inv, 9, 134, 17, Filter.ANY));
		this.addSlot(new IESlot.NewOutput(inv, 10, 134, 53));
		ownSlotCount = 11;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 143));
		addGenericData(GenericContainerData.energy(energyStorage));
		addGenericData(GenericContainerData.fluid(tank));
	}
}