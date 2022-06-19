/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler.LogicCircuitInstruction;
import blusunrize.immersiveengineering.common.blocks.wooden.CircuitTableBlockEntity;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.items.LogicCircuitBoardItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import static blusunrize.immersiveengineering.common.blocks.wooden.CircuitTableBlockEntity.*;

public class CircuitTableMenu extends IEContainerMenu
{
	private final ItemStackHandler outputInventory = new ItemStackHandler(1);

	public LogicCircuitInstruction instruction;
	private final IItemHandler inv;
	public final EnergyStorage energyStorage;

	public static CircuitTableMenu makeServer(
			MenuType<?> type, int id, Inventory invPlayer, CircuitTableBlockEntity be
	)
	{
		return new CircuitTableMenu(
				blockCtx(type, id, be), invPlayer, new ItemStackHandler(be.getInventory()), be.energyStorage
		);
	}

	public static CircuitTableMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		return new CircuitTableMenu(
				clientCtx(type, id), invPlayer,
				new ItemStackHandler(NUM_SLOTS), new MutableEnergyStorage(ENERGY_CAPACITY)
		);
	}

	private CircuitTableMenu(
			MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, MutableEnergyStorage energyStorage
	)
	{
		super(ctx);
		this.inv = inv;
		this.energyStorage = energyStorage;

		this.addSlot(new IESlot.Tagged(inv, this.ownSlotCount++, 8, 14, IETags.circuitPCB));
		this.addSlot(new IESlot.Tagged(inv, this.ownSlotCount++, 8, 34, IETags.circuitLogic));
		this.addSlot(new IESlot.Tagged(inv, this.ownSlotCount++, 8, 54, IETags.circuitSolder));

		this.addSlot(new IESlot.LogicCircuit(inv, this.ownSlotCount++, 175, 11));

		this.addSlot(new IESlot.NewOutput(outputInventory, 0, 194, 56)
		{
			@Override
			public int getMaxStackSize()
			{
				return 1;
			}

			@Override
			public void onTake(Player player, ItemStack stack)
			{
				consumeInputs();
				super.onTake(player, stack);
			}
		});
		this.ownSlotCount++;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 143));
		addGenericData(GenericContainerData.energy(energyStorage));
	}

	private void consumeInputs()
	{
		if(instruction!=null)
		{
			consumeInputs(instruction, getEditInstruction()!=null);
			updateOutput();
		}
	}

	private LogicCircuitInstruction getEditInstruction()
	{
		return LogicCircuitBoardItem.getInstruction(inv.getStackInSlot(getEditSlot()));
	}

	@Override
	public void slotsChanged(Container inventory)
	{
		updateOutput();
		super.slotsChanged(inventory);
	}

	private void updateOutput()
	{
		ItemStack newOutput;
		if(instruction!=null&&canAssemble(instruction, getEditInstruction()!=null))
			newOutput = LogicCircuitBoardItem.buildCircuitBoard(instruction);
		else
			newOutput = ItemStack.EMPTY;
		this.outputInventory.setStackInSlot(0, newOutput);
	}

	public boolean canAssemble(LogicCircuitInstruction instruction, boolean editInstruction)
	{
		if(energyStorage.getEnergyStored() < ASSEMBLY_ENERGY)
			return false;
		if(editInstruction)
			return !inv.getStackInSlot(getEditSlot()).isEmpty();
		for(int i = 0; i < SLOT_TYPES.length; i++)
		{
			ItemStack input = inv.getStackInSlot(i);
			if(input.getCount() < getIngredientAmount(instruction, i))
				return false;
		}
		return true;
	}

	public void consumeInputs(LogicCircuitInstruction instruction, boolean editInstruction)
	{
		energyStorage.extractEnergy(ASSEMBLY_ENERGY, false);
		if(editInstruction)
			inv.getStackInSlot(getEditSlot()).shrink(1);
		else
			for(int i = 0; i < SLOT_TYPES.length; i++)
				inv.getStackInSlot(i).shrink(getIngredientAmount(instruction, i));
	}

	@Override
	public void receiveMessageFromScreen(CompoundTag nbt)
	{
		this.instruction = nbt.contains("operator")?LogicCircuitInstruction.deserialize(nbt): null;
		updateOutput();
	}
}