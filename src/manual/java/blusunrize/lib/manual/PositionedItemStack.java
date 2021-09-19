/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import net.minecraft.tags.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

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
			displayList.addAll(Arrays.asList(((Ingredient)stack).getItems()));
		else if(stack instanceof List&&!((List<?>)stack).isEmpty())
			displayList.addAll((List<ItemStack>)this.stack);
		else if(stack instanceof Tag)
			((Tag<?>)stack).getValues().stream()
					.map(o -> ((ItemLike)o).asItem())
					.map(ItemStack::new)
					.forEach(displayList::add);
		else
			throw new RuntimeException("Unexpected stack object: "+stack);
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
