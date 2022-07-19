/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.List;

public record PositionedItemStack(List<ItemStack> displayList, int x, int y, String amount)
{
	public PositionedItemStack(List<ItemStack> displayList, int x, int y)
	{
		this(displayList, x, y, null);
	}

	public PositionedItemStack(ItemStack stack, int x, int y)
	{
		this(List.of(stack), x, y);
	}

	public PositionedItemStack(ItemStack stack, int x, int y, String amount)
	{
		this(List.of(stack), x, y, amount);
	}

	public PositionedItemStack(ItemStack[] stacks, int x, int y)
	{
		this(Arrays.asList(stacks), x, y);
	}

	public PositionedItemStack(Ingredient ingredient, int x, int y)
	{
		this(ingredient.getItems(), x, y);
	}

	public ItemStack getStackAtCurrentTime()
	{
		if(displayList.isEmpty())
			return ItemStack.EMPTY;

		int perm = (int)(System.currentTimeMillis()/1000%displayList.size());
		return displayList.get(perm);
	}
}
