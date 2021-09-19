/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public class IETools
{
	public static ShovelItem createShovel(Tier tier, String name)
	{
		ShovelItem ret = new ShovelItem(tier, 1.5f, -3.0f, toolProperties());
		return init(ret, name);
	}

	public static AxeItem createAxe(Tier tier, String name)
	{
		AxeItem ret = new AxeItem(tier, 5.5f, -3.1f, toolProperties());
		return init(ret, name);
	}

	public static PickaxeItem createPickaxe(Tier tier, String name)
	{
		PickaxeItem ret = new PickaxeItem(tier, 1, -2.8f, toolProperties());
		return init(ret, name);
	}

	public static SwordItem createSword(Tier tier, String name)
	{
		SwordItem ret = new SwordItem(tier, 3, -2.4F, toolProperties());
		return init(ret, name);
	}

	public static HoeItem createHoe(Tier tier, String name)
	{
		HoeItem ret = new HoeItem(tier, (int)-tier.getAttackDamageBonus(), 0.0F, toolProperties());
		return init(ret, name);
	}

	private static <I extends Item> I init(I i, String name)
	{
		i.setRegistryName(ImmersiveEngineering.MODID, name);
		IEContent.registeredIEItems.add(i);
		return i;
	}

	private static Item.Properties toolProperties()
	{
		return new Item.Properties().tab(ImmersiveEngineering.ITEM_GROUP).stacksTo(1);
	}
}
