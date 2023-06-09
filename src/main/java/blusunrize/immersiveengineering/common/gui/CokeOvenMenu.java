/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.CokeOvenLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.CokeOvenLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.FurnaceHandler.StateView;
import blusunrize.immersiveengineering.common.gui.IESlot.NewFluidContainer.Filter;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class CokeOvenMenu extends IEContainerMenu
{
	public final ContainerData data;
	public final FluidTank tank;

	public static CokeOvenMenu makeServer(
			MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<CokeOvenLogic.State> ctx
	)
	{
		final State state = ctx.mbContext().getState();
		return new CokeOvenMenu(
				multiblockCtx(type, id, ctx), invPlayer, state.getInventory().getRawHandler(), state, state.getTank()
		);
	}

	public static CokeOvenMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		return new CokeOvenMenu(
				clientCtx(type, id),
				invPlayer,
				new ItemStackHandler(CokeOvenLogic.NUM_SLOTS),
				new SimpleContainerData(StateView.NUM_SLOTS),
				new FluidTank(CokeOvenLogic.TANK_CAPACITY)
		);
	}

	private CokeOvenMenu(
			MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, ContainerData data, FluidTank tank
	)
	{
		super(ctx);

		this.addSlot(new SlotItemHandler(inv, 0, 30, 35)
		{
			@Override
			public boolean mayPlace(@Nonnull ItemStack itemStack)
			{
				return CokeOvenRecipe.findRecipe(inventoryPlayer.player.level(), itemStack)!=null;
			}
		});
		this.addSlot(new IESlot.NewOutput(inv, 1, 85, 35));
		this.addSlot(new IESlot.NewFluidContainer(inv, 2, 152, 17, Filter.ANY));
		this.addSlot(new IESlot.NewOutput(inv, 3, 152, 53));
		ownSlotCount = 4;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 142));
		this.data = data;
		this.tank = tank;
		addDataSlots(data);
		addGenericData(GenericContainerData.fluid(tank));
	}
}