/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.tool.ToolboxHandler;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityToolbox;
import blusunrize.immersiveengineering.common.gui.IESlot.ICallbackContainer;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class ContainerToolboxBlock extends ContainerIEBase implements ICallbackContainer
{
	IItemHandler inv;

	public ContainerToolboxBlock(InventoryPlayer inventoryPlayer, TileEntityToolbox tile)
	{
		super(inventoryPlayer, tile);
		this.tile = tile;
		inv = new ItemStackHandler(tile.getInventory());
		if(inv instanceof IEItemStackHandler)
			((IEItemStackHandler)inv).setTile(tile);
		this.addSlotToContainer(new IESlot.ContainerCallback(this, inv, slotCount++, 48, 24));
		this.addSlotToContainer(new IESlot.ContainerCallback(this, inv, slotCount++, 30, 42));
		this.addSlotToContainer(new IESlot.ContainerCallback(this, inv, slotCount++, 48, 42));

		this.addSlotToContainer(new IESlot.ContainerCallback(this, inv, slotCount++, 75, 24));
		this.addSlotToContainer(new IESlot.ContainerCallback(this, inv, slotCount++, 93, 24));
		this.addSlotToContainer(new IESlot.ContainerCallback(this, inv, slotCount++, 111, 24));
		this.addSlotToContainer(new IESlot.ContainerCallback(this, inv, slotCount++, 75, 42));
		this.addSlotToContainer(new IESlot.ContainerCallback(this, inv, slotCount++, 93, 42));
		this.addSlotToContainer(new IESlot.ContainerCallback(this, inv, slotCount++, 111, 42));
		this.addSlotToContainer(new IESlot.ContainerCallback(this, inv, slotCount++, 129, 42));

		for(int j = 0; j < 6; j++)
			this.addSlotToContainer(new IESlot.ContainerCallback(this, inv, slotCount++, 35+j*18, 77));
		for(int j = 0; j < 7; j++)
			this.addSlotToContainer(new IESlot.ContainerCallback(this, inv, slotCount++, 26+j*18, 112));

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				this.addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 157+i*18));
		for(int i = 0; i < 9; i++)
			this.addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 215));
	}

	@Override
	public boolean canInsert(ItemStack stack, int slotNumer, Slot slotObject)
	{
		if(stack.isEmpty())
			return false;
		if(!IEApi.isAllowedInCrate(stack))
			return false;
		if(slotNumer < 3)
			return ToolboxHandler.isFood(stack);
		else if(slotNumer < 10)
			return ToolboxHandler.isTool(stack);
		else if(slotNumer < 16)
			return ToolboxHandler.isWiring(stack, this.tile.getWorld());
		else
			return true;
	}

	@Override
	public boolean canTake(ItemStack stack, int slotNumer, Slot slotObject)
	{
		return true;
	}

	@Override
	public void onContainerClosed(EntityPlayer playerIn)
	{
		if(inv instanceof IEItemStackHandler)
			((IEItemStackHandler)inv).setTile(null);
	}
}