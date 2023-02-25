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
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.SqueezerLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.SqueezerLogic.State;
import blusunrize.immersiveengineering.common.gui.IESlot.NewFluidContainer.Filter;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SqueezerMenu extends IEContainerMenu
{
	public final IMutableEnergyStorage energy;
	public final FluidTank tank;

	public static SqueezerMenu makeServer(
			MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<State> ctx
	)
	{
		final State state = ctx.mbContext().getState();
		return new SqueezerMenu(
				multiblockCtx(type, id, ctx), invPlayer, state.getInventory(), state.getEnergy(), state.getTank()
		);
	}

	public static SqueezerMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		return new SqueezerMenu(
				clientCtx(type, id),
				invPlayer,
				new ItemStackHandler(SqueezerLogic.NUM_SLOTS),
				new MutableEnergyStorage(SqueezerLogic.ENERGY_CAPACITY),
				new FluidTank(SqueezerLogic.TANK_CAPACITY)
		);
	}

	public SqueezerMenu(
			MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, IMutableEnergyStorage energy, FluidTank tank
	)
	{
		super(ctx);
		this.energy = energy;
		this.tank = tank;

		for(int i = 0; i < 8; i++)
			this.addSlot(new SlotItemHandler(inv, i, 8+(i%4)*18, 35+(i/4)*18));
		this.addSlot(new IESlot.NewOutput(inv, 8, 91, 53));
		this.addSlot(new IESlot.NewFluidContainer(inv, 9, 134, 17, Filter.ANY));
		this.addSlot(new IESlot.NewOutput(inv, 10, 134, 53));
		ownSlotCount = 11;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 143));
		addGenericData(GenericContainerData.energy(energy));
		addGenericData(GenericContainerData.fluid(tank));
	}
}