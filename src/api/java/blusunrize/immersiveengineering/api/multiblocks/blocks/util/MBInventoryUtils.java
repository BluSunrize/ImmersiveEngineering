/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks.blocks.util;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.util.function.Consumer;

public class MBInventoryUtils
{
	public static void dropItems(IItemHandlerModifiable inv, Consumer<ItemStack> drop)
	{
		for(int slot = 0; slot < inv.getSlots(); slot++)
		{
			final ItemStack stack = inv.getStackInSlot(slot);
			if(!stack.isEmpty())
			{
				drop.accept(stack.copy());
				inv.setStackInSlot(slot, ItemStack.EMPTY);
			}
		}
	}
}
