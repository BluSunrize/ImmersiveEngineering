/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PositionedItemStack
{
	public Object stack;
	public int x;
	public int y;

	public PositionedItemStack(Object stack, int x, int y)
	{
		this.stack = stack;
		this.x = x;
		this.y = y;
	}

	private List<ItemStack> displayList;

	public List<ItemStack> getDisplayList()
	{
		if(displayList==null)
			init();
		return displayList;
	}

	private void init()
	{
		displayList = new ArrayList<>();
		if(stack instanceof ItemStack)
			displayList.add((ItemStack)stack);
		else if(stack instanceof ItemStack[])
			Collections.addAll(displayList, (ItemStack[])stack);
		else if(stack instanceof Ingredient)
			displayList.addAll(Arrays.asList(((Ingredient)stack).getMatchingStacks()));
		else if(stack instanceof IngredientWithSize)
			displayList.addAll(Arrays.asList(((IngredientWithSize)stack).getMatchingStacks()));
		else if(stack instanceof List&&!((List)stack).isEmpty())
			displayList.addAll((List<ItemStack>)this.stack);
		else if(stack instanceof Tag)
			((Tag<?>)stack).getAllElements().stream()
					.map(o -> ((IItemProvider)o).asItem())
					.map(ItemStack::new)
					.forEach(displayList::add);
	}

	public ItemStack getStack()
	{
		if(displayList==null)
			init();
		if(displayList.isEmpty())
			return ItemStack.EMPTY;

		int perm = (int)(System.currentTimeMillis()/1000%displayList.size());
		return displayList.get(perm);
	}
}
