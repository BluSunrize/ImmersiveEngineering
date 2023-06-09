/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.AdvBlastFurnaceLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.BlastFurnaceLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.BlastFurnaceLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.FurnaceHandler.StateView;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class BlastFurnaceMenu extends IEContainerMenu
{
	public final ContainerData state;
	public final GetterAndSetter<Boolean> leftHeater;
	public final GetterAndSetter<Boolean> rightHeater;

	public static BlastFurnaceMenu makeServer(
			MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<State> ctx
	)
	{
		final State state = ctx.mbContext().getState();
		return new BlastFurnaceMenu(
				multiblockCtx(type, id, ctx), invPlayer,
				state.getInventory(), state.getStateView(),
				GetterAndSetter.constant(false), GetterAndSetter.constant(false)
		);
	}

	public static BlastFurnaceMenu makeServerAdv(
			MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<AdvBlastFurnaceLogic.State> ctx
	)
	{
		final AdvBlastFurnaceLogic.State state = ctx.mbContext().getState();
		final IMultiblockLevel level = ctx.mbContext().getLevel();
		return new BlastFurnaceMenu(
				multiblockCtx(type, id, ctx), invPlayer,
				state.getInventory(), state.getStateView(),
				state.preheaterActive(level, 0), state.preheaterActive(level, 1)
		);
	}

	public static BlastFurnaceMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		return new BlastFurnaceMenu(
				clientCtx(type, id), invPlayer,
				new ItemStackHandler(BlastFurnaceLogic.NUM_SLOTS),
				new SimpleContainerData(StateView.NUM_SLOTS),
				GetterAndSetter.standalone(false), GetterAndSetter.standalone(false)
		);
	}

	private BlastFurnaceMenu(
			MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, ContainerData state,
			GetterAndSetter<Boolean> leftHeater, GetterAndSetter<Boolean> rightHeater
	)
	{
		super(ctx);
		this.leftHeater = leftHeater;
		this.rightHeater = rightHeater;

		Level level = inventoryPlayer.player.level();
		this.addSlot(new SlotItemHandler(inv, 0, 52, 17)
		{
			@Override
			public boolean mayPlace(ItemStack itemStack)
			{
				return BlastFurnaceRecipe.findRecipe(level, itemStack, null)!=null;
			}
		});
		this.addSlot(new IESlot.BlastFuel(inv, 1, 52, 53, level));
		this.addSlot(new IESlot.NewOutput(inv, 2, 112, 17));
		this.addSlot(new IESlot.NewOutput(inv, 3, 112, 53));
		ownSlotCount = 4;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 142));
		this.state = state;
		addDataSlots(state);
		addGenericData(new GenericContainerData<>(GenericDataSerializers.BOOLEAN, leftHeater));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.BOOLEAN, rightHeater));
	}
}