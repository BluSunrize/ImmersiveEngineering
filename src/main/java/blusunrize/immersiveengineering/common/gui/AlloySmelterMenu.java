/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.AlloySmelterLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.AlloySmelterLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.FurnaceHandler.StateView;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class AlloySmelterMenu extends IEContainerMenu
{
	private final ContainerData stateView;

	public static AlloySmelterMenu makeServer(
			MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<State> ctx
	)
	{
		final State state = ctx.mbContext().getState();
		return new AlloySmelterMenu(
				multiblockCtx(type, id, ctx), invPlayer, state.getInventory(), state.getStateView()
		);
	}

	public static AlloySmelterMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		return new AlloySmelterMenu(
				clientCtx(type, id),
				invPlayer,
				new ItemStackHandler(AlloySmelterLogic.NUM_SLOTS),
				new SimpleContainerData(StateView.NUM_SLOTS)
		);
	}

	private AlloySmelterMenu(
			MenuContext ctx, Inventory inventoryPlayer, IItemHandler smelterInv, ContainerData stateView
	)
	{
		super(ctx);
		this.stateView = stateView;

		this.addSlot(new SlotItemHandler(smelterInv, 0, 38, 17));
		this.addSlot(new SlotItemHandler(smelterInv, 1, 66, 17));
		this.addSlot(new IESlot.IEFurnaceSFuelSlot(smelterInv, 2, 52, 53));
		this.addSlot(new IESlot.NewOutput(smelterInv, 3, 120, 35));
		ownSlotCount = 4;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 142));
		addDataSlots(stateView);
	}

	public ContainerData getStateView()
	{
		return stateView;
	}
}