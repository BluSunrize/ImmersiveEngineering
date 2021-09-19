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
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class ToolboxHandler
{
	private static final List<Predicate<ItemStack>> tools = new ArrayList<>();
	private static final List<Predicate<ItemStack>> foods = new ArrayList<>();
	private static final List<BiPredicate<ItemStack, Level>> wiring = new ArrayList<>();

	static
	{
		tools.add((s) -> (s.getItem() instanceof ITool&&((ITool)s.getItem()).isTool(s)));
		tools.add((s) -> (s.getItem() instanceof DiggerItem));
		tools.add((s) -> (s.getItem() instanceof ShearsItem));
		foods.add((s) -> (s.getItem().isEdible()));
		wiring.add((s, w) -> (s.getItem() instanceof IWireCoil));
		wiring.add((s, w) ->
				{
					Block b = Block.byItem(s.getItem());
					BlockState defaultState = b.defaultBlockState();
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

	public static boolean isWiring(ItemStack s, Level w)
	{
		for(BiPredicate<ItemStack, Level> p : wiring)
			if(p.test(s, w))
				return true;
		return false;
	}

	public static void addWiringType(BiPredicate<ItemStack, Level> in)
	{
		wiring.add(in);
	}
}
