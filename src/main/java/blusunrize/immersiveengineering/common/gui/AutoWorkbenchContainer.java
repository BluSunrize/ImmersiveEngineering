/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.metal.AutoWorkbenchBlockEntity;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class AutoWorkbenchContainer extends IEBaseContainer<AutoWorkbenchBlockEntity>
{
	public Inventory inventoryPlayer;

	public AutoWorkbenchContainer(MenuType<?> type, int id, Inventory inventoryPlayer, AutoWorkbenchBlockEntity tile)
	{
		super(type, inventoryPlayer, tile, id);

		this.inventoryPlayer = inventoryPlayer;
		this.addSlot(new IESlot.AutoBlueprint(this, this.inv, 0, 102, 69));

		for(int i = 0; i < 16; i++)
			this.addSlot(new Slot(this.inv, 1+i, 7+(i%4)*18, 24+(i/4)*18));
		slotCount = 17;

		bindPlayerInv(inventoryPlayer);
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

			if(slot < slotCount)
			{
				if(!this.moveItemStackTo(stackInSlot, slotCount, (slotCount+36), true))
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
					for(int i = 1; i < slotCount; i++)
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
	public void slotsChanged(Container inventoryIn)
	{
		super.slotsChanged(inventoryIn);
		tile.markContainingBlockForUpdate(null);
	}
}