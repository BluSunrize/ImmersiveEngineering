/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import net.minecraft.world.item.*;

import java.util.function.Supplier;

public class IETools
{
	public static Supplier<ShovelItem> createShovel(Tier tier)
	{
		return () -> new ShovelItem(tier, toolProperties());
	}

	public static Supplier<AxeItem> createAxe(Tier tier)
	{
		return () -> new AxeItem(tier, toolProperties());
	}

	public static Supplier<PickaxeItem> createPickaxe(Tier tier)
	{
		return () -> new PickaxeItem(tier, toolProperties());
	}

	public static Supplier<SwordItem> createSword(Tier tier)
	{
		return () -> new SwordItem(tier, toolProperties());
	}

	public static Supplier<HoeItem> createHoe(Tier tier)
	{
		return () -> new HoeItem(tier, toolProperties());
	}

	private static Item.Properties toolProperties()
	{
		return new Item.Properties().stacksTo(1);
	}
}
