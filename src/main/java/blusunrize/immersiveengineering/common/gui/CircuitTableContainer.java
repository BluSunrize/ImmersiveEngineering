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
import blusunrize.immersiveengineering.common.items.LogicCircuitBoardItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class CircuitTableContainer extends IEBaseContainer<CircuitTableTileEntity>
{
	private final Inventory outputInventory = new Inventory(1);

	public LogicCircuitInstruction instruction;

	public CircuitTableContainer(int id, PlayerInventory inventoryPlayer, CircuitTableTileEntity tile)
	{
		super(inventoryPlayer, tile, id);

		this.addSlot(new IESlot.Tagged(this, this.inv, this.slotCount++, 8, 7, IETags.circuitPCB));
		this.addSlot(new IESlot.Tagged(this, this.inv, this.slotCount++, 8, 25, IETags.circuitLogic));
		this.addSlot(new IESlot.Tagged(this, this.inv, this.slotCount++, 8, 61, IETags.circuitSolder));

		this.addSlot(new IESlot.Output(this, this.outputInventory, 0, 194, 56)
		{
			@Override
			public int getSlotStackLimit()
			{
				return 1;
			}

			@Override
			public ItemStack onTake(PlayerEntity player, ItemStack stack)
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
	}

	private void consumeInputs()
	{
		if(instruction!=null)
			this.tile.consumeInputs(instruction);
	}

	@Override
	public void onCraftMatrixChanged(IInventory inventory)
	{
		if(instruction!=null&&this.tile.canAssemble(instruction))
			this.outputInventory.setInventorySlotContents(0, LogicCircuitBoardItem.buildCircuitBoard(instruction));
		else
			this.outputInventory.setInventorySlotContents(0, ItemStack.EMPTY);
		super.onCraftMatrixChanged(inventory);
	}

	@Override
	public void receiveMessageFromScreen(CompoundNBT nbt)
	{
		this.instruction = nbt.contains("operator")?LogicCircuitInstruction.deserialize(nbt): null;
		this.onCraftMatrixChanged(this.inv);
	}
}