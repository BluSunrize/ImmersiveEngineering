/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import blusunrize.immersiveengineering.api.IETags;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ToolboxHandler
{
	private static final List<Predicate<ItemStack>> tools = new ArrayList<>();
	private static final List<Predicate<ItemStack>> foods = new ArrayList<>();
	private static final List<Predicate<ItemStack>> wiring = new ArrayList<>();

	static
	{
		tools.add((s) -> s.is(IETags.toolboxTools));
		tools.add((s) -> (s.getItem() instanceof DiggerItem));
		tools.add((s) -> (s.getItem() instanceof ShearsItem));
		foods.add((s) -> (s.getItem().isEdible()));
		foods.add((s) -> s.is(IETags.toolboxFood));
		wiring.add((s) -> s.is(IETags.toolboxWiring));
	}

	public static boolean isTool(ItemStack s)
	{
		for(Predicate<ItemStack> p : tools)
			if(p.test(s))
				return true;
		return false;
	}

	public static void addToolType(Predicate<ItemStack> in)
	{
		tools.add(in);
	}

	public static boolean isFood(ItemStack s)
	{
		for(Predicate<ItemStack> p : foods)
			if(p.test(s))
				return true;
		return false;
	}

	public static void addFoodType(Predicate<ItemStack> in)
	{
		foods.add(in);
	}

	public static boolean isWiring(ItemStack s)
	{
		for(Predicate<ItemStack> p : wiring)
			if(p.test(s))
				return true;
		return false;
	}

	public static void addWiringType(Predicate<ItemStack> in)
	{
		wiring.add(in);
	}
}
