/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.blocks.wooden.WoodenCrateBlockEntity;
import com.google.common.base.Preconditions;
import invtweaks.api.container.ChestContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

@ChestContainer
public class CrateMenu extends AbstractContainerMenu implements IScreenMessageReceive
{
	private final Container container;

	public CrateMenu(MenuType<?> type, int id, Inventory inventoryPlayer, Container container)
	{
		super(type, id);
		Preconditions.checkArgument(container.getContainerSize()==WoodenCrateBlockEntity.CONTAINER_SIZE);
		this.container = container;
		for(int i = 0; i < container.getContainerSize(); i++)
			this.addSlot(new Slot(container, i, 8+(i%9)*18, 18+(i/9)*18)
			{
				@Override
				public boolean mayPlace(ItemStack stack)
				{
					return IEApi.isAllowedInCrate(stack);
				}
			});

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 87+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 145));
	}

	public CrateMenu(MenuType<?> type, int id, Inventory inventoryPlayer)
	{
		this(type, id, inventoryPlayer, new SimpleContainer(WoodenCrateBlockEntity.CONTAINER_SIZE));
	}

	@Override
	public void receiveMessageFromScreen(CompoundTag nbt)
	{
		if(container instanceof WoodenCrateBlockEntity crate)
		{
			crate.setCustomName(Component.literal(nbt.getString("name")));
			crate.doGraphicalUpdates();
		}
	}

	@Override
	public boolean stillValid(@Nonnull Player pPlayer)
	{
		return container.stillValid(pPlayer);
	}

	public ItemStack quickMoveStack(Player pPlayer, int pIndex)
	{
		ItemStack stackInSlot = ItemStack.EMPTY;
		Slot slot = this.slots.get(pIndex);
		if(slot!=null&&slot.hasItem())
		{
			ItemStack itemInSlot = slot.getItem();
			stackInSlot = itemInSlot.copy();
			if(pIndex < WoodenCrateBlockEntity.CONTAINER_SIZE)
			{
				if(!this.moveItemStackTo(itemInSlot, WoodenCrateBlockEntity.CONTAINER_SIZE, this.slots.size(), true))
					return ItemStack.EMPTY;
			}
			else if(!this.moveItemStackTo(itemInSlot, 0, WoodenCrateBlockEntity.CONTAINER_SIZE, false))
				return ItemStack.EMPTY;
			slot.setChanged();
		}

		return stackInSlot;
	}
}