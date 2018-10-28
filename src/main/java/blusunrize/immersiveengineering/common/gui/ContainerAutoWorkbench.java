/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAutoWorkbench;
import blusunrize.immersiveengineering.common.items.ItemEngineersBlueprint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ContainerAutoWorkbench extends ContainerIEBase<TileEntityAutoWorkbench>
{
	public InventoryPlayer inventoryPlayer;

	public ContainerAutoWorkbench(InventoryPlayer inventoryPlayer, TileEntityAutoWorkbench tile)
	{
		super(inventoryPlayer, tile);

		this.inventoryPlayer = inventoryPlayer;
		this.addSlotToContainer(new IESlot.AutoBlueprint(this, this.inv, 0, 102, 69));

		for(int i = 0; i < 16; i++)
			this.addSlotToContainer(new Slot(this.inv, 1+i, 7+(i%4)*18, 24+(i/4)*18));
		slotCount = 17;

		bindPlayerInv(inventoryPlayer);
	}

	private void bindPlayerInv(InventoryPlayer inventoryPlayer)
	{
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 103+i*18));
		for(int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 161));
	}

//	public void rebindSlots()
//	{
//
//		ImmersiveEngineering.proxy.reInitGui();
//	}

	@Nonnull
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
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
				if(stackInSlot.getItem() instanceof ItemEngineersBlueprint)
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
	public void onCraftMatrixChanged(IInventory p_75130_1_)
	{
		super.onCraftMatrixChanged(p_75130_1_);
		tile.markContainingBlockForUpdate(null);
	}
}