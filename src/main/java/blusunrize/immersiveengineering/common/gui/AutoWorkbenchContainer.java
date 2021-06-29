/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.metal.AutoWorkbenchTileEntity;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class AutoWorkbenchContainer extends IEBaseContainer<AutoWorkbenchTileEntity>
{
	public PlayerInventory inventoryPlayer;

	public AutoWorkbenchContainer(ContainerType<?> type, int id, PlayerInventory inventoryPlayer, AutoWorkbenchTileEntity tile)
	{
		super(type, inventoryPlayer, tile, id);

		this.inventoryPlayer = inventoryPlayer;
		this.addSlot(new IESlot.AutoBlueprint(this, this.inv, 0, 102, 69));

		for(int i = 0; i < 16; i++)
			this.addSlot(new Slot(this.inv, 1+i, 7+(i%4)*18, 24+(i/4)*18));
		slotCount = 17;

		bindPlayerInv(inventoryPlayer);
	}

	private void bindPlayerInv(PlayerInventory inventoryPlayer)
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
	public ItemStack transferStackInSlot(PlayerEntity player, int slot)
	{
		ItemStack stack = ItemStack.EMPTY;
		Slot slotObject = inventorySlots.get(slot);

		if(slotObject!=null&&slotObject.getHasStack())
		{
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();

			if(slot < slotCount)
			{
				if(!this.mergeItemStack(stackInSlot, slotCount, (slotCount+36), true))
					return ItemStack.EMPTY;
			}
			else if(!stackInSlot.isEmpty())
			{
				if(stackInSlot.getItem() instanceof EngineersBlueprintItem)
				{
					if(!this.mergeItemStack(stackInSlot, 0, 1, true))
						return ItemStack.EMPTY;
				}
				else
				{
					boolean b = true;
					for(int i = 1; i < slotCount; i++)
					{
						Slot s = inventorySlots.get(i);
						if(s!=null&&s.isItemValid(stackInSlot))
							if(this.mergeItemStack(stackInSlot, i, i+1, true))
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
				slotObject.putStack(ItemStack.EMPTY);
			else
				slotObject.onSlotChanged();

			if(stackInSlot.getCount()==stack.getCount())
				return ItemStack.EMPTY;
			slotObject.onTake(player, stack);
		}
		return stack;
	}

	@Override
	public void onCraftMatrixChanged(IInventory inventoryIn)
	{
		super.onCraftMatrixChanged(inventoryIn);
		tile.markContainingBlockForUpdate(null);
	}
}