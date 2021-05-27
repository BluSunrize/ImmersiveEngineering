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
import net.minecraft.item.*;

public class IETools
{
	public static ShovelItem createShovel(IItemTier tier, String name)
	{
		ShovelItem ret = new ShovelItem(tier, 1.5f, -3.0f, toolProperties());
		return init(ret, name);
	}

	public static AxeItem createAxe(IItemTier tier, String name)
	{
		AxeItem ret = new AxeItem(tier, 5.5f, -3.1f, toolProperties());
		return init(ret, name);
	}

	public static PickaxeItem createPickaxe(IItemTier tier, String name)
	{
		PickaxeItem ret = new PickaxeItem(tier, 1, -2.8f, toolProperties());
		return init(ret, name);
	}

	public static SwordItem createSword(IItemTier tier, String name)
	{
		SwordItem ret = new SwordItem(tier, 3, -2.4F, toolProperties());
		return init(ret, name);
	}

	public static HoeItem createHoe(IItemTier tier, String name)
	{
		HoeItem ret = new HoeItem(tier, (int)-tier.getAttackDamage(), 0.0F, toolProperties());
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
		return new Item.Properties().group(ImmersiveEngineering.ITEM_GROUP).maxStackSize(1);
	}
}
