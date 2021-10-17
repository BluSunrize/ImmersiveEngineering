/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.metal.AssemblerBlockEntity;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;

public class AssemblerContainer extends IEBaseContainer<AssemblerBlockEntity>
{
	public AssemblerContainer(MenuType<?> type, int id, Inventory inventoryPlayer, AssemblerBlockEntity tile)
	{
		super(type, tile, id);
		this.tile = tile;
		for(int i = 0; i < tile.patterns.length; i++)
		{
			this.tile.patterns[i].recalculateOutput();
			IItemHandler itemHandler = new InvWrapper(this.tile.patterns[i]);
			for(int j = 0; j < 9; j++)
			{
				int x = 9+i*58+(j%3)*18;
				int y = 7+(j/3)*18;
				this.addSlot(new IESlot.ItemHandlerGhost(itemHandler, j, x, y));
			}
			this.addSlot(new IESlot.Output(this, this.inv, 18+i, 27+i*58, 64));
		}
		for(int i = 0; i < 18; i++)
			this.addSlot(new Slot(this.inv, i, 13+(i%9)*18, 87+(i/9)*18));
		slotCount = 21;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 13+j*18, 137+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 13+i*18, 195));
		addGenericData(GenericContainerData.energy(tile.energyStorage));
	}

	@Nonnull
	@Override
	public ItemStack quickMoveStack(Player player, int slot)
	{
		ItemStack stack = ItemStack.EMPTY;
		Slot slotObject = slots.get(slot);

		if(slotObject!=null&&slotObject.hasItem()&&!(slotObject instanceof IESlot.ItemHandlerGhost))
		{
			ItemStack stackInSlot = slotObject.getItem();
			stack = stackInSlot.copy();
			if(slot < 48)
			{
				if(!this.moveItemStackTo(stackInSlot, 48, (48+36), true))
					return ItemStack.EMPTY;
			}
			else
			{
				if(!this.moveItemStackTo(stackInSlot, 30, 48, false))
					return ItemStack.EMPTY;
			}

			if(stackInSlot.getCount()==0)
				slotObject.set(ItemStack.EMPTY);
			else
				slotObject.setChanged();

			if(stackInSlot.getCount()==stack.getCount())
				return ItemStack.EMPTY;
			slotObject.onTake(player, stackInSlot);
		}
		return stack;
	}
}