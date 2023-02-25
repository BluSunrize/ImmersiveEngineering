/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.AutoWorkbenchLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.AutoWorkbenchLogic.State;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class AutoWorkbenchMenu extends IEContainerMenu
{
	public final EnergyStorage energyStorage;
	public final GetterAndSetter<Integer> selectedRecipe;

	public static AutoWorkbenchMenu makeServer(
			MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<State> ctx
	)
	{
		final State state = ctx.mbContext().getState();
		return new AutoWorkbenchMenu(
				multiblockCtx(type, id, ctx), invPlayer,
				state.inventory, state.getEnergy(),
				new GetterAndSetter<>(() -> state.selectedRecipe, i -> state.selectedRecipe = i)
		);
	}

	public static AutoWorkbenchMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		return new AutoWorkbenchMenu(
				clientCtx(type, id),
				invPlayer,
				new ItemStackHandler(AutoWorkbenchLogic.NUM_SLOTS),
				new MutableEnergyStorage(AutoWorkbenchLogic.ENERGY_CAPACITY),
				GetterAndSetter.standalone(0)
		);
	}

	private AutoWorkbenchMenu(
			MenuContext ctx, Inventory inventoryPlayer,
			IItemHandler inv, MutableEnergyStorage energyStorage, GetterAndSetter<Integer> selectedRecipe
	)
	{
		super(ctx);
		this.energyStorage = energyStorage;
		this.selectedRecipe = selectedRecipe;

		this.addSlot(new IESlot.AutoBlueprint(inv, 0, 102, 69));

		for(int i = 0; i < 16; i++)
			this.addSlot(new SlotItemHandler(inv, 1+i, 7+(i%4)*18, 24+(i/4)*18));
		ownSlotCount = 17;

		bindPlayerInv(inventoryPlayer);
		addGenericData(GenericContainerData.energy(energyStorage));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.INT32, selectedRecipe));
	}

	private void bindPlayerInv(Inventory inventoryPlayer)
	{
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 103+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 161));
	}

//	public void rebindSlots()
//	{
//
//		ImmersiveEngineering.proxy.reInitGui();
//	}

	@Nonnull
	@Override
	public ItemStack quickMoveStack(Player player, int slot)
	{
		ItemStack stack = ItemStack.EMPTY;
		Slot slotObject = slots.get(slot);

		if(slotObject!=null&&slotObject.hasItem())
		{
			ItemStack stackInSlot = slotObject.getItem();
			stack = stackInSlot.copy();

			if(slot < ownSlotCount)
			{
				if(!this.moveItemStackTo(stackInSlot, ownSlotCount, (ownSlotCount+36), true))
					return ItemStack.EMPTY;
			}
			else if(!stackInSlot.isEmpty())
			{
				if(stackInSlot.getItem() instanceof EngineersBlueprintItem)
				{
					if(!this.moveItemStackTo(stackInSlot, 0, 1, true))
						return ItemStack.EMPTY;
				}
				else
				{
					boolean b = true;
					for(int i = 1; i < ownSlotCount; i++)
					{
						Slot s = slots.get(i);
						if(s!=null&&s.mayPlace(stackInSlot))
							if(this.moveItemStackTo(stackInSlot, i, i+1, true))
							{
								b = false;
								break;
							}
							else
								continue;
					}
					if(b)
						return ItemStack.EMPTY;
				}
			}

			if(stackInSlot.getCount()==0)
				slotObject.set(ItemStack.EMPTY);
			else
				slotObject.setChanged();

			if(stackInSlot.getCount()==stack.getCount())
				return ItemStack.EMPTY;
			slotObject.onTake(player, stack);
		}
		return stack;
	}

	@Override
	public void receiveMessageFromScreen(CompoundTag nbt)
	{
		if(nbt.contains("recipe", Tag.TAG_INT))
			this.selectedRecipe.set(nbt.getInt("recipe"));
	}
}