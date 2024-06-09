/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.common.register.IEArmorMaterials;
import net.minecraft.world.item.ArmorItem;

public class SteelArmorItem extends ArmorItem
{
	public SteelArmorItem(Type type)
	{
		super(IEArmorMaterials.STEEL, type, new Properties().stacksTo(1));
	}
}