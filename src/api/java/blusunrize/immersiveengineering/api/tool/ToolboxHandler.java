/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import blusunrize.immersiveengineering.api.IETags;
import com.google.common.collect.Sets;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class ToolboxHandler
{
	public enum ToolboxCategory
	{
		FOOD(0, 2, ToolboxHandler::isFood),
		TOOL(3, 9, ToolboxHandler::isTool),
		WIRING(10, 15, ToolboxHandler::isWiring),
		ANY(16, 22, s -> true);

		final int[] slots;
		final Predicate<ItemStack> accepts;

		ToolboxCategory(int from, int to, Predicate<ItemStack> accepts)
		{
			this.slots = IntStream.rangeClosed(from, to).toArray();
			this.accepts = accepts;
		}

		public int[] getSlots()
		{
			return slots;
		}

		public boolean accepts(ItemStack stack)
		{
			return accepts.test(stack);
		}
	}

	private static final List<Predicate<ItemStack>> tools = new ArrayList<>();
	private static final List<Predicate<ItemStack>> foods = new ArrayList<>();
	private static final List<Predicate<ItemStack>> wiring = new ArrayList<>();

	static
	{
		Set<ItemAbility> toolActions = Sets.newHashSet();
		toolActions.addAll(ItemAbilities.DEFAULT_PICKAXE_ACTIONS);
		toolActions.addAll(ItemAbilities.DEFAULT_AXE_ACTIONS);
		toolActions.addAll(ItemAbilities.DEFAULT_SHOVEL_ACTIONS);
		toolActions.addAll(ItemAbilities.DEFAULT_HOE_ACTIONS);
		toolActions.addAll(ItemAbilities.DEFAULT_SHEARS_ACTIONS);

		tools.add((s) -> s.is(IETags.toolboxTools));
		tools.add((s) -> {
			for(ItemAbility action : toolActions)
				if(s.canPerformAction(action))
					return true;
			return false;
		});
		foods.add((s) -> s.has(DataComponents.FOOD));
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
