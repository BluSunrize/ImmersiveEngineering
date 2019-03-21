/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
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

	public ArrayList<ItemStack> displayList;

	public ItemStack getStack()
	{
		if(displayList==null)
		{
			displayList = new ArrayList<ItemStack>();
			if(stack instanceof ItemStack)
			{
				if(((ItemStack)stack).getItemDamage()==OreDictionary.WILDCARD_VALUE)
				{
					NonNullList<ItemStack> list = NonNullList.create();
					((ItemStack)stack).getItem().getSubItems(((ItemStack)stack).getItem().getCreativeTab(), list);
					if(list.size() > 0)
						displayList.addAll(list);
				}
				else
					displayList.add((ItemStack)stack);
			}
			else if(stack instanceof Ingredient)
			{
				for(ItemStack subStack : ((Ingredient)stack).getMatchingStacks())
				{
					if(subStack.getItemDamage()==OreDictionary.WILDCARD_VALUE)
					{
						NonNullList<ItemStack> list = NonNullList.create();
						subStack.getItem().getSubItems(subStack.getItem().getCreativeTab(), list);
						if(list.size() > 0)
							displayList.addAll(list);
					}
					else
						displayList.add(subStack);
				}
			}
			else if(stack instanceof List&&!((List)stack).isEmpty())
			{
				for(ItemStack subStack : (List<ItemStack>)this.stack)
				{
					if(subStack.getItemDamage()==OreDictionary.WILDCARD_VALUE)
					{
						NonNullList<ItemStack> list = NonNullList.create();
						subStack.getItem().getSubItems(subStack.getItem().getCreativeTab(), list);
						if(list.size() > 0)
							displayList.addAll(list);
					}
					else
						displayList.add(subStack);
				}
			}
		}
		if(displayList==null||displayList.isEmpty())
			return ItemStack.EMPTY;

		int perm = (int)(System.currentTimeMillis()/1000%displayList.size());
		return displayList.get(perm);
	}
}
