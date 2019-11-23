/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.IWireCoil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class ToolboxHandler
{
	private static final List<Predicate<ItemStack>> tools = new ArrayList<>();
	private static final List<Predicate<ItemStack>> foods = new ArrayList<>();
	private static final List<BiPredicate<ItemStack, World>> wiring = new ArrayList<>();

	static
	{
		tools.add((s) -> (s.getItem() instanceof ITool&&((ITool)s.getItem()).isTool(s)));
		tools.add((s) -> (s.getItem() instanceof ToolItem));
		foods.add((s) -> (s.getItem().isFood()));
		wiring.add((s, w) -> (s.getItem() instanceof IWireCoil));
		wiring.add((s, w) ->
				{
					Block b = Block.getBlockFromItem(s.getItem());
					BlockState defaultState = b.getDefaultState();
					return b.hasTileEntity(defaultState)&&b.createTileEntity(defaultState, w) instanceof IImmersiveConnectable;
				}
		);
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

	public static boolean isWiring(ItemStack s, World w)
	{
		for(BiPredicate<ItemStack, World> p : wiring)
			if(p.test(s, w))
				return true;
		return false;
	}

	public static void addWiringType(BiPredicate<ItemStack, World> in)
	{
		wiring.add(in);
	}
}
