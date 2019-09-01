/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;


import blusunrize.immersiveengineering.common.IERecipes;
import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags.Wrapper;
import net.minecraft.tags.Tag;

public class IETags
{
	public static final Tag<Item> STEEL_INGOTS = new Wrapper(IERecipes.getIngot("steel"));
}
