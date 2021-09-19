/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class IngredientUtils
{
	public static boolean stacksMatchIngredientList(List<Ingredient> list, NonNullList<ItemStack> stacks)
	{
		return stacksMatchList(list, stacks, i -> 1, Ingredient::test);
	}

	public static boolean stacksMatchIngredientWithSizeList(List<IngredientWithSize> list, NonNullList<ItemStack> stacks)
	{
		return stacksMatchList(list, stacks, IngredientWithSize::getCount, IngredientWithSize::testIgnoringSize);
	}

	public static Ingredient createIngredientFromList(List<ItemStack> list)
	{
		return Ingredient.of(list.toArray(new ItemStack[0]));
	}

	private static <T> boolean stacksMatchList(List<T> list, NonNullList<ItemStack> stacks, Function<T, Integer> size,
											   BiPredicate<T, ItemStack> matchesIgnoringSize)
	{
		List<ItemStack> queryList = new ArrayList<>(stacks.size());
		for(ItemStack s : stacks)
			if(!s.isEmpty())
				queryList.add(s.copy());

		for(T ingr : list)
			if(ingr!=null)
			{
				int amount = size.apply(ingr);
				Iterator<ItemStack> it = queryList.iterator();
				while(it.hasNext())
				{
					ItemStack query = it.next();
					if(!query.isEmpty())
					{
						if(matchesIgnoringSize.test(ingr, query))
						{
							if(query.getCount() > amount)
							{
								query.shrink(amount);
								amount = 0;
							}
							else
							{
								amount -= query.getCount();
								query.setCount(0);
							}
						}
						if(query.getCount() <= 0)
							it.remove();
						if(amount <= 0)
							break;
					}
				}
				if(amount > 0)
					return false;
			}
		return true;
	}

	public static boolean hasPlayerIngredient(Player player, IngredientWithSize ingredient)
	{
		int amount = ingredient.getCount();
		ItemStack itemstack;
		for(InteractionHand hand : InteractionHand.values())
		{
			itemstack = player.getItemInHand(hand);
			if(ingredient.test(itemstack))
			{
				amount -= itemstack.getCount();
				if(amount <= 0)
					return true;
			}
		}
		for(int i = 0; i < player.inventory.getContainerSize(); i++)
		{
			itemstack = player.inventory.getItem(i);
			if(ingredient.test(itemstack))
			{
				amount -= itemstack.getCount();
				if(amount <= 0)
					return true;
			}
		}
		return amount <= 0;
	}

	public static void consumePlayerIngredient(Player player, IngredientWithSize ingredient)
	{
		int amount = ingredient.getCount();
		ItemStack itemstack;
		for(InteractionHand hand : InteractionHand.values())
		{
			itemstack = player.getItemInHand(hand);
			if(ingredient.testIgnoringSize(itemstack))
			{
				int taken = Math.min(amount, itemstack.getCount());
				amount -= taken;
				itemstack.shrink(taken);
				if(itemstack.getCount() <= 0)
					player.setItemInHand(hand, ItemStack.EMPTY);
				if(amount <= 0)
					return;
			}
		}
		for(int i = 0; i < player.inventory.getContainerSize(); i++)
		{
			itemstack = player.inventory.getItem(i);
			if(ingredient.testIgnoringSize(itemstack))
			{
				int taken = Math.min(amount, itemstack.getCount());
				amount -= taken;
				itemstack.shrink(taken);
				if(itemstack.getCount() <= 0)
					player.inventory.setItem(i, ItemStack.EMPTY);
				if(amount <= 0)
					return;
			}
		}
	}
}
