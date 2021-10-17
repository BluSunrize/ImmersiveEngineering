/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler.LogicCircuitInstruction;
import blusunrize.immersiveengineering.common.blocks.wooden.CircuitTableTileEntity;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.items.LogicCircuitBoardItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CircuitTableContainer extends IEBaseContainer<CircuitTableTileEntity>
{
	private final SimpleContainer outputInventory = new SimpleContainer(1);

	public LogicCircuitInstruction instruction;

	public CircuitTableContainer(int id, Inventory inventoryPlayer, CircuitTableTileEntity tile)
	{
		super(tile, id);

		this.addSlot(new IESlot.Tagged(this, this.inv, this.slotCount++, 8, 14, IETags.circuitPCB));
		this.addSlot(new IESlot.Tagged(this, this.inv, this.slotCount++, 8, 34, IETags.circuitLogic));
		this.addSlot(new IESlot.Tagged(this, this.inv, this.slotCount++, 8, 54, IETags.circuitSolder));

		this.addSlot(new IESlot.Output(this, this.outputInventory, 0, 194, 56)
		{
			@Override
			public int getMaxStackSize()
			{
				return 1;
			}

			@Override
			public ItemStack onTake(Player player, ItemStack stack)
			{
				consumeInputs();
				return super.onTake(player, stack);
			}
		});
		this.slotCount++;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 143));
		addGenericData(GenericContainerData.energy(tile.getFluxStorage()));
	}

	private void consumeInputs()
	{
		if(instruction!=null)
			this.tile.consumeInputs(instruction);
	}

	@Override
	public void slotsChanged(Container inventory)
	{
		if(instruction!=null&&this.tile.canAssemble(instruction))
			this.outputInventory.setItem(0, LogicCircuitBoardItem.buildCircuitBoard(instruction));
		else
			this.outputInventory.setItem(0, ItemStack.EMPTY);
		super.slotsChanged(inventory);
	}

	@Override
	public void receiveMessageFromScreen(CompoundTag nbt)
	{
		this.instruction = nbt.contains("operator")?LogicCircuitInstruction.deserialize(nbt): null;
		this.slotsChanged(this.inv);
	}
}