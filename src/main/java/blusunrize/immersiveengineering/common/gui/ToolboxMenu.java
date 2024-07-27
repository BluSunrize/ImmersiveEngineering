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
import blusunrize.immersiveengineering.common.blocks.metal.ToolboxBlockEntity;
import blusunrize.immersiveengineering.common.gui.IESlot.ICallbackContainer;
import blusunrize.immersiveengineering.common.items.ToolboxItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.Objects;

public class ToolboxMenu extends IEContainerMenu implements ICallbackContainer
{
	protected int blockedSlot = -1;

	public static ToolboxMenu makeFromBE(
			MenuType<?> type, int id, Inventory invPlayer, ToolboxBlockEntity be
	)
	{
		return new ToolboxMenu(blockCtx(type, id, be), invPlayer, new ItemStackHandler(be.getInventory()));
	}

	public static ToolboxMenu makeFromItem(
			MenuType<?> type, int id, Inventory invPlayer, EquipmentSlot slot, ItemStack stack
	)
	{
		return new ToolboxMenu(
				itemCtx(type, id, invPlayer, slot, stack),
				invPlayer,
				Objects.requireNonNull(stack.getCapability(ItemHandler.ITEM))
		).setBlockedSlot(invPlayer);
	}

	public static ToolboxMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		return new ToolboxMenu(clientCtx(type, id), invPlayer, new ItemStackHandler(ToolboxItem.SLOT_COUNT));
	}

	public static ToolboxMenu makeClientItem(MenuType<?> type, int id, Inventory invPlayer)
	{
		return new ToolboxMenu(clientCtx(type, id), invPlayer, new ItemStackHandler(ToolboxItem.SLOT_COUNT))
				.setBlockedSlot(invPlayer);
	}

	public ToolboxMenu setBlockedSlot(Inventory invPlayer)
	{
		this.blockedSlot = (invPlayer.selected+27+ToolboxItem.SLOT_COUNT);
		return this;
	}

	public ToolboxMenu(MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv)
	{
		super(ctx);
		ownSlotCount = 0;
		addSlot(new IESlot.ContainerCallback(this, inv, ownSlotCount++, 48, 24));
		addSlot(new IESlot.ContainerCallback(this, inv, ownSlotCount++, 30, 42));
		addSlot(new IESlot.ContainerCallback(this, inv, ownSlotCount++, 48, 42));

		addSlot(new IESlot.ContainerCallback(this, inv, ownSlotCount++, 75, 24));
		addSlot(new IESlot.ContainerCallback(this, inv, ownSlotCount++, 93, 24));
		addSlot(new IESlot.ContainerCallback(this, inv, ownSlotCount++, 111, 24));
		addSlot(new IESlot.ContainerCallback(this, inv, ownSlotCount++, 75, 42));
		addSlot(new IESlot.ContainerCallback(this, inv, ownSlotCount++, 93, 42));
		addSlot(new IESlot.ContainerCallback(this, inv, ownSlotCount++, 111, 42));
		addSlot(new IESlot.ContainerCallback(this, inv, ownSlotCount++, 129, 42));

		for(int j = 0; j < 6; j++)
			addSlot(new IESlot.ContainerCallback(this, inv, ownSlotCount++, 35+j*18, 77));
		for(int j = 0; j < 7; j++)
			addSlot(new IESlot.ContainerCallback(this, inv, ownSlotCount++, 26+j*18, 112));

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 157+i*18));

		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 215));
	}

	@Override
	public boolean canInsert(ItemStack stack, int slotNumber, Slot slotObject)
	{
		return canInsert(stack, slotNumber);
	}

	public static boolean canInsert(ItemStack stack, int slotNumber)
	{
		if(stack.isEmpty())
			return false;
		if(!IEApi.isAllowedInCrate(stack))
			return false;
		if(slotNumber < 3)
			return ToolboxHandler.isFood(stack);
		else if(slotNumber < 10)
			return ToolboxHandler.isTool(stack);
		else if(slotNumber < 16)
			return ToolboxHandler.isWiring(stack);
		else
			return true;
	}

	@Override
	public void clicked(int par1, int par2, ClickType par3, Player par4EntityPlayer)
	{
		if(blockedSlot >= 0&&(par1==this.blockedSlot||(par3==ClickType.SWAP&&par2==par4EntityPlayer.getInventory().selected)))
			return;
		super.clicked(par1, par2, par3, par4EntityPlayer);
	}

	@Override
	public boolean canTake(ItemStack stack, int slotNumer, Slot slotObject)
	{
		return true;
	}
}